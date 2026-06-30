package io.dm.interfaceeditor.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Pair;
import net.runelite.cache.definitions.InterfaceDefinition;

/**
 * Read-only inspector for the currently selected widget's attributes.
 * Editing is added in a later stage.
 */
public class InterfacePropertiesPane extends BorderPane
{
	private final TableView<Pair<String, String>> table = new TableView<>();
	private final Label header = new Label("Select a widget in the tree to inspect it.");

	public InterfacePropertiesPane()
	{
		TableColumn<Pair<String, String>, String> nameCol = new TableColumn<>("Property");
		nameCol.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(() -> c.getValue().getKey()));
		nameCol.setPrefWidth(220);

		TableColumn<Pair<String, String>, String> valueCol = new TableColumn<>("Value");
		valueCol.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(() -> c.getValue().getValue()));
		valueCol.setPrefWidth(480);

		table.getColumns().addAll(nameCol, valueCol);
		table.setPlaceholder(new Label(""));

		header.setStyle("-fx-padding: 8; -fx-font-weight: bold;");

		setTop(header);
		setCenter(table);
	}

	public void show(InterfaceDefinition def)
	{
		header.setText("Interface " + (def.id >>> 16) + " / Widget " + (def.id & 0xffff)
			+ "  -  " + InterfaceTypeNames.of(def.type));

		ObservableList<Pair<String, String>> rows = FXCollections.observableArrayList();
		rows.add(new Pair<>("id", String.valueOf(def.id)));
		rows.add(new Pair<>("type", InterfaceTypeNames.of(def.type)));
		rows.add(new Pair<>("contentType", String.valueOf(def.contentType)));
		rows.add(new Pair<>("parentId", String.valueOf(def.parentId)));
		rows.add(new Pair<>("position", def.originalX + ", " + def.originalY));
		rows.add(new Pair<>("size", def.originalWidth + " x " + def.originalHeight));
		rows.add(new Pair<>("widthMode/heightMode", def.widthMode + " / " + def.heightMode));
		rows.add(new Pair<>("xPositionMode/yPositionMode", def.xPositionMode + " / " + def.yPositionMode));
		rows.add(new Pair<>("hidden", String.valueOf(def.isHidden)));
		rows.add(new Pair<>("name", String.valueOf(def.name)));
		rows.add(new Pair<>("text", String.valueOf(def.text)));
		rows.add(new Pair<>("alternateText", String.valueOf(def.alternateText)));
		rows.add(new Pair<>("textColor", "0x" + Integer.toHexString(def.textColor)));
		rows.add(new Pair<>("fontId", String.valueOf(def.fontId)));
		rows.add(new Pair<>("spriteId", String.valueOf(def.spriteId)));
		rows.add(new Pair<>("textureId", String.valueOf(def.textureId)));
		rows.add(new Pair<>("modelId", String.valueOf(def.modelId)));
		rows.add(new Pair<>("opacity", String.valueOf(def.opacity)));
		rows.add(new Pair<>("clickMask", String.valueOf(def.clickMask)));
		rows.add(new Pair<>("actions", def.actions == null ? "[]" : java.util.Arrays.toString(def.actions)));
		rows.add(new Pair<>("tooltip", String.valueOf(def.tooltip)));
		rows.add(new Pair<>("scroll size", def.scrollWidth + " x " + def.scrollHeight));
		rows.add(new Pair<>("onLoadListener (CS2)", describeListener(def.onLoadListener)));
		rows.add(new Pair<>("onClickListener (CS2)", describeListener(def.onClickListener)));
		rows.add(new Pair<>("onVarTransmitListener (CS2)", describeListener(def.onVarTransmitListener)));
		rows.add(new Pair<>("clientScripts (IF1 CS)", def.clientScripts == null ? "none"
			: def.clientScripts.length + " script(s)"));

		table.setItems(rows);
	}

	private String describeListener(Object[] listener)
	{
		if (listener == null || listener.length == 0)
		{
			return "none";
		}
		return java.util.Arrays.toString(listener);
	}
}
