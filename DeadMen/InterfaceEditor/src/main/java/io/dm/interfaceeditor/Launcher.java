package io.dm.interfaceeditor;

/**
 * Entry point. Indirection avoids JavaFX module-path issues when run from a fat jar.
 */
public class Launcher
{
	public static void main(String[] args)
	{
		InterfaceEditorApp.main(args);
	}
}
