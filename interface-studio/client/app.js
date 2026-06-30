// Dead-Men Interface Studio — frontend logic.
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => [...document.querySelectorAll(sel)];

const api = {
  async get(url) { const r = await fetch(url); if (!r.ok) throw new Error((await r.json()).error || r.statusText); return r.json(); },
  async post(url, body) { const r = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body || {}) }); if (!r.ok) throw new Error((await r.json()).error || r.statusText); return r.json(); },
};

const state = {
  tree: [],
  archive: null,
  components: [],      // decoded components for current interface
  selectedFileId: null,
  scripts: [],
  usedScripts: [],
  currentScript: null,
  dirty: false,
};

// Component type metadata
const TYPE_NAMES = { 0: 'Layer', 1: 'Text(legacy)', 2: 'Inventory', 3: 'Rectangle', 4: 'Text', 5: 'Graphic', 6: 'Model', 7: 'ItemList', 8: 'Tooltip', 9: 'Line' };
const POS_MODES = { 0: 'Absolute', 1: 'Centred', 2: 'Right/Bottom', 3: 'Abs ×16384ths', 4: 'Centred ×16384ths', 5: 'Right ×16384ths' };
const SIZE_MODES = { 0: 'Absolute', 1: 'Parent − size', 2: 'Proportional ×16384ths' };

// ----------------------------------------------------------------------------
// Open / cache
// ----------------------------------------------------------------------------
async function init() {
  const status = await api.get('/api/status');
  if (status.suggestions?.length) $('#cache-path').value = status.suggestions[0];
  const sug = $('#suggestions');
  sug.innerHTML = '';
  for (const p of status.suggestions || []) {
    const el = document.createElement('div');
    el.className = 'sug'; el.textContent = '📁 ' + p;
    el.onclick = () => { $('#cache-path').value = p; openCache(); };
    sug.appendChild(el);
  }
  if (status.open) { await onOpened(status); }
}

async function openCache() {
  const path = $('#cache-path').value.trim();
  $('#open-error').textContent = '';
  try {
    const res = await api.post('/api/open', { path });
    await onOpened(res);
  } catch (e) {
    $('#open-error').textContent = e.message;
  }
}

async function onOpened(res) {
  $('#open-overlay').classList.add('hidden');
  $('#topbar').classList.remove('hidden');
  $('#app').classList.remove('hidden');
  $('#cache-info').textContent = `${res.cachePath || ''} — ${res.interfaceCount ?? '?'} interfaces`;
  await loadTree();
  await loadScriptList();
}

// ----------------------------------------------------------------------------
// Interface tree
// ----------------------------------------------------------------------------
async function loadTree() {
  const { interfaces } = await api.get('/api/tree');
  state.tree = interfaces;
  renderTree();
}

function renderTree() {
  const filter = $('#iface-search').value.trim();
  const list = $('#iface-list');
  list.innerHTML = '';
  const items = filter ? state.tree.filter((i) => String(i.id).includes(filter)) : state.tree;
  for (const i of items.slice(0, 1500)) {
    const node = document.createElement('div');
    node.className = 'node' + (i.id === state.archive ? ' selected' : '');
    node.innerHTML = `<span>Interface ${i.id}</span><span class="meta">${i.childCount} comp</span>`;
    node.onclick = () => selectInterface(i.id);
    list.appendChild(node);
  }
}

async function selectInterface(archive) {
  if (state.dirty && !confirm('Discard unsaved changes to interface ' + state.archive + '?')) return;
  state.archive = archive;
  state.dirty = false;
  const data = await api.get('/api/interface/' + archive);
  state.components = data.components;
  state.usedScripts = data.scripts;
  state.selectedFileId = state.components[0]?.fileId ?? null;
  renderTree();
  $('#render-title').textContent = `Interface ${archive} — ${state.components.length} components`;
  autoSizeCanvas();
  renderStage();
  renderComponentTree();
  renderEditForm();
  renderUsedScripts();
  renderGameframe();
}

// ----------------------------------------------------------------------------
// Layout resolution (OSRS position/size modes)
// ----------------------------------------------------------------------------
function resolveSize(mode, orig, parent) {
  if (mode === 1) return parent - orig;
  if (mode === 2) return Math.floor((parent * orig) / 16384);
  return orig;
}
function resolvePos(mode, orig, parent, dim) {
  switch (mode) {
    case 1: return Math.floor((parent - dim) / 2) + orig;
    case 2: return parent - dim - orig;
    case 3: return Math.floor((orig * parent) / 16384);
    case 4: return Math.floor((orig * parent) / 16384) + Math.floor((parent - dim) / 2);
    case 5: return parent - dim - Math.floor((orig * parent) / 16384);
    default: return orig;
  }
}

function computeLayout(rootW, rootH) {
  const byId = new Map(state.components.map((c) => [c.id, c]));
  const childrenOf = new Map();
  const roots = [];
  for (const c of state.components) {
    const pid = c.parentId;
    if (pid == null || pid === -1 || !byId.has(pid)) roots.push(c);
    else { if (!childrenOf.has(pid)) childrenOf.set(pid, []); childrenOf.get(pid).push(c); }
  }
  const layout = new Map();
  const walk = (c, px, py, pw, ph) => {
    const w = resolveSize(c.widthMode || 0, c.originalWidth || 0, pw);
    const h = resolveSize(c.heightMode || 0, c.originalHeight || 0, ph);
    const x = px + resolvePos(c.xPositionMode || 0, c.originalX || 0, pw, w);
    const y = py + resolvePos(c.yPositionMode || 0, c.originalY || 0, ph, h);
    layout.set(c.id, { x, y, w, h });
    for (const ch of childrenOf.get(c.id) || []) walk(ch, x, y, w, h);
  };
  for (const r of roots) walk(r, 0, 0, rootW, rootH);
  return layout;
}

function autoSizeCanvas() {
  // Prefer an explicit root container size, else bounding box, else default.
  const root = state.components.find((c) => (c.parentId === -1 || c.parentId == null) && c.type === 0 && c.originalWidth > 0);
  let w = root?.originalWidth || 0, h = root?.originalHeight || 0;
  if (!w || !h) {
    const lay = computeLayout(520, 340);
    let maxX = 0, maxY = 0;
    for (const { x, y, w: cw, h: ch } of lay.values()) { maxX = Math.max(maxX, x + cw); maxY = Math.max(maxY, y + ch); }
    w = w || Math.max(maxX, 100); h = h || Math.max(maxY, 100);
  }
  $('#canvas-w').value = w;
  $('#canvas-h').value = h;
}

// ----------------------------------------------------------------------------
// Render stage (DOM-based for easy selection)
// ----------------------------------------------------------------------------
function renderStage() {
  const stage = $('#render-stage');
  const W = Number($('#canvas-w').value) || 520;
  const H = Number($('#canvas-h').value) || 340;
  const zoom = Number($('#zoom').value) || 1;
  const showBounds = $('#show-bounds').checked;
  stage.style.width = W + 'px';
  stage.style.height = H + 'px';
  stage.style.transform = `scale(${zoom})`;
  stage.innerHTML = '';
  const layout = computeLayout(W, H);
  // paint parents before children (state order is by file id which is roughly hierarchical)
  for (const c of state.components) {
    const box = layout.get(c.id);
    if (!box) continue;
    const el = renderComponentEl(c, box, showBounds);
    stage.appendChild(el);
  }
}

function rgbToHex(v) {
  if (v == null) v = 0;
  const u = v & 0xFFFFFF;
  return '#' + u.toString(16).padStart(6, '0');
}

function renderComponentEl(c, box, showBounds) {
  const el = document.createElement('div');
  el.className = 'comp' + (showBounds ? ' outline' : '') + (c.fileId === state.selectedFileId ? ' selected' : '');
  el.style.left = box.x + 'px';
  el.style.top = box.y + 'px';
  el.style.width = box.w + 'px';
  el.style.height = box.h + 'px';
  el.title = `[${c.fileId}] ${TYPE_NAMES[c.type] || c.type}`;
  el.onclick = (e) => { e.stopPropagation(); selectComponent(c.fileId); switchTab('edit'); };

  if (c.type === 5 && c.spriteId != null && c.spriteId >= 0) {
    const img = `/api/sprite/${c.spriteId}/0.png`;
    if (c.spriteTiling) { el.style.background = `url(${img})`; el.style.backgroundRepeat = 'repeat'; }
    else {
      const im = document.createElement('img');
      im.src = img; im.style.width = '100%'; im.style.height = '100%';
      im.style.objectFit = 'fill'; im.draggable = false;
      im.style.transform = `scale(${c.flippedHorizontally ? -1 : 1}, ${c.flippedVertically ? -1 : 1})`;
      im.onerror = () => { el.style.background = '#ffffff11'; };
      el.appendChild(im);
    }
    if (c.opacity) el.style.opacity = (255 - c.opacity) / 255;
  } else if (c.type === 3) {
    if (c.filled) { el.style.background = rgbToHex(c.textColor); el.style.opacity = (255 - (c.opacity || 0)) / 255; }
    else { el.style.border = '1px solid ' + rgbToHex(c.textColor); }
  } else if (c.type === 4 || c.type === 1) {
    const t = document.createElement('div');
    t.className = 'ctext';
    t.textContent = c.text || '';
    t.style.color = rgbToHex(c.textColor);
    t.style.fontSize = '12px';
    t.style.justifyContent = c.xTextAlignment === 1 ? 'center' : c.xTextAlignment === 2 ? 'flex-end' : 'flex-start';
    t.style.alignItems = c.yTextAlignment === 1 ? 'center' : c.yTextAlignment === 2 ? 'flex-end' : 'flex-start';
    if (c.textShadowed) t.style.textShadow = '1px 1px 0 #000';
    t.style.whiteSpace = 'pre-wrap'; t.style.lineHeight = (c.lineHeight || 14) + 'px';
    el.appendChild(t);
  } else if (c.type === 6) {
    el.style.background = 'repeating-linear-gradient(45deg,#3a3a5a,#3a3a5a 6px,#2c2c45 6px,#2c2c45 12px)';
    el.innerHTML = `<div style="color:#9aa;font-size:10px;padding:2px">model ${c.modelId}</div>`;
  } else if (c.type === 9) {
    el.style.borderTop = (c.lineWidth || 1) + 'px solid ' + rgbToHex(c.textColor);
  } else if (c.type === 0) {
    if (showBounds) el.style.outline = '1px dotted #5b86c955';
  } else {
    el.style.background = '#ffffff0a';
  }
  return el;
}

// ----------------------------------------------------------------------------
// Component tree (Edit tab)
// ----------------------------------------------------------------------------
function renderComponentTree() {
  const byId = new Map(state.components.map((c) => [c.id, c]));
  const childrenOf = new Map();
  const roots = [];
  for (const c of state.components) {
    const pid = c.parentId;
    if (pid == null || pid === -1 || !byId.has(pid)) roots.push(c);
    else { if (!childrenOf.has(pid)) childrenOf.set(pid, []); childrenOf.get(pid).push(c); }
  }
  const container = $('#comp-tree');
  container.innerHTML = '';
  const addNode = (c, depth) => {
    const node = document.createElement('div');
    node.className = 'node ' + (depth === 1 ? 'child' : depth >= 2 ? 'child2' : '') + (c.fileId === state.selectedFileId ? ' selected' : '');
    node.innerHTML = `<span class="badge">${c.fileId}</span><span>${TYPE_NAMES[c.type] || 't' + c.type}</span>` +
      (c.text ? `<span class="meta">${escapeHtml(c.text.slice(0, 14))}</span>` : c.name ? `<span class="meta">${escapeHtml(c.name.slice(0, 14))}</span>` : '');
    node.onclick = () => selectComponent(c.fileId);
    container.appendChild(node);
    for (const ch of childrenOf.get(c.id) || []) addNode(ch, depth + 1);
  };
  for (const r of roots) addNode(r, 0);
}

function selectComponent(fileId) {
  state.selectedFileId = fileId;
  renderComponentTree();
  renderEditForm();
  renderStage();
}

function selectedComponent() { return state.components.find((c) => c.fileId === state.selectedFileId); }

// ----------------------------------------------------------------------------
// Edit form
// ----------------------------------------------------------------------------
function renderEditForm() {
  const c = selectedComponent();
  const form = $('#edit-form');
  if (!c) { form.innerHTML = '<p class="hint">Select a component to edit.</p>'; return; }
  form.innerHTML = '';

  const setField = (prop, value) => { c[prop] = value; markDirty(); renderStage(); };

  // --- General / layout ---
  form.appendChild(group('Component', [
    rowStatic('File id', String(c.fileId)),
    rowSelect('Type', c.type, TYPE_NAMES, (v) => { c.type = Number(v); markDirty(); renderEditForm(); renderStage(); }),
    rowText('Name', c.name || '', (v) => setField('name', v)),
    rowCheck('Hidden', !!c.isHidden, (v) => setField('isHidden', v)),
  ]));

  form.appendChild(group('Position & size', [
    rowNum('X', c.originalX || 0, (v) => setField('originalX', v)),
    rowNum('Y', c.originalY || 0, (v) => setField('originalY', v)),
    rowNum('Width', c.originalWidth || 0, (v) => setField('originalWidth', v)),
    rowNum('Height', c.originalHeight || 0, (v) => setField('originalHeight', v)),
    rowSelect('X mode', c.xPositionMode || 0, POS_MODES, (v) => setField('xPositionMode', Number(v))),
    rowSelect('Y mode', c.yPositionMode || 0, POS_MODES, (v) => setField('yPositionMode', Number(v))),
    rowSelect('Width mode', c.widthMode || 0, SIZE_MODES, (v) => setField('widthMode', Number(v))),
    rowSelect('Height mode', c.heightMode || 0, SIZE_MODES, (v) => setField('heightMode', Number(v))),
  ]));

  // --- type specific ---
  if (c.type === 4 || c.type === 1) {
    form.appendChild(group('Text', [
      rowText('Text', c.text || '', (v) => setField('text', v)),
      rowNum('Font id', c.fontId ?? -1, (v) => setField('fontId', v)),
      rowColor('Colour', c.textColor || 0, (v) => setField('textColor', v)),
      rowSelect('H align', c.xTextAlignment || 0, { 0: 'Left', 1: 'Centre', 2: 'Right' }, (v) => setField('xTextAlignment', Number(v))),
      rowSelect('V align', c.yTextAlignment || 0, { 0: 'Top', 1: 'Middle', 2: 'Bottom' }, (v) => setField('yTextAlignment', Number(v))),
      rowCheck('Shadow', !!c.textShadowed, (v) => setField('textShadowed', v)),
      rowNum('Line height', c.lineHeight || 0, (v) => setField('lineHeight', v)),
    ]));
  }
  if (c.type === 3) {
    form.appendChild(group('Rectangle', [
      rowColor('Colour', c.textColor || 0, (v) => setField('textColor', v)),
      rowCheck('Filled', !!c.filled, (v) => setField('filled', v)),
      rowNum('Transparency', c.opacity || 0, (v) => setField('opacity', v)),
    ]));
  }
  if (c.type === 5) {
    form.appendChild(group('Graphic', [
      rowSprite('Sprite id', c.spriteId ?? -1, (v) => setField('spriteId', v)),
      rowNum('Texture id', c.textureId || 0, (v) => setField('textureId', v)),
      rowCheck('Tiling', !!c.spriteTiling, (v) => setField('spriteTiling', v)),
      rowNum('Transparency', c.opacity || 0, (v) => setField('opacity', v)),
      rowCheck('Flip H', !!c.flippedHorizontally, (v) => setField('flippedHorizontally', v)),
      rowCheck('Flip V', !!c.flippedVertically, (v) => setField('flippedVertically', v)),
    ]));
  }
  if (c.type === 6) {
    form.appendChild(group('Model', [
      rowNum('Model id', c.modelId ?? -1, (v) => setField('modelId', v)),
      rowNum('Animation', c.animation ?? -1, (v) => setField('animation', v)),
      rowNum('Zoom', c.modelZoom || 0, (v) => setField('modelZoom', v)),
      rowNum('Rotate X', c.rotationX || 0, (v) => setField('rotationX', v)),
      rowNum('Rotate Y', c.rotationY || 0, (v) => setField('rotationY', v)),
      rowNum('Rotate Z', c.rotationZ || 0, (v) => setField('rotationZ', v)),
    ]));
  }

  // --- interactions (easy mode) ---
  form.appendChild(interactionsGroup(c));
  // --- delete ---
  const del = document.createElement('button');
  del.textContent = '🗑 Delete component';
  del.style.marginTop = '8px';
  del.onclick = () => deleteComponent(c);
  form.appendChild(del);
}

// Easy interactions: right-click options drive actions[] + clickMask op bits,
// and optionally bind a CS2 script to run on op.
function interactionsGroup(c) {
  const g = document.createElement('div');
  g.className = 'field-group';
  g.innerHTML = '<h4>Interactions</h4>';

  const list = document.createElement('div');
  list.className = 'actions-list';
  const actions = c.actions || (c.actions = []);
  actions.forEach((a, i) => {
    const row = document.createElement('div');
    row.className = 'action-row';
    const inp = document.createElement('input');
    inp.value = a || '';
    inp.placeholder = `Option ${i + 1} (e.g. "Open")`;
    inp.oninput = () => { actions[i] = inp.value; syncClickMask(c); markDirty(); };
    const rm = document.createElement('button');
    rm.className = 'mini'; rm.textContent = '✕';
    rm.onclick = () => { actions.splice(i, 1); syncClickMask(c); markDirty(); renderEditForm(); };
    row.append(inp, rm);
    list.appendChild(row);
  });
  g.appendChild(list);

  const add = document.createElement('button');
  add.className = 'mini'; add.textContent = '＋ Add right-click option';
  add.onclick = () => { actions.push('Select'); syncClickMask(c); markDirty(); renderEditForm(); };
  g.appendChild(add);

  // CS2 on-click binding
  const builder = document.createElement('div');
  builder.className = 'interaction-builder';
  const onOp = c.onOpListener;
  const boundScript = onOp && onOp[0] && onOp[0].type === 'int' ? onOp[0].value : null;
  builder.innerHTML = `<div style="margin-bottom:6px"><strong>Run CS2 script on click</strong></div>`;
  const scriptRow = document.createElement('div');
  scriptRow.className = 'color-row';
  const scriptInput = document.createElement('input');
  scriptInput.type = 'number';
  scriptInput.placeholder = 'script id';
  scriptInput.value = boundScript ?? '';
  scriptInput.style.width = '100px';
  const bindBtn = document.createElement('button');
  bindBtn.className = 'mini'; bindBtn.textContent = 'Bind';
  bindBtn.onclick = () => {
    const sid = Number(scriptInput.value);
    if (!Number.isFinite(sid)) return;
    c.onOpListener = [{ type: 'int', value: sid }];
    c.hasListener = true;
    if (!c.clickMask) c.clickMask = 0;
    markDirty(); toast('Bound onOp → script ' + sid); renderEditForm();
  };
  const genBtn = document.createElement('button');
  genBtn.className = 'mini'; genBtn.textContent = '✨ Generate new script';
  genBtn.title = 'Creates a CS2 script (a CC_SETHIDE toggle stub) and binds it on click';
  genBtn.onclick = () => generateInteractionScript(c);
  scriptRow.append(scriptInput, bindBtn, genBtn);
  builder.appendChild(scriptRow);
  if (boundScript != null) {
    const open = document.createElement('button');
    open.className = 'mini'; open.textContent = 'Open script ' + boundScript;
    open.style.marginTop = '6px';
    open.onclick = () => { switchTab('scripts'); loadScript(boundScript); };
    builder.appendChild(open);
  }
  g.appendChild(builder);

  const mask = document.createElement('div');
  mask.style.cssText = 'margin-top:8px;color:var(--text-dim);font-size:11px';
  mask.textContent = `clickMask = 0x${(c.clickMask || 0).toString(16)} (${c.clickMask || 0})`;
  g.appendChild(mask);
  return g;
}

// Enable the menu-option bit for each defined action: option N (1-based) -> 1<<N.
function syncClickMask(c) {
  let mask = c.clickMask || 0;
  // clear option bits 1..10 then re-set for present actions
  for (let n = 1; n <= 10; n++) mask &= ~(1 << n);
  (c.actions || []).forEach((a, i) => { if (a && a.length) mask |= 1 << (i + 1); });
  c.clickMask = mask;
}

async function generateInteractionScript(c) {
  try {
    // A minimal, valid CS2 stub: toggles this component's hidden flag.
    // cc_sethide takes (component_id, bool) -> here we just return; the user
    // can flesh it out in the Scripts tab. Bind it as the onOp handler.
    const def = {
      name: `iface_${state.archive}_${c.fileId}_onop`,
      localIntCount: 0, localStringCount: 0, intStackCount: 0, stringStackCount: 0,
      switches: [],
      instructions: [{ opcode: 21, intOperand: 0 }], // RETURN
    };
    const res = await api.post('/api/script/new', { def });
    c.onOpListener = [{ type: 'int', value: res.id }];
    c.hasListener = true;
    markDirty();
    toast('Created + bound script ' + res.id);
    await loadScriptList();
    renderEditForm();
  } catch (e) { toast(e.message, true); }
}

function deleteComponent(c) {
  if (c.parentId === -1 && state.components.filter((x) => x.parentId === -1).length === 1) {
    return toast('Cannot delete the only root component', true);
  }
  if (!confirm(`Delete component ${c.fileId}?`)) return;
  state.components = state.components.filter((x) => x.fileId !== c.fileId);
  state.selectedFileId = state.components[0]?.fileId ?? null;
  markDirty();
  renderComponentTree(); renderEditForm(); renderStage();
}

// ----------------------------------------------------------------------------
// Form widget helpers
// ----------------------------------------------------------------------------
function group(title, rows) {
  const g = document.createElement('div');
  g.className = 'field-group';
  g.innerHTML = `<h4>${title}</h4>`;
  rows.forEach((r) => g.appendChild(r));
  return g;
}
function field(label, control) {
  const f = document.createElement('div');
  f.className = 'field';
  const l = document.createElement('label'); l.textContent = label;
  f.append(l, control);
  return f;
}
function rowStatic(label, value) {
  const span = document.createElement('div'); span.textContent = value; span.style.color = 'var(--text-dim)';
  return field(label, span);
}
function rowText(label, value, on) {
  const inp = document.createElement('input'); inp.value = value; inp.oninput = () => on(inp.value);
  return field(label, inp);
}
function rowNum(label, value, on) {
  const inp = document.createElement('input'); inp.type = 'number'; inp.value = value;
  inp.oninput = () => on(inp.value === '' ? 0 : Number(inp.value));
  return field(label, inp);
}
function rowCheck(label, value, on) {
  const inp = document.createElement('input'); inp.type = 'checkbox'; inp.checked = value;
  inp.onchange = () => on(inp.checked);
  return field(label, inp);
}
function rowSelect(label, value, options, on) {
  const sel = document.createElement('select');
  for (const [k, v] of Object.entries(options)) {
    const o = document.createElement('option'); o.value = k; o.textContent = `${k}: ${v}`;
    if (Number(k) === Number(value)) o.selected = true;
    sel.appendChild(o);
  }
  sel.onchange = () => on(sel.value);
  return field(label, sel);
}
function rowColor(label, value, on) {
  const wrap = document.createElement('div'); wrap.className = 'color-row';
  const col = document.createElement('input'); col.type = 'color'; col.value = rgbToHex(value);
  const hex = document.createElement('input'); hex.value = rgbToHex(value); hex.style.width = '90px';
  const apply = (v) => { const n = parseInt(v.replace('#', ''), 16) || 0; col.value = rgbToHex(n); hex.value = rgbToHex(n); on(n); };
  col.oninput = () => apply(col.value);
  hex.oninput = () => apply(hex.value);
  wrap.append(col, hex);
  return field(label, wrap);
}
function rowSprite(label, value, on) {
  const wrap = document.createElement('div'); wrap.className = 'color-row';
  const inp = document.createElement('input'); inp.type = 'number'; inp.value = value; inp.style.width = '90px';
  const img = document.createElement('img'); img.style.cssText = 'height:28px;max-width:64px;background:#0003;border:1px solid var(--border)';
  const upd = () => { const v = Number(inp.value); img.src = v >= 0 ? `/api/sprite/${v}/0.png` : ''; on(v); };
  inp.oninput = upd;
  if (value >= 0) img.src = `/api/sprite/${value}/0.png`;
  img.onerror = () => { img.style.opacity = .2; };
  wrap.append(inp, img);
  return field(label, wrap);
}

// ----------------------------------------------------------------------------
// CS2 scripts tab
// ----------------------------------------------------------------------------
async function loadScriptList() {
  const { scripts } = await api.get('/api/scripts');
  state.scripts = scripts;
  renderScriptList();
}
function renderUsedScripts() {
  const cont = $('#script-used');
  cont.innerHTML = '';
  if (!state.usedScripts.length) { cont.innerHTML = '<div class="hint" style="padding:4px 8px">none detected</div>'; return; }
  for (const id of state.usedScripts) {
    const n = document.createElement('div'); n.className = 'node'; n.textContent = 'script ' + id;
    n.onclick = () => loadScript(id);
    cont.appendChild(n);
  }
}
function renderScriptList() {
  const filter = $('#script-search').value.trim();
  const cont = $('#script-all');
  cont.innerHTML = '';
  const items = filter ? state.scripts.filter((id) => String(id).includes(filter)) : state.scripts;
  for (const id of items.slice(0, 1000)) {
    const n = document.createElement('div'); n.className = 'node' + (state.currentScript?.id === id ? ' selected' : '');
    n.textContent = 'script ' + id;
    n.onclick = () => loadScript(id);
    cont.appendChild(n);
  }
}
async function loadScript(id) {
  try {
    const def = await api.get('/api/script/' + id);
    state.currentScript = def;
    $('#script-title').textContent = `Script ${id}` + (def.name ? ` — ${def.name}` : '');
    $('#script-meta').innerHTML =
      metaInput('localInt', def.localIntCount) + metaInput('localStr', def.localStringCount) +
      metaInput('intStack', def.intStackCount) + metaInput('strStack', def.stringStackCount) +
      `<span>${def.instructions.length} ops · ${def.switches?.length || 0} switch(es)</span>`;
    $('#script-asm').value = def.disassembly;
    $('#script-msg').textContent = '';
    renderScriptList();
  } catch (e) { toast(e.message, true); }
}
function metaInput(label, v) { return `<span>${label}: <strong>${v}</strong></span>`; }

async function saveScript() {
  const def = state.currentScript;
  if (!def) return;
  try {
    const parsed = parseDisassembly($('#script-asm').value, def);
    const res = await api.post('/api/script/' + def.id, parsed);
    $('#script-msg').textContent = `Saved script ${def.id} (rev ${res.revision}, crc ${res.crc}).`;
    $('#script-msg').className = 'msg ok';
    toast('Script saved to cache');
  } catch (e) {
    $('#script-msg').textContent = e.message;
    $('#script-msg').className = 'msg err';
  }
}

// Parse the editable disassembly back into instructions. Lines look like:
//   "  12  CC_SETHIDE   1"  or  "  3  SCONST  "text"". Comments (;) ignored.
let OPCODE_NAMES = {}, OPCODE_BY_NAME = {};
async function loadOpcodes() {
  OPCODE_NAMES = await api.get('/api/opcodes');
  for (const [k, v] of Object.entries(OPCODE_NAMES)) OPCODE_BY_NAME[v] = Number(k);
}
function parseDisassembly(text, base) {
  const instructions = [];
  for (const raw of text.split('\n')) {
    const line = raw.trim();
    if (!line || line.startsWith(';')) continue;
    // strip leading index number
    const m = line.match(/^(\d+)\s+(\S+)\s*(.*)$/);
    if (!m) throw new Error('Cannot parse line: ' + raw);
    const name = m[2];
    let operandStr = m[3].trim();
    const opcode = OPCODE_BY_NAME[name] ?? (name.startsWith('OP_') ? Number(name.slice(3)) : NaN);
    if (!Number.isFinite(opcode)) throw new Error('Unknown opcode: ' + name);
    const ins = { opcode };
    if (opcode === 3) { // SCONST
      ins.stringOperand = operandStr.replace(/^"(.*)"$/, '$1');
    } else {
      ins.intOperand = Number(operandStr) || 0;
    }
    instructions.push(ins);
  }
  return {
    name: base.name,
    localIntCount: base.localIntCount, localStringCount: base.localStringCount,
    intStackCount: base.intStackCount, stringStackCount: base.stringStackCount,
    switches: base.switches || [],
    instructions,
  };
}

// ----------------------------------------------------------------------------
// Gameframe render mode
// ----------------------------------------------------------------------------
function renderGameframe() {
  const stage = $('#gameframe-stage');
  const mode = $('#frame-mode').value;
  const anchor = $('#frame-anchor').value;
  const dims = mode === 'fixed' ? { w: 765, h: 503 } : { w: 1000, h: 700 };
  stage.style.width = dims.w + 'px';
  stage.style.height = dims.h + 'px';
  stage.innerHTML = '';

  // Approximate OSRS fixed-mode regions so position is meaningful.
  const regions = mode === 'fixed'
    ? { viewport: { x: 4, y: 4, w: 512, h: 334 }, minimap: { x: 520, y: 4, w: 240, h: 160 }, tabs: { x: 520, y: 168, w: 240, h: 170 }, chat: { x: 4, y: 342, w: 512, h: 142 } }
    : { viewport: { x: 0, y: 0, w: dims.w, h: dims.h }, minimap: { x: dims.w - 160, y: 4, w: 156, h: 156 }, tabs: { x: dims.w - 240, y: dims.h - 280, w: 236, h: 276 }, chat: { x: 4, y: dims.h - 170, w: 519, h: 165 } };
  for (const [name, r] of Object.entries(regions)) {
    const d = document.createElement('div');
    d.className = 'gf-region gf-' + name;
    d.style.cssText = `left:${r.x}px;top:${r.y}px;width:${r.w}px;height:${r.h}px`;
    const lbl = document.createElement('div'); lbl.className = 'gf-label'; lbl.textContent = name;
    d.appendChild(lbl);
    stage.appendChild(d);
  }

  if (!state.components.length) return;
  const W = Number($('#canvas-w').value) || 520;
  const H = Number($('#canvas-h').value) || 340;
  const mount = anchor === 'viewport' ? regions.viewport : anchor === 'chat' ? regions.chat : { x: 0, y: 0, w: dims.w, h: dims.h };
  const holder = document.createElement('div');
  holder.className = 'gf-iface-mount';
  // centre the interface within the mount region
  const mx = mount.x + Math.max(0, Math.floor((mount.w - W) / 2));
  const my = mount.y + Math.max(0, Math.floor((mount.h - H) / 2));
  holder.style.cssText = `left:${mx}px;top:${my}px;width:${W}px;height:${H}px`;
  const layout = computeLayout(W, H);
  for (const c of state.components) {
    const box = layout.get(c.id);
    if (!box) continue;
    holder.appendChild(renderComponentEl(c, box, false));
  }
  stage.appendChild(holder);
}

// ----------------------------------------------------------------------------
// Save / create / copy interface
// ----------------------------------------------------------------------------
async function saveInterface() {
  if (state.archive == null) return toast('No interface loaded', true);
  try {
    const res = await api.post('/api/interface/' + state.archive, { components: state.components });
    state.dirty = false;
    toast(`Saved interface ${state.archive} → cache (rev ${res.revision})`);
    await loadTree();
  } catch (e) { toast(e.message, true); }
}
async function newInterface() {
  const w = Number(prompt('Canvas width', '512')) || 512;
  const h = Number(prompt('Canvas height', '334')) || 334;
  try {
    const res = await api.post('/api/interface/new', { width: w, height: h });
    toast('Created interface ' + res.archive);
    await loadTree();
    await selectInterface(res.archive);
  } catch (e) { toast(e.message, true); }
}
async function copyInterface() {
  if (state.archive == null) return toast('Select an interface to copy first', true);
  const toStr = prompt('Copy interface ' + state.archive + ' to new id (blank = next free id):', '');
  if (toStr === null) return;
  try {
    const body = { from: state.archive };
    if (toStr.trim()) body.to = Number(toStr.trim());
    const res = await api.post('/api/interface/copy', body);
    toast(`Copied ${res.from} → ${res.to}`);
    await loadTree();
    await selectInterface(res.to);
  } catch (e) { toast(e.message, true); }
}
async function addComponent() {
  if (state.archive == null) return toast('Load an interface first', true);
  const type = Number(prompt('Component type (0 Layer, 3 Rect, 4 Text, 5 Graphic, 6 Model):', '4'));
  const root = state.components.find((c) => c.parentId === -1) || state.components[0];
  try {
    const res = await api.post(`/api/interface/${state.archive}/component`, {
      type: Number.isFinite(type) ? type : 4,
      parentId: root ? root.id : (state.archive << 16),
    });
    toast('Added component ' + res.fileId);
    await selectInterface(state.archive);
    selectComponent(res.fileId);
  } catch (e) { toast(e.message, true); }
}

// ----------------------------------------------------------------------------
// misc
// ----------------------------------------------------------------------------
function markDirty() { state.dirty = true; }
function switchTab(name) {
  $$('.tab').forEach((t) => t.classList.toggle('active', t.dataset.tab === name));
  $$('.tabpanel').forEach((p) => p.classList.toggle('active', p.id === 'tab-' + name));
  if (name === 'gameframe') renderGameframe();
  if (name === 'render') renderStage();
}
function escapeHtml(s) { return String(s).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c])); }
let toastTimer;
function toast(msg, err) {
  const t = $('#toast'); t.textContent = msg; t.className = 'toast show' + (err ? ' err' : '');
  clearTimeout(toastTimer); toastTimer = setTimeout(() => t.className = 'toast', 2600);
}

// ----------------------------------------------------------------------------
// wire up
// ----------------------------------------------------------------------------
$('#open-btn').onclick = openCache;
$('#cache-path').onkeydown = (e) => { if (e.key === 'Enter') openCache(); };
$('#btn-reopen').onclick = () => { $('#open-overlay').classList.remove('hidden'); };
$('#btn-save').onclick = saveInterface;
$('#btn-new').onclick = newInterface;
$('#btn-copy').onclick = copyInterface;
$('#add-component').onclick = addComponent;
$('#iface-search').oninput = renderTree;
$('#script-search').oninput = renderScriptList;
$('#new-script').onclick = async () => {
  try { const res = await api.post('/api/script/new', {}); toast('Created script ' + res.id); await loadScriptList(); loadScript(res.id); }
  catch (e) { toast(e.message, true); }
};
$('#save-script').onclick = saveScript;
$$('.tab').forEach((t) => t.onclick = () => switchTab(t.dataset.tab));
['#canvas-w', '#canvas-h', '#zoom', '#show-bounds'].forEach((s) => $(s).oninput = renderStage);
['#frame-mode', '#frame-anchor'].forEach((s) => $(s).onchange = renderGameframe);
window.addEventListener('beforeunload', (e) => { if (state.dirty) { e.preventDefault(); e.returnValue = ''; } });

loadOpcodes();
init();
