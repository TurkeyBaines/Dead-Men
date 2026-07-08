package io.dm.net

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.api.js5.Js5Service

/**
 * A [DmCacheProvider] backed by two thin suppliers over Dead-Men's existing filestore
 * (`io.dm.api.filestore.FileStore`). This keeps the RSProt-specific byte-format details
 * (container stripping, Huffman construction) inside the compiled `:dm-net` module, so
 * `:DMS` only has to hand over raw cache bytes.
 *
 * @param rawGroup returns the group container **exactly as stored in the cache** —
 *   i.e. `FileStore.get(index).getArchiveData(group)`: `[compression:1][length:4][payload][version:2?]`.
 *   For the master index request `(255, 255)` return the freshly-built
 *   `[0][length:4][payload]` (crc+revision table), same as the legacy `CacheManager` 255/255 branch.
 * @param huffmanTable returns the **decompressed** Huffman table (cache archive 10, group "huffman").
 */
class FilestoreCacheProvider(
    private val rawGroup: (archive: Int, group: Int) -> ByteArray,
    private val huffmanTable: () -> ByteArray,
) : DmCacheProvider {

    // rawGroup returns the trimmed cache container [compression][length][data]; RSProt's
    // prepareJs5Buffer adds the [archive][group] header and inserts the 0xFF block separators
    // at the exact offsets it expects (3-byte header + 509-byte first block, then 0xFF + 511).
    override fun js5Group(archive: Int, group: Int): ByteBuf {
        val input = Unpooled.wrappedBuffer(rawGroup(archive, group))
        val output = Unpooled.buffer(input.readableBytes() + 16)
        Js5Service.prepareJs5Buffer(archive, group, input, output)
        // RSProt serves the group in 512-byte blocks, taking a non-retained slice() per block;
        // the pipeline releases each slice, decrementing this shared parent. Pre-retain one ref
        // per block so it survives until the whole group has been served.
        val blocks = (output.readableBytes() + BLOCK_SIZE - 1) / BLOCK_SIZE
        if (blocks > 1) output.retain(blocks - 1)
        return output
    }

    private companion object {
        private const val BLOCK_SIZE = 512
    }

    override fun huffmanCodec(): HuffmanCodec =
        HuffmanCodec.create(Unpooled.wrappedBuffer(huffmanTable()))

}
