package io.dm.api.filestore;

import io.dm.api.buffer.InBuffer;

import java.util.zip.CRC32;

public class ReferenceTable {

    public final int crc;
    public final int revision;

    public final int archiveCount;
    public final int[] archiveIds;
    public final Archive[] archives;

    // Sum of per-group uncompressed lengths (only populated when the ref table carries the
    // LENGTHS flag, 0x4). Used to build the modern JS5 master index (255/255).
    public int totalUncompressedLength = 0;

    public ReferenceTable(byte[] archiveData) {

        CRC32 crc32 = new CRC32();
        crc32.update(archiveData);
        this.crc = (int) crc32.getValue();

        InBuffer in = new InBuffer(Archive.decompress(archiveData, null));

        int protocol = in.readUnsignedByte();
        if(protocol < 5 || protocol > 7)
            throw new RuntimeException("Invalid Protocol!");
        revision = protocol >= 6 ? in.readInt() : 0;

        int hash = in.readUnsignedByte();
        boolean named = (0x1 & hash) != 0;

        this.archiveCount = protocol >= 7 ? in.readBigSmart() : in.readUnsignedShort();
        this.archiveIds = new int[archiveCount];

        int lastArchiveId = 0;
        int highestArchiveId = -1;
        for(int index = 0; index < archiveCount; index++) {
            int archiveId = lastArchiveId += protocol >= 7 ? in.readBigSmart() : in.readUnsignedShort();
            if(archiveId > highestArchiveId)
                highestArchiveId = archiveId;
            archiveIds[index] = archiveId;
        }
        archives = new Archive[highestArchiveId + 1];
        for(int index = 0; index < archiveCount; index++) {
            int archiveId = archiveIds[index];
            archives[archiveId] = new Archive();
        }
        if(named) {
            for(int index = 0; index < archiveCount; index++) {
                int archiveId = archiveIds[index];
                archives[archiveId].nameHash = in.readInt();
            }
        }
        for(int index = 0; index < archiveCount; index++) {
            int archiveId = archiveIds[index];
            archives[archiveId].crc = in.readInt();
        }
        // Modern JS5 reference-table flags (OSRS 2xx caches, e.g. 236) add per-archive fields
        // between the crc and revision tables. Legacy caches leave these bits clear, so these
        // loops are no-ops there. Without them the parser misaligns and reads a garbage
        // fileCount below -> OutOfMemoryError.
        if((hash & 0x8) != 0) { // uncompressed checksums
            for(int index = 0; index < archiveCount; index++)
                in.readInt();
        }
        if((hash & 0x2) != 0) { // whirlpool digests (64 bytes per archive)
            for(int index = 0; index < archiveCount; index++)
                for(int b = 0; b < 64; b++)
                    in.readUnsignedByte();
        }
        if((hash & 0x4) != 0) { // compressed + uncompressed lengths
            for(int index = 0; index < archiveCount; index++) {
                in.readInt();                                 // compressed length (unused)
                totalUncompressedLength += in.readInt();      // uncompressed length
            }
        }
        for(int index = 0; index < archiveCount; index++) {
            int archiveId = archiveIds[index];
            archives[archiveId].revision = in.readInt();
        }
        for(int index = 0; index < archiveCount; index++) {
            int archiveId = archiveIds[index];
            int fileCount = protocol >= 7 ? in.readBigSmart() : in.readUnsignedShort();
            archives[archiveId].fileCount = fileCount;
            archives[archiveId].fileIds = new int[fileCount];
        }
        for(int index = 0; index < archiveCount; index++) {
            int archiveId = archiveIds[index];
            Archive archive = archives[archiveId];

            int lastFileId = 0;
            int highestFileId = -1;
            for(int i = 0; i < archive.fileCount; i++) {
                int fileId = lastFileId += protocol >= 7 ? in.readBigSmart() : in.readUnsignedShort();
                if(fileId > highestFileId)
                    highestFileId = fileId;
                archive.fileIds[i] = fileId;
            }
            archive.files = new Archive.File[highestFileId + 1];
            for(int i = 0; i < archive.fileCount; i++) {
                int fileId = archive.fileIds[i];
                archive.files[fileId] = new Archive.File();
            }
        }
        if(named) {
            for(int index = 0; index < archiveCount; index++) {
                int archiveId = archiveIds[index];
                Archive archive = archives[archiveId];
                boolean b = archive.files.length == archive.fileCount;
                for(int i = 0; i < archive.fileCount; i++) {
                    int fileId;
                    if(!b) {
                        fileId = archive.fileIds[i];
                    } else {
                        //System.out.println("@@" + archiveId + ": " + i + "," + archive.fileIds[i]);
                        fileId = i;
                    }
                    archive.files[fileId].nameHash = in.readInt();
                }
            }
        }
    }

}
