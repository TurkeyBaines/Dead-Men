// OSRS file store: reads/writes main_file_cache.dat2 + .idxN, ported from
// net.runelite.cache.fs.* (DiskStorage, DataFile, IndexFile, Container,
// IndexData, ArchiveFiles). The data file is append-only on write, matching
// the reference implementation.
import fs from 'node:fs';
import path from 'node:path';
import { InStream, OutStream } from './io.js';
import { decompress, compress, COMPRESSION } from './compression.js';
import { crc32 } from './crc32.js';

const SECTOR_SIZE = 520;
const INDEX_ENTRY_LEN = 6;
const IDX255 = 255;

// ---- raw .dat2 sector read/write ----
class DataFile {
  constructor(file) { this.fd = fs.openSync(file, 'r+'); }
  close() { fs.closeSync(this.fd); }
  size() { return fs.fstatSync(this.fd).size; }

  read(indexId, archiveId, sector, size) {
    if (sector <= 0 || Math.floor(this.size() / SECTOR_SIZE) < sector) return null;
    const out = Buffer.alloc(size);
    let read = 0, part = 0;
    const big = archiveId > 0xFFFF;
    const headerSize = big ? 10 : 8;
    const sectorBuf = Buffer.alloc(SECTOR_SIZE);
    while (size > read) {
      if (sector === 0) return null;
      let dataBlockSize = size - read;
      if (dataBlockSize > SECTOR_SIZE - headerSize) dataBlockSize = SECTOR_SIZE - headerSize;
      const want = headerSize + dataBlockSize;
      const got = fs.readSync(this.fd, sectorBuf, 0, want, SECTOR_SIZE * sector);
      if (got !== want) return null;
      let curArchive, curPart, nextSector, curIndex;
      if (big) {
        curArchive = (sectorBuf[0] << 24) | (sectorBuf[1] << 16) | (sectorBuf[2] << 8) | sectorBuf[3];
        curPart = (sectorBuf[4] << 8) | sectorBuf[5];
        nextSector = (sectorBuf[6] << 16) | (sectorBuf[7] << 8) | sectorBuf[8];
        curIndex = sectorBuf[9];
      } else {
        curArchive = (sectorBuf[0] << 8) | sectorBuf[1];
        curPart = (sectorBuf[2] << 8) | sectorBuf[3];
        nextSector = (sectorBuf[4] << 16) | (sectorBuf[5] << 8) | sectorBuf[6];
        curIndex = sectorBuf[7];
      }
      if (archiveId !== curArchive || curPart !== part || indexId !== curIndex) {
        throw new Error(`data mismatch ${archiveId}!=${curArchive} ${part}!=${curPart} ${indexId}!=${curIndex}`);
      }
      sectorBuf.copy(out, read, headerSize, headerSize + dataBlockSize);
      read += dataBlockSize;
      part++;
      sector = nextSector;
    }
    return out;
  }

  // Appends sectors to the end of the file. Returns start sector.
  write(indexId, archiveId, compressedData) {
    const writeBuf = Buffer.alloc(SECTOR_SIZE);
    let sector = Math.floor((this.size() + SECTOR_SIZE - 1) / SECTOR_SIZE);
    if (sector === 0) sector = 1;
    const startSector = sector;
    const big = archiveId > 0xFFFF;
    let pos = 0, part = 0;
    while (pos < compressedData.length) {
      let nextSector = sector + 1;
      writeBuf.fill(0);
      let dataToWrite;
      if (big) {
        if (compressedData.length - pos <= 510) nextSector = 0;
        writeBuf[0] = archiveId >>> 24; writeBuf[1] = archiveId >>> 16;
        writeBuf[2] = archiveId >>> 8; writeBuf[3] = archiveId;
        writeBuf[4] = part >>> 8; writeBuf[5] = part;
        writeBuf[6] = nextSector >>> 16; writeBuf[7] = nextSector >>> 8; writeBuf[8] = nextSector;
        writeBuf[9] = indexId;
        dataToWrite = Math.min(compressedData.length - pos, 510);
        compressedData.copy(writeBuf, 10, pos, pos + dataToWrite);
        fs.writeSync(this.fd, writeBuf, 0, 10 + dataToWrite, SECTOR_SIZE * sector);
      } else {
        if (compressedData.length - pos <= 512) nextSector = 0;
        writeBuf[0] = archiveId >>> 8; writeBuf[1] = archiveId;
        writeBuf[2] = part >>> 8; writeBuf[3] = part;
        writeBuf[4] = nextSector >>> 16; writeBuf[5] = nextSector >>> 8; writeBuf[6] = nextSector;
        writeBuf[7] = indexId;
        dataToWrite = Math.min(compressedData.length - pos, 512);
        compressedData.copy(writeBuf, 8, pos, pos + dataToWrite);
        fs.writeSync(this.fd, writeBuf, 0, 8 + dataToWrite, SECTOR_SIZE * sector);
      }
      pos += dataToWrite;
      part++;
      sector = nextSector;
    }
    return { sector: startSector, compressedLength: compressedData.length };
  }
}

// ---- .idxN entry table ----
class IndexFile {
  constructor(id, file) {
    this.id = id;
    this.file = file;
    this.fd = fs.openSync(file, fs.existsSync(file) ? 'r+' : 'w+');
  }
  close() { fs.closeSync(this.fd); }
  count() { return Math.floor(fs.fstatSync(this.fd).size / INDEX_ENTRY_LEN); }

  read(id) {
    const buf = Buffer.alloc(INDEX_ENTRY_LEN);
    const got = fs.readSync(this.fd, buf, 0, INDEX_ENTRY_LEN, id * INDEX_ENTRY_LEN);
    if (got !== INDEX_ENTRY_LEN) return null;
    const length = (buf[0] << 16) | (buf[1] << 8) | buf[2];
    const sector = (buf[3] << 16) | (buf[4] << 8) | buf[5];
    if (length <= 0 || sector <= 0) return null;
    return { id, sector, length };
  }

  write(id, sector, length) {
    const buf = Buffer.alloc(INDEX_ENTRY_LEN);
    buf[0] = length >>> 16; buf[1] = length >>> 8; buf[2] = length;
    buf[3] = sector >>> 16; buf[4] = sector >>> 8; buf[5] = sector;
    fs.writeSync(this.fd, buf, 0, INDEX_ENTRY_LEN, id * INDEX_ENTRY_LEN);
  }
}

// ---- reference table (index 255 contents) ----
export function decodeReferenceTable(data) {
  const s = new InStream(data);
  const protocol = s.readUnsignedByte();
  if (protocol < 5 || protocol > 7) throw new Error('Unsupported ref table protocol ' + protocol);
  const revision = protocol >= 6 ? s.readInt() : 0;
  const hash = s.readUnsignedByte();
  const named = (hash & 1) !== 0;
  const count = protocol >= 7 ? s.readBigSmart() : s.readUnsignedShort();
  const archives = [];
  let last = 0;
  for (let i = 0; i < count; i++) {
    last += protocol >= 7 ? s.readBigSmart() : s.readUnsignedShort();
    archives.push({ id: last, nameHash: 0, crc: 0, revision: 0, files: [] });
  }
  if (named) for (let i = 0; i < count; i++) archives[i].nameHash = s.readInt();
  for (let i = 0; i < count; i++) archives[i].crc = s.readInt();
  for (let i = 0; i < count; i++) archives[i].revision = s.readInt();
  const numFiles = [];
  for (let i = 0; i < count; i++) numFiles.push(protocol >= 7 ? s.readBigSmart() : s.readUnsignedShort());
  for (let i = 0; i < count; i++) {
    let lastFile = 0;
    for (let j = 0; j < numFiles[i]; j++) {
      lastFile += protocol >= 7 ? s.readBigSmart() : s.readUnsignedShort();
      archives[i].files.push({ id: lastFile, nameHash: 0 });
    }
  }
  if (named) for (let i = 0; i < count; i++) for (let j = 0; j < numFiles[i]; j++) archives[i].files[j].nameHash = s.readInt();
  return { protocol, revision, named, archives };
}

export function encodeReferenceTable(rt) {
  const { protocol, revision, named, archives } = rt;
  const out = new OutStream();
  out.writeByte(protocol);
  if (protocol >= 6) out.writeInt(revision);
  out.writeByte(named ? 1 : 0);
  const writeId = protocol >= 7 ? (v) => out.writeBigSmart(v) : (v) => out.writeShort(v);
  if (protocol >= 7) out.writeBigSmart(archives.length); else out.writeShort(archives.length);
  for (let i = 0; i < archives.length; i++) writeId(archives[i].id - (i ? archives[i - 1].id : 0));
  if (named) for (const a of archives) out.writeInt(a.nameHash);
  for (const a of archives) out.writeInt(a.crc);
  for (const a of archives) out.writeInt(a.revision);
  for (const a of archives) (protocol >= 7 ? out.writeBigSmart(a.files.length) : out.writeShort(a.files.length));
  for (const a of archives) for (let j = 0; j < a.files.length; j++) writeId(a.files[j].id - (j ? a.files[j - 1].id : 0));
  if (named) for (const a of archives) for (const f of a.files) out.writeInt(f.nameHash);
  return out.flip();
}

// ---- archive multi-file split/join (single chunk) ----
export function splitArchiveFiles(data, fileEntries) {
  if (fileEntries.length === 1) return { [fileEntries[0].id]: data };
  const n = fileEntries.length;
  const s = new InStream(data);
  s.setOffset(s.length - 1);
  const chunks = s.readUnsignedByte();
  s.setOffset(s.length - 1 - chunks * n * 4);
  const sizes = Array.from({ length: n }, () => 0);
  const chunkSizes = Array.from({ length: n }, () => new Array(chunks).fill(0));
  for (let c = 0; c < chunks; c++) {
    let cs = 0;
    for (let i = 0; i < n; i++) {
      cs += s.readInt();
      chunkSizes[i][c] = cs;
      sizes[i] += cs;
    }
  }
  const contents = sizes.map((sz) => Buffer.alloc(sz));
  const offsets = new Array(n).fill(0);
  s.setOffset(0);
  for (let c = 0; c < chunks; c++) {
    for (let i = 0; i < n; i++) {
      const cs = chunkSizes[i][c];
      s.readBytes(contents[i], offsets[i], cs);
      offsets[i] += cs;
    }
  }
  const result = {};
  fileEntries.forEach((fe, i) => { result[fe.id] = contents[i]; });
  return result;
}

export function joinArchiveFiles(fileEntries, contentsById) {
  const ordered = fileEntries.map((fe) => contentsById[fe.id] ?? Buffer.alloc(0));
  if (ordered.length === 1) return Buffer.from(ordered[0]);
  const out = new OutStream();
  for (const c of ordered) out.writeBytes(c);
  let prev = 0;
  for (const c of ordered) { out.writeInt(c.length - prev); prev = c.length; }
  out.writeByte(1); // one chunk
  return out.flip();
}

// ---- high-level store ----
export class CacheStore {
  constructor(folder) {
    this.folder = folder;
    const datPath = path.join(folder, 'main_file_cache.dat2');
    if (!fs.existsSync(datPath)) throw new Error('main_file_cache.dat2 not found in ' + folder);
    this.data = new DataFile(datPath);
    this.idx255 = new IndexFile(IDX255, path.join(folder, 'main_file_cache.idx255'));
    this.indexFiles = new Map();
    this.refTables = new Map(); // indexId -> decoded ref table (cached)
  }

  close() {
    this.data.close();
    this.idx255.close();
    for (const f of this.indexFiles.values()) f.close();
  }

  getIndexFile(id) {
    if (!this.indexFiles.has(id)) {
      this.indexFiles.set(id, new IndexFile(id, path.join(this.folder, 'main_file_cache.idx' + id)));
    }
    return this.indexFiles.get(id);
  }

  listIndexes() {
    const ids = [];
    for (let i = 0; i < this.idx255.count(); i++) {
      if (this.idx255.read(i)) ids.push(i);
    }
    return ids;
  }

  getReferenceTable(indexId) {
    if (this.refTables.has(indexId)) return this.refTables.get(indexId);
    const entry = this.idx255.read(indexId);
    if (!entry) throw new Error('No reference table for index ' + indexId);
    const raw = this.data.read(IDX255, indexId, entry.sector, entry.length);
    const container = decompress(raw, null);
    const rt = decodeReferenceTable(container.data);
    rt._compression = container.compression;
    this.refTables.set(indexId, rt);
    return rt;
  }

  archiveMeta(indexId, archiveId) {
    const rt = this.getReferenceTable(indexId);
    return rt.archives.find((a) => a.id === archiveId) || null;
  }

  // Returns { [fileId]: Buffer } of decompressed/split files for an archive.
  readArchiveFiles(indexId, archiveId) {
    const meta = this.archiveMeta(indexId, archiveId);
    if (!meta) throw new Error(`archive ${indexId}/${archiveId} not in reference table`);
    const idxFile = this.getIndexFile(indexId);
    const entry = idxFile.read(archiveId);
    if (!entry) throw new Error(`archive ${indexId}/${archiveId} missing in idx`);
    const raw = this.data.read(indexId, archiveId, entry.sector, entry.length);
    const container = decompress(raw, null);
    return splitArchiveFiles(container.data, meta.files);
  }

  // Writes files back into an archive: re-packs, compresses, appends sectors,
  // updates the idx entry + reference table (crc, revision, file list) and the
  // index revision, then rewrites index 255. `fileIds` is the desired ordered
  // file list (defaults to existing meta order).
  writeArchiveFiles(indexId, archiveId, contentsById, fileIds = null) {
    const rt = this.getReferenceTable(indexId);
    let meta = rt.archives.find((a) => a.id === archiveId);
    if (!meta) {
      meta = { id: archiveId, nameHash: -1, crc: 0, revision: 0, files: [] };
      rt.archives.push(meta);
      rt.archives.sort((a, b) => a.id - b.id);
    }
    const ids = (fileIds || Object.keys(contentsById).map(Number)).sort((a, b) => a - b);
    meta.files = ids.map((id) => ({ id, nameHash: -1 }));

    const packed = joinArchiveFiles(meta.files, contentsById);
    const newRevision = (meta.revision || 0) + 1;
    const compression = COMPRESSION.GZ;
    const containerBlob = compress(packed, compression, newRevision);

    // write data + idx
    const res = this.data.write(indexId, archiveId, containerBlob);
    this.getIndexFile(indexId).write(archiveId, res.sector, res.compressedLength);

    // crc excludes the appended 2-byte revision (compression != NONE so +0 here
    // because the revision short is what we exclude). Match Java: crc over
    // [compression(1) + len(4) + compressedSize + (4 if compressed)].
    const compressedSize = containerBlob.readInt32BE(1);
    const crcLen = 1 + 4 + compressedSize + (compression !== COMPRESSION.NONE ? 4 : 0);
    meta.crc = crc32(containerBlob, 0, crcLen);
    meta.revision = newRevision;

    // bump index revision and rewrite reference table (index 255)
    rt.revision = (rt.revision || 0) + 1;
    this.#saveReferenceTable(indexId, rt);
    return { sector: res.sector, length: res.compressedLength, crc: meta.crc, revision: newRevision };
  }

  #saveReferenceTable(indexId, rt) {
    const encoded = encodeReferenceTable(rt);
    const blob = compress(encoded, rt._compression ?? COMPRESSION.GZ, -1); // ref table revision always -1
    const res = this.data.write(IDX255, indexId, blob);
    this.idx255.write(indexId, res.sector, res.compressedLength);
    this.refTables.set(indexId, rt);
  }

  // Allocates the next free archive id in an index (max existing + 1).
  nextArchiveId(indexId) {
    const rt = this.getReferenceTable(indexId);
    return rt.archives.reduce((m, a) => Math.max(m, a.id), -1) + 1;
  }
}
