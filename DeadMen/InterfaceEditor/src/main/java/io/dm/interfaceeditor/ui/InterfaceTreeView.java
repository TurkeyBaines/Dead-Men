package io.dm.interfaceeditor.ui;

import io.dm.interfaceeditor.cache.CacheSession;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import net.runelite.cache.definitions.InterfaceDefinition;

/**
 * Shows every interface group as a root node, with its widgets nested underneath
 * by parentId so multi-component interfaces read as one tree instead of a flat
 * list of unrelated widgets.
 */
public class InterfaceTreeView extends TreeView<Object>
{
	public InterfaceTreeView(CacheSession session, Consumer<InterfaceDefinition> onSelect)
	{
		setShowRoot(false);

		TreeItem<Object> root = new TreeItem<>("root");
		InterfaceDefinition[][] groups = session.getInterfaceGroups();

		for (int groupId = 0; groupId < groups.length; groupId++)
		{
			InterfaceDefinition[] group = groups[groupId];
			if (group == null)
			{
				continue;
			}

			TreeItem<Object> groupItem = new TreeItem<>(new GroupNode(groupId));

			Map<Integer, TreeItem<Object>> byChildId = new HashMap<>();
			for (InterfaceDefinition def : group)
			{
				if (def == null)
				{
					continue;
				}
				byChildId.put(def.id & 0xffff, new TreeItem<>(def));
			}

			for (InterfaceDefinition def : group)
			{
				if (def == null)
				{
					continue;
				}

				TreeItem<Object> item = byChildId.get(def.id & 0xffff);
				int parentGroup = def.parentId >>> 16;
				int parentChild = def.parentId & 0xffff;

				TreeItem<Object> parentItem = null;
				if (def.parentId >= 0 && parentGroup == groupId)
				{
					parentItem = byChildId.get(parentChild);
				}

				if (parentItem != null)
				{
					parentItem.getChildren().add(item);
				}
				else
				{
					groupItem.getChildren().add(item);
				}
			}

			if (!groupItem.getChildren().isEmpty())
			{
				root.getChildren().add(groupItem);
			}
		}

		setRoot(root);
		setCellFactory(tv -> new InterfaceTreeCell());

		getSelectionModel().selectedItemProperty().addListener((obs, old, sel) ->
		{
			if (sel != null && sel.getValue() instanceof InterfaceDefinition)
			{
				onSelect.accept((InterfaceDefinition) sel.getValue());
			}
		});
	}

	/**
	 * Marker for a top-level interface group root node (an archive in the cache's
	 * INTERFACES index - what players see as a single "main interface").
	 */
	public static final class GroupNode
	{
		public final int groupId;

		public GroupNode(int groupId)
		{
			this.groupId = groupId;
		}
	}
}
