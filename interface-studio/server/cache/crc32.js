// Standard CRC-32 (IEEE 802.3 / java.util.zip.CRC32), returned as a signed int
// to match the Java cache library which stores crc in a Java int.
const TABLE = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c;
  }
  return t;
})();

export function crc32(buf, off = 0, len = buf.length - off) {
  let crc = 0xFFFFFFFF;
  for (let i = off; i < off + len; i++) {
    crc = (crc >>> 8) ^ TABLE[(crc ^ buf[i]) & 0xFF];
  }
  crc = (crc ^ 0xFFFFFFFF) | 0; // signed 32-bit, matches Java int
  return crc;
}
