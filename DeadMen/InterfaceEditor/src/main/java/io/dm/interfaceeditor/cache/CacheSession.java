package io.dm.interfaceeditor.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.runelite.cache.IndexType;
import net.runelite.cache.InterfaceManager;
import net.runelite.cache.definitions.InterfaceDefinition;
import net.runelite.cache.definitions.ScriptDefinition;
import net.runelite.cache.definitions.loaders.ScriptLoader;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.ArchiveFiles;
import net.runelite.cache.fs.FSFile;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Storage;
import net.runelite.cache.fs.Store;

/**
 * Holds an open cache (source, read-only) and tracks edits that will be written
 * to a separate output directory on save - the source cache is never modified.
 */
public class CacheSession implements AutoCloseable
{
	private final File sourceCacheDir;
	private final Store store;
	private final InterfaceManager interfaceManager;
	private final Map<Integer, ScriptDefinition> scripts = new HashMap<>();

	private CacheSession(File sourceCacheDir, Store store, InterfaceManager interfaceManager)
	{
		this.sourceCacheDir = sourceCacheDir;
		this.store = store;
		this.interfaceManager = interfaceManager;
	}

	public static CacheSession open(File cacheDir) throws IOException
	{
		Store store = new Store(cacheDir);
		store.load();

		InterfaceManager im = new InterfaceManager(store);
		im.load();

		CacheSession session = new CacheSession(cacheDir, store, im);
		session.loadScripts();
		return session;
	}

	private void loadScripts() throws IOException
	{
		ScriptLoader loader = new ScriptLoader();
		Storage storage = store.getStorage();
		Index index = store.getIndex(IndexType.CLIENTSCRIPT);

		if (index == null)
		{
			return;
		}

		for (Archive archive : index.getArchives())
		{
			byte[] data = storage.loadArchive(archive);
			if (data == null)
			{
				continue;
			}
			ArchiveFiles files = archive.getFiles(data);
			for (FSFile file : files.getFiles())
			{
				int id = archive.getArchiveId();
				try
				{
					ScriptDefinition def = loader.load(id, file.getContents());
					scripts.put(id, def);
				}
				catch (Exception ex)
				{
					// not every archive in this index is a valid script in older cache revisions
				}
			}
		}
	}

	public File getSourceCacheDir()
	{
		return sourceCacheDir;
	}

	public InterfaceManager getInterfaceManager()
	{
		return interfaceManager;
	}

	public InterfaceDefinition[][] getInterfaceGroups()
	{
		return interfaceManager.getInterfaces();
	}

	public Map<Integer, ScriptDefinition> getScripts()
	{
		return scripts;
	}

	public ScriptDefinition getScript(int id)
	{
		return scripts.get(id);
	}

	@Override
	public void close() throws IOException
	{
		store.close();
	}
}
