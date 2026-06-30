// Sprite codec for index 8, ported from
// net.runelite.cache.definitions.loaders.SpriteLoader. Decodes a sprite group
// into frames with ARGB pixel data; encodePng turns a frame into a PNG buffer.
import zlib from 'node:zlib';
import { InStream } from './io.js';
import { crc32 } from './crc32.js';

const FLAG_VERTICAL = 1;
const FLAG_ALPHA = 2;

export function decodeSprites(id, b) {
  const is = new InStream(b);
  is.setOffset(is.length - 2);
  const spriteCount = is.readUnsignedShort();
  const frames = [];

  is.setOffset(is.length - 7 - spriteCount * 8);
  const maxWidth = is.readUnsignedShort();
  const maxHeight = is.readUnsignedShort();
  const paletteLength = is.readUnsignedByte() + 1;

  for (let i = 0; i < spriteCount; i++) frames.push({ id, frame: i, maxWidth, maxHeight });
  for (let i = 0; i < spriteCount; i++) frames[i].offsetX = is.readUnsignedShort();
  for (let i = 0; i < spriteCount; i++) frames[i].offsetY = is.readUnsignedShort();
  for (let i = 0; i < spriteCount; i++) frames[i].width = is.readUnsignedShort();
  for (let i = 0; i < spriteCount; i++) frames[i].height = is.readUnsignedShort();

  is.setOffset(is.length - 7 - spriteCount * 8 - (paletteLength - 1) * 3);
  const palette = new Array(paletteLength).fill(0);
  for (let i = 1; i < paletteLength; i++) {
    let c = is.read24BitInt();
    if (c === 0) c = 1;
    palette[i] = c;
  }

  is.setOffset(0);
  for (let i = 0; i < spriteCount; i++) {
    const def = frames[i];
    const w = def.width, h = def.height, dim = w * h;
    const idxArr = new Uint8Array(dim);
    const alphas = new Uint8Array(dim);
    const flags = is.readUnsignedByte();

    if ((flags & FLAG_VERTICAL) === 0) {
      for (let j = 0; j < dim; j++) idxArr[j] = is.readUnsignedByte();
    } else {
      for (let x = 0; x < w; x++) for (let y = 0; y < h; y++) idxArr[w * y + x] = is.readUnsignedByte();
    }

    if ((flags & FLAG_ALPHA) !== 0) {
      if ((flags & FLAG_VERTICAL) === 0) {
        for (let j = 0; j < dim; j++) alphas[j] = is.readUnsignedByte();
      } else {
        for (let x = 0; x < w; x++) for (let y = 0; y < h; y++) alphas[w * y + x] = is.readUnsignedByte();
      }
    } else {
      for (let j = 0; j < dim; j++) if (idxArr[j] !== 0) alphas[j] = 0xFF;
    }

    // RGBA bytes for PNG
    const rgba = Buffer.alloc(dim * 4);
    for (let j = 0; j < dim; j++) {
      const rgb = palette[idxArr[j]] || 0;
      rgba[j * 4] = (rgb >> 16) & 0xFF;
      rgba[j * 4 + 1] = (rgb >> 8) & 0xFF;
      rgba[j * 4 + 2] = rgb & 0xFF;
      rgba[j * 4 + 3] = alphas[j];
    }
    def.rgba = rgba;
  }
  return frames;
}

// Minimal PNG encoder (RGBA, no filtering) for a decoded frame.
export function encodePng(width, height, rgba) {
  if (width === 0 || height === 0) {
    // 1x1 transparent placeholder
    width = 1; height = 1; rgba = Buffer.from([0, 0, 0, 0]);
  }
  const raw = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y++) {
    raw[y * (width * 4 + 1)] = 0; // filter type 0
    rgba.copy(raw, y * (width * 4 + 1) + 1, y * width * 4, (y + 1) * width * 4);
  }
  const idat = zlib.deflateSync(raw);

  const chunks = [];
  chunks.push(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]));
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;  // bit depth
  ihdr[9] = 6;  // color type RGBA
  ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
  chunks.push(pngChunk('IHDR', ihdr));
  chunks.push(pngChunk('IDAT', idat));
  chunks.push(pngChunk('IEND', Buffer.alloc(0)));
  return Buffer.concat(chunks);
}

function pngChunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const typeBuf = Buffer.from(type, 'ascii');
  const body = Buffer.concat([typeBuf, data]);
  const crcBuf = Buffer.alloc(4);
  // PNG CRC is the same CRC-32; reuse but as unsigned
  crcBuf.writeInt32BE(crc32(body), 0);
  return Buffer.concat([len, body, crcBuf]);
}
