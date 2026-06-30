// Binary stream readers/writers matching the RuneLite cache io format
// (net.runelite.cache.io.InputStream / OutputStream), including the RS
// cp1252 special-character table used by readString/writeString.

const CHARACTERS = [
  '€', '\0', '‚', 'ƒ', '„', '…', '†', '‡',
  'ˆ', '‰', 'Š', '‹', 'Œ', '\0', 'Ž', '\0',
  '\0', '‘', '’', '“', '”', '•', '–', '—',
  '˜', '™', 'š', '›', 'œ', '\0', 'ž', 'Ÿ',
].map((c) => c.charCodeAt(0));

export class InStream {
  constructor(buf) {
    this.buf = Buffer.from(buf);
    this.offset = 0;
  }
  get length() { return this.buf.length; }
  remaining() { return this.buf.length - this.offset; }
  setOffset(o) { this.offset = o; }
  getOffset() { return this.offset; }

  readByte() { const v = this.buf.readInt8(this.offset); this.offset += 1; return v; }
  readUnsignedByte() { const v = this.buf.readUInt8(this.offset); this.offset += 1; return v; }
  peek() { return this.buf.readInt8(this.offset); }

  readShort() { const v = this.buf.readInt16BE(this.offset); this.offset += 2; return v; }
  readUnsignedShort() { const v = this.buf.readUInt16BE(this.offset); this.offset += 2; return v; }

  read24BitInt() {
    return (this.readUnsignedByte() << 16) | (this.readUnsignedByte() << 8) | this.readUnsignedByte();
  }

  readInt() { const v = this.buf.readInt32BE(this.offset); this.offset += 4; return v; }

  readBigSmart() {
    return this.peek() >= 0
      ? (this.readUnsignedShort() & 0xFFFF)
      : (this.readInt() & 0x7FFFFFFF);
  }

  readBytes(dst, off = 0, len = dst.length) {
    this.buf.copy(dst, off, this.offset, this.offset + len);
    this.offset += len;
  }

  getRemaining() {
    const r = this.buf.subarray(this.offset);
    this.offset = this.buf.length;
    return r;
  }

  readString() {
    let s = '';
    for (;;) {
      let ch = this.readUnsignedByte();
      if (ch === 0) break;
      if (ch >= 128 && ch < 160) {
        let mapped = CHARACTERS[ch - 128];
        if (mapped === 0) mapped = 63; // '?'
        ch = mapped;
      }
      s += String.fromCharCode(ch);
    }
    return s;
  }

  readStringOrNull() {
    if (this.peek() !== 0) return this.readString();
    this.readByte();
    return null;
  }
}

export class OutStream {
  constructor() {
    this.chunks = [];
    this._len = 0;
  }
  get length() { return this._len; }
  getOffset() { return this._len; }

  _push(buf) { this.chunks.push(buf); this._len += buf.length; }

  writeByte(v) { const b = Buffer.alloc(1); b.writeUInt8(v & 0xFF, 0); this._push(b); }
  writeShort(v) { const b = Buffer.alloc(2); b.writeUInt16BE(v & 0xFFFF, 0); this._push(b); }
  write24BitInt(v) {
    this.writeByte((v >> 16) & 0xFF);
    this.writeByte((v >> 8) & 0xFF);
    this.writeByte(v & 0xFF);
  }
  writeInt(v) { const b = Buffer.alloc(4); b.writeInt32BE(v | 0, 0); this._push(b); }

  writeBigSmart(v) {
    if (v >= 32768) this.writeInt((1 << 31) | v);
    else this.writeShort(v);
  }

  writeBytes(buf) { this._push(Buffer.from(buf)); }

  // RS string: ascii/cp1252, null-terminated. Reader only remaps bytes 128-159
  // via the special table; bytes 160-255 are read as raw Latin-1. Mirror that:
  // map the special unicode chars back to 128-159, pass 0-127 and 160-255 as-is.
  writeString(s) {
    if (s == null) s = '';
    for (let i = 0; i < s.length; i++) {
      let code = s.charCodeAt(i);
      if (code > 127) {
        const idx = CHARACTERS.indexOf(code);
        if (idx >= 0) code = idx + 128;
        else if (code > 255) code = 63; // '?'
        // 160-255 written unchanged
      }
      this.writeByte(code);
    }
    this.writeByte(0);
  }

  flip() { return Buffer.concat(this.chunks, this._len); }
}
