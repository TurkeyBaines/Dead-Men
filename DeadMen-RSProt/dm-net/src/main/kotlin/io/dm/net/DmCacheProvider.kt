package io.dm.net

import io.netty.buffer.ByteBuf
import net.rsprot.compression.HuffmanCodec

/**
 * Seam between RSProt's networking and Dead-Men's cache/filestore (`:DMFS` / `:kronos-api`
 * filestore). RSProt needs two things out of the cache:
 *
 *  1. Raw JS5 group bytes, to serve the client's on-demand cache requests, and
 *  2. The Huffman table (cache archive 10, group "huffman"), used to (de)compress chat.
 *
 * `:DMS` implements this against the existing `io.dm.api.filestore` reader. Keeping it as
 * an interface means `:dm-net` never has to depend on the concrete cache code.
 */
interface DmCacheProvider {

    /**
     * Returns the raw, already-compressed JS5 group container for [archive]/[group],
     * *including* the 2-byte version trailer the client expects. Backs
     * [net.rsprot.protocol.api.js5.Js5GroupProvider.provide].
     */
    fun js5Group(archive: Int, group: Int): ByteBuf

    /** The Huffman codec built from the cache's huffman table (archive 10, "huffman"). */
    fun huffmanCodec(): HuffmanCodec
}
