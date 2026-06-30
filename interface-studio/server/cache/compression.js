// Container (de)compression, matching net.runelite.cache.fs.Container.
// Compression types: 0=NONE, 1=BZ2, 2=GZ.
import zlib from 'node:zlib';
import Bzip2 from 'seek-bzip';
import { InStream, OutStream } from './io.js';
import { crc32 } from './crc32.js';

export const COMPRESSION = { NONE: 0, BZ2: 1, GZ: 2 };

const BZIP_HEADER = Buffer.from([0x42, 0x5a, 0x68, 0x31]); // "BZh1"

function gunzip(buf) { return zlib.gunzipSync(buf); }
function gzip(buf) {
  // RS expects a standard gzip stream. mtime/os bytes are zeroed for determinism.
  return zlib.gzipSync(buf, { level: zlib.constants.Z_BEST_COMPRESSION });
}

function bunzip2(buf, decompressedLen) {
  const full = Buffer.concat([BZIP_HEADER, buf]);
  return Buffer.from(Bzip2.decode(full));
}

// Returns { data, compression, revision, crc }
export function decompress(b, keys = null) {
  const stream = new InStream(b);
  const compression = stream.readUnsignedByte();
  const compressedLength = stream.readInt();
  if (compressedLength < 0 || compressedLength > 10_000_000) {
    throw new Error('Invalid container length ' + compressedLength);
  }
  const crc = crc32(b.subarray(0, 5 + (compression === COMPRESSION.NONE ? compressedLength : compressedLength + 4)));

  let data;
  let revision = -1;
  if (compression === COMPRESSION.NONE) {
    const enc = Buffer.alloc(compressedLength);
    stream.readBytes(enc, 0, compressedLength);
    if (stream.remaining() >= 2) revision = stream.readUnsignedShort();
    data = enc;
  } else {
    const enc = Buffer.alloc(compressedLength + 4);
    stream.readBytes(enc, 0, enc.length);
    if (stream.remaining() >= 2) revision = stream.readUnsignedShort();
    const inner = new InStream(enc);
    const decompressedLength = inner.readInt();
    const payload = inner.getRemaining();
    data = compression === COMPRESSION.GZ ? gunzip(payload) : bunzip2(payload, decompressedLength);
    if (data.length !== decompressedLength) {
      throw new Error(`decompressed length mismatch ${data.length} != ${decompressedLength}`);
    }
  }
  return { data, compression, revision, crc };
}

// Builds a container blob. keys not supported (interfaces/scripts are unencrypted).
export function compress(data, compression, revision = -1) {
  data = Buffer.from(data);
  let compressedData;
  let length;
  if (compression === COMPRESSION.NONE) {
    compressedData = data;
    length = compressedData.length;
  } else if (compression === COMPRESSION.GZ) {
    compressedData = Buffer.concat([int32(data.length), gzip(data)]);
    length = compressedData.length - 4;
  } else if (compression === COMPRESSION.BZ2) {
    throw new Error('BZ2 compression on write is not supported; use GZ or NONE');
  } else {
    throw new Error('Unknown compression type ' + compression);
  }

  const out = new OutStream();
  out.writeByte(compression);
  out.writeInt(length);
  out.writeBytes(compressedData);
  if (revision !== -1) out.writeShort(revision);
  return out.flip();
}

function int32(v) {
  const b = Buffer.alloc(4);
  b.writeInt32BE(v | 0, 0);
  return b;
}
