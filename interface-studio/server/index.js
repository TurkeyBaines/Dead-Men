import express from 'express';
import path from 'node:path';
import fs from 'node:fs';
import { fileURLToPath } from 'node:url';
import { CacheStore } from './cache/store.js';
import { decodeComponent, encodeComponent } from './cache/interface.js';
import { decodeScript, encodeScript, disassemble, opcodeName } from './cache/script.js';
import { decodeSprites, encodePng } from './cache/sprite.js';
import { OPCODE_NAMES } from './cache/opcodes.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const CLIENT_DIR = path.join(__dirname, '..', 'client');
const INTERFACE_INDEX = 3;
const SPRITE_INDEX = 8;
const SCRIPT_INDEX = 12;

const app = express();
app.use(express.json({ limit: '64mb' }));
app.use(express.static(CLIENT_DIR));

/** @type {CacheStore|null} */
let store = null;
let cachePath = null;
const spriteCache = new Map(); // group -> frames

function requireStore(res) {
  if (!store) { res.status(409).json({ error: 'No cache open. POST /api/open first.' }); return false; }
  return true;
}

// Suggested default cache locations (the bundled Dead-Men cache, etc.)
const DEFAULT_CACHE_CANDIDATES = [
  path.resolve(__dirname, '..', '..', 'DeadMen', 'Cache'),
  path.resolve(process.cwd(), 'DeadMen', 'Cache'),
  path.resolve(process.cwd(), 'Cache'),
];

app.get('/api/status', (req, res) => {
  const defaults = DEFAULT_CACHE_CANDIDATES.filter((p) => fs.existsSync(path.join(p, 'main_file_cache.dat2')));
  res.json({
    open: !!store,
    cachePath,
    suggestions: defaults,
  });
});

app.post('/api/open', (req, res) => {
  const dir = req.body.path;
  if (!dir) return res.status(400).json({ error: 'path required' });
  if (!fs.existsSync(path.join(dir, 'main_file_cache.dat2'))) {
    return res.status(400).json({ error: 'main_file_cache.dat2 not found in ' + dir });
  }
  try {
    if (store) store.close();
    spriteCache.clear();
    store = new CacheStore(dir);
    cachePath = dir;
    const rt = store.getReferenceTable(INTERFACE_INDEX);
    res.json({ open: true, cachePath, interfaceCount: rt.archives.length, indexes: store.listIndexes() });
  } catch (e) {
    store = null; cachePath = null;
    res.status(500).json({ error: String(e.message || e) });
  }
});

// Tree of main interfaces (archives in index 3) with child counts.
app.get('/api/tree', (req, res) => {
  if (!requireStore(res)) return;
  const rt = store.getReferenceTable(INTERFACE_INDEX);
  const list = rt.archives.map((a) => ({ id: a.id, childCount: a.files.length, revision: a.revision }));
  res.json({ interfaces: list });
});

// All components of one main interface, plus referenced CS2 scripts.
app.get('/api/interface/:archive', (req, res) => {
  if (!requireStore(res)) return;
  const archive = Number(req.params.archive);
  try {
    const files = store.readArchiveFiles(INTERFACE_INDEX, archive);
    const components = [];
    const scriptIds = new Set();
    for (const fid of Object.keys(files).map(Number).sort((a, b) => a - b)) {
      const c = decodeComponent((archive << 16) | fid, files[fid]);
      c.fileId = fid;
      collectScriptIds(c, scriptIds);
      components.push(c);
    }
    res.json({ archive, components, scripts: [...scriptIds].sort((a, b) => a - b) });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

function collectScriptIds(c, set) {
  for (const key of Object.keys(c)) {
    if (key.endsWith('Listener') && Array.isArray(c[key]) && c[key].length) {
      const first = c[key][0];
      if (first && first.type === 'int') set.add(first.value);
    }
  }
}

// Save a whole interface (array of component objects). The numeric constraint
// keeps this from shadowing /api/interface/copy and /api/interface/new.
app.post('/api/interface/:archive(\\d+)', (req, res) => {
  if (!requireStore(res)) return;
  const archive = Number(req.params.archive);
  const components = req.body.components;
  if (!Array.isArray(components)) return res.status(400).json({ error: 'components array required' });
  try {
    const contents = {};
    const fileIds = [];
    for (const c of components) {
      const fid = c.fileId ?? (c.id & 0xFFFF);
      contents[fid] = encodeComponent(c);
      fileIds.push(fid);
    }
    fileIds.sort((a, b) => a - b);
    const result = store.writeArchiveFiles(INTERFACE_INDEX, archive, contents, fileIds);
    res.json({ ok: true, archive, ...result });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

// Copy an existing interface to a new (or specified) archive id.
app.post('/api/interface/copy', (req, res) => {
  if (!requireStore(res)) return;
  const from = Number(req.body.from);
  let to = req.body.to != null ? Number(req.body.to) : store.nextArchiveId(INTERFACE_INDEX);
  try {
    const files = store.readArchiveFiles(INTERFACE_INDEX, from);
    const contents = {};
    const fileIds = [];
    for (const fid of Object.keys(files).map(Number)) {
      // Re-point parentId high bits to the new archive so the copy is self-contained.
      const c = decodeComponent((from << 16) | fid, files[fid]);
      if (c.parentId !== -1 && c.parentId != null && (c.parentId >> 16) === from) {
        c.parentId = (to << 16) | (c.parentId & 0xFFFF);
      }
      contents[fid] = encodeComponent(c);
      fileIds.push(fid);
    }
    fileIds.sort((a, b) => a - b);
    const result = store.writeArchiveFiles(INTERFACE_INDEX, to, contents, fileIds);
    res.json({ ok: true, from, to, ...result });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

// Create a brand-new interface with a single root container component.
app.post('/api/interface/new', (req, res) => {
  if (!requireStore(res)) return;
  const to = req.body.to != null ? Number(req.body.to) : store.nextArchiveId(INTERFACE_INDEX);
  const width = req.body.width || 512;
  const height = req.body.height || 334;
  try {
    const root = newComponent((to << 16) | 0, 0, -1); // type 0 layer/container
    root.originalWidth = width;
    root.originalHeight = height;
    const contents = { 0: encodeComponent(root) };
    const result = store.writeArchiveFiles(INTERFACE_INDEX, to, contents, [0]);
    res.json({ ok: true, archive: to, ...result });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

// Append a new child component to an interface; returns the new file id.
app.post('/api/interface/:archive/component', (req, res) => {
  if (!requireStore(res)) return;
  const archive = Number(req.params.archive);
  const type = req.body.type ?? 4;
  try {
    const files = store.readArchiveFiles(INTERFACE_INDEX, archive);
    const fids = Object.keys(files).map(Number);
    const newFid = (fids.length ? Math.max(...fids) : -1) + 1;
    const parentId = req.body.parentId != null ? Number(req.body.parentId) : ((archive << 16) | 0);
    const comp = newComponent((archive << 16) | newFid, type, parentId);
    Object.assign(comp, req.body.fields || {});
    const contents = {};
    for (const fid of fids) contents[fid] = files[fid];
    contents[newFid] = encodeComponent(comp);
    const fileIds = [...fids, newFid].sort((a, b) => a - b);
    const result = store.writeArchiveFiles(INTERFACE_INDEX, archive, contents, fileIds);
    res.json({ ok: true, archive, fileId: newFid, ...result });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

function newComponent(id, type, parentId) {
  return {
    id, isIf3: true, type, contentType: 0,
    originalX: 0, originalY: 0, originalWidth: 50, originalHeight: 20,
    widthMode: 0, heightMode: 0, xPositionMode: 0, yPositionMode: 0,
    parentId, isHidden: false,
    clickMask: 0, name: '', actions: [],
    dragDeadZone: 0, dragDeadTime: 0, dragRenderBehavior: false, targetVerb: '',
    // type-specific sensible defaults
    ...(type === 4 ? { fontId: 495, text: 'New text', lineHeight: 0, xTextAlignment: 0, yTextAlignment: 0, textShadowed: true, textColor: 0xff981f } : {}),
    ...(type === 3 ? { textColor: 0x000000, filled: true, opacity: 0 } : {}),
    ...(type === 5 ? { spriteId: -1, textureId: 0, spriteTiling: false, opacity: 0, borderType: 0, shadowColor: 0, flippedVertically: false, flippedHorizontally: false } : {}),
    ...(type === 0 ? { scrollWidth: 0, scrollHeight: 0, noClickThrough: false } : {}),
  };
}

// ---- sprites ----
app.get('/api/sprite/:group/:frame', (req, res) => {
  if (!requireStore(res)) return;
  const group = Number(req.params.group);
  const frame = Number(req.params.frame.replace(/\.png$/, ''));
  try {
    let frames = spriteCache.get(group);
    if (!frames) {
      const files = store.readArchiveFiles(SPRITE_INDEX, group);
      const fid = Object.keys(files).map(Number)[0];
      frames = decodeSprites(group, files[fid]);
      spriteCache.set(group, frames);
    }
    const f = frames[frame] || frames[0];
    if (!f) return res.status(404).end();
    const png = encodePng(f.width, f.height, f.rgba);
    res.set('Content-Type', 'image/png');
    res.set('Cache-Control', 'public, max-age=3600');
    res.send(png);
  } catch (e) {
    res.status(404).json({ error: String(e.message || e) });
  }
});

// ---- CS2 scripts ----
app.get('/api/scripts', (req, res) => {
  if (!requireStore(res)) return;
  const rt = store.getReferenceTable(SCRIPT_INDEX);
  res.json({ scripts: rt.archives.map((a) => a.id) });
});

app.get('/api/script/:id', (req, res) => {
  if (!requireStore(res)) return;
  const id = Number(req.params.id);
  try {
    const files = store.readArchiveFiles(SCRIPT_INDEX, id);
    const fid = Object.keys(files).map(Number)[0];
    const def = decodeScript(id, files[fid]);
    res.json({ ...def, disassembly: disassemble(def), fileId: fid });
  } catch (e) {
    res.status(404).json({ error: String(e.message || e) });
  }
});

app.post('/api/script/:id', (req, res) => {
  if (!requireStore(res)) return;
  const id = Number(req.params.id);
  const def = req.body;
  try {
    const blob = encodeScript({ id, ...def });
    const files = store.readArchiveFiles(SCRIPT_INDEX, id);
    const fid = Object.keys(files).map(Number)[0] ?? 0;
    const result = store.writeArchiveFiles(SCRIPT_INDEX, id, { [fid]: blob }, [fid]);
    res.json({ ok: true, id, ...result });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

app.post('/api/script/new', (req, res) => {
  if (!requireStore(res)) return;
  const id = req.body.id != null ? Number(req.body.id) : store.nextArchiveId(SCRIPT_INDEX);
  const def = req.body.def || { instructions: [{ opcode: 21, intOperand: 0 }], localIntCount: 0, localStringCount: 0, intStackCount: 0, stringStackCount: 0, switches: [], name: null };
  try {
    const blob = encodeScript({ id, ...def });
    const result = store.writeArchiveFiles(SCRIPT_INDEX, id, { 0: blob }, [0]);
    res.json({ ok: true, id, ...result });
  } catch (e) {
    res.status(500).json({ error: String(e.message || e) });
  }
});

app.get('/api/opcodes', (req, res) => res.json(OPCODE_NAMES));

const PORT = process.env.PORT || 4173;
app.listen(PORT, () => {
  console.log(`Dead-Men Interface Studio running at http://localhost:${PORT}`);
  const def = DEFAULT_CACHE_CANDIDATES.find((p) => fs.existsSync(path.join(p, 'main_file_cache.dat2')));
  if (def) console.log(`Detected cache at: ${def}`);
});
