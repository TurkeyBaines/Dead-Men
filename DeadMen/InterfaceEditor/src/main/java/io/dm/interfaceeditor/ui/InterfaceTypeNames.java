package io.dm.interfaceeditor.ui;

/**
 * Human-readable names for InterfaceDefinition.type (IF3 widget types).
 */
public final class InterfaceTypeNames
{
	private static final String[] NAMES = {
		"Layer", "Unused", "Rectangle", "Text", "Sprite", "Model",
		"InventoryGrid", "RectangleText", "Line"
	};

	private InterfaceTypeNames()
	{
	}

	public static String of(int type)
	{
		if (type >= 0 && type < NAMES.length)
		{
			return NAMES[type];
		}
		return "Type" + type;
	}
}
