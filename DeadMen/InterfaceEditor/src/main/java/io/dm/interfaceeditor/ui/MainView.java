package io.dm.interfaceeditor.ui;

import io.dm.interfaceeditor.cache.CacheSession;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Root layout: a tree of interfaces on the left, and a tabbed editor area on the
 * right (Layout / CS2 Scripts / Render - the latter two are added in later stages).
 */
public class MainView extends BorderPane
{
	private final Stage stage;
	private final CacheSession session;
	private final InterfaceTreeView treeView;
	private final InterfacePropertiesPane propertiesPane;

	public MainView(Stage stage, CacheSession session)
	{
		this.stage = stage;
		this.session = session;

		propertiesPane = new InterfacePropertiesPane();
		treeView = new InterfaceTreeView(session, propertiesPane::show);

		TabPane tabs = new TabPane();
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		Tab layoutTab = new Tab("Layout", propertiesPane);
		tabs.getTabs().add(layoutTab);

		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		split.getItems().addAll(treeView, tabs);
		split.setDividerPositions(0.28);

		setCenter(split);
	}
}
