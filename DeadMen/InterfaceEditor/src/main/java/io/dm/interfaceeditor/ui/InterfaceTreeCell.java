package io.dm.interfaceeditor.ui;

import javafx.scene.control.TreeCell;
import net.runelite.cache.definitions.InterfaceDefinition;

public class InterfaceTreeCell extends TreeCell<Object>
{
	@Override
	protected void updateItem(Object item, boolean empty)
	{
		super.updateItem(item, empty);

		if (empty || item == null)
		{
			setText(null);
			return;
		}

		if (item instanceof InterfaceTreeView.GroupNode)
		{
			InterfaceTreeView.GroupNode node = (InterfaceTreeView.GroupNode) item;
			setText("Interface " + node.groupId);
		}
		else if (item instanceof InterfaceDefinition)
		{
			InterfaceDefinition def = (InterfaceDefinition) item;
			int childId = def.id & 0xffff;
			String label = childId + ": " + InterfaceTypeNames.of(def.type);
			if (def.name != null && !def.name.isEmpty())
			{
				label += " (" + def.name + ")";
			}
			else if (def.text != null && !def.text.isEmpty())
			{
				String text = def.text.length() > 24 ? def.text.substring(0, 24) + "..." : def.text;
				label += " \"" + text + "\"";
			}
			setText(label);
		}
		else
		{
			setText(String.valueOf(item));
		}
	}
}
