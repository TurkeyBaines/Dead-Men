package io.dm.panel.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A small reusable dashboard "tile": a muted caption over a large accent value.
 * Used across the Dashboard and Economy tabs. Styled via {@code .stat-tile},
 * {@code .stat-tile-title}, {@code .stat-tile-value} in dark-theme.css.
 */
public final class StatTile extends VBox {

    private final Label valueLabel = new Label("--");
    private final Label titleLabel = new Label();

    public StatTile(String title) {
        getStyleClass().add("stat-tile");
        titleLabel.setText(title.toUpperCase());
        titleLabel.getStyleClass().add("stat-tile-title");
        valueLabel.getStyleClass().add("stat-tile-value");
        getChildren().addAll(titleLabel, valueLabel);
    }

    public StatTile(String title, String accentStyleClass) {
        this(title);
        if (accentStyleClass != null) {
            valueLabel.getStyleClass().add(accentStyleClass);
        }
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setValue(long value) {
        valueLabel.setText(Long.toString(value));
    }

    /**
     * Apply a live health color to the value.
     * @param statusClass one of {@code "stat-ok"}, {@code "stat-warn"}, {@code "stat-err"},
     *                    or {@code null} to clear back to the default/accent color.
     */
    public void setStatus(String statusClass) {
        valueLabel.getStyleClass().removeAll("stat-ok", "stat-warn", "stat-err");
        if (statusClass != null) {
            valueLabel.getStyleClass().add(statusClass);
        }
    }
}
