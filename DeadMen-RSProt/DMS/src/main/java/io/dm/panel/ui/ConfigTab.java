package io.dm.panel.ui;

import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.configs.CustomConfig;
import io.dm.deadman.tournament.configs.NormalSolo;
import io.dm.deadman.tournament.configs.QuickSolo;
import io.dm.deadman.tournament.configs.Test;
import io.dm.deadman.tournament.mutators.Mutators;
import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.ServerSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * World & tournament configuration: economy multipliers, global event toggles,
 * tournament config presets, a custom config builder, and season rotation.
 * Everything routes through {@link ActionDispatcher} onto the game thread.
 */
public final class ConfigTab implements PanelTab {

    private final Label code = new Label("Tournament Code: --");
    private final Label current = new Label("Current: --");
    private final Label next = new Label("Next: --");

    @Override
    public String title() {
        return "Config";
    }

    @Override
    public Node build() {
        Label header = new Label("World & Tournament Config");
        header.getStyleClass().add("section-header");

        code.getStyleClass().addAll("config-line", "accent-value");
        current.getStyleClass().add("config-line");
        next.getStyleClass().add("config-line");
        VBox statusBox = new VBox(4, code, current, next);
        statusBox.getStyleClass().add("config-box");

        // --- Economy multipliers ---
        Label ecoHeader = new Label("ECONOMY MULTIPLIERS");
        ecoHeader.getStyleClass().add("subsection-header");
        Spinner<Integer> bmMult = spinner(1, 10, 1);
        Button applyBmMult = btn("Apply", () -> ActionDispatcher.setBloodMoneyMultiplier(bmMult.getValue()));
        Spinner<Integer> baseBm = spinner(0, 100000, 1000, 100);
        Button applyBaseBm = btn("Apply", () -> ActionDispatcher.setBaseBloodMoney(baseBm.getValue()));
        HBox ecoBar = new HBox(10,
                new Label("Blood Money x"), bmMult, applyBmMult,
                sep(),
                new Label("Base BM/kill"), baseBm, applyBaseBm);
        ecoBar.setAlignment(Pos.CENTER_LEFT);

        // --- Global toggles ---
        Label toggleHeader = new Label("GLOBAL TOGGLES");
        toggleHeader.getStyleClass().add("subsection-header");
        FlowPane toggles = new FlowPane(10, 10,
                btn("Double Drops", ActionDispatcher::toggleDoubleDrops),
                btn("Double PKP", ActionDispatcher::toggleDoublePkp),
                btn("Double Slayer", ActionDispatcher::toggleDoubleSlayer),
                btn("Double Pest", ActionDispatcher::toggleDoublePest),
                btn("Weekend XP", ActionDispatcher::toggleWeekendExp));

        // --- Tournament presets ---
        Label presetHeader = new Label("NEXT TOURNAMENT CONFIG");
        presetHeader.getStyleClass().add("subsection-header");
        FlowPane presets = new FlowPane(10, 10,
                btn("Normal Solo", () -> ActionDispatcher.overrideNextConfig(new NormalSolo(), "Panel: Normal Solo")),
                btn("Quick Solo", () -> ActionDispatcher.overrideNextConfig(new QuickSolo(), "Panel: Quick Solo")),
                btn("Test (10m)", () -> ActionDispatcher.overrideNextConfig(new Test(), "Panel: Test")),
                dangerBtn("New Tournament Code", () -> confirm(
                        "Rotate the tournament code? All players are reset to fresh accounts on next login.",
                        ActionDispatcher::newTournamentCode)));

        // --- Custom config builder ---
        Label customHeader = new Label("CUSTOM CONFIG BUILDER");
        customHeader.getStyleClass().add("subsection-header");
        ComboBox<TournamentConfig.Timespan> runtime = new ComboBox<>();
        runtime.getItems().addAll(TournamentConfig.Timespan.values());
        runtime.setValue(TournamentConfig.Timespan.ONE_HOUR);
        runtime.setConverter(convert(t -> t.text));
        ComboBox<TournamentConfig.TeamSize> teamSize = new ComboBox<>();
        teamSize.getItems().addAll(TournamentConfig.TeamSize.values());
        teamSize.setValue(TournamentConfig.TeamSize.SOLO);
        teamSize.setConverter(convert(t -> t.name));
        Spinner<Integer> xp = spinner(1, 100, 1);
        Spinner<Integer> drop = spinner(1, 100, 1);
        Spinner<Integer> pet = spinner(1, 100, 1);
        ComboBox<io.dm.deadman.tournament.mutators.Mutator> mutator = new ComboBox<>();
        mutator.getItems().addAll(
                new io.dm.deadman.tournament.mutators.impl.BloodFrenzyMutator(),
                new io.dm.deadman.tournament.mutators.impl.BountyHunterMutator(),
                new io.dm.deadman.tournament.mutators.impl.FamineMutator(),
                new io.dm.deadman.tournament.mutators.impl.VampiricRitesMutator());
        mutator.setValue(mutator.getItems().get(0));
        mutator.setConverter(convert(m -> m.name()));
        Button applyCustom = btn("Set as Next Config", () ->
                ActionDispatcher.overrideNextConfig(
                        new CustomConfig(runtime.getValue(), teamSize.getValue(),
                                xp.getValue(), drop.getValue(), pet.getValue(),
                                mutator.getValue() == null ? Mutators.getRandom() : mutator.getValue()),
                        "Panel: Custom (" + runtime.getValue().text + ", " + teamSize.getValue().name + ")"));
        applyCustom.getStyleClass().add("primary-button");
        FlowPane customBar = new FlowPane(10, 10,
                new Label("Runtime"), runtime,
                new Label("Team"), teamSize,
                new Label("XP x"), xp,
                new Label("Drop x"), drop,
                new Label("Pet x"), pet,
                new Label("Mutator"), mutator,
                applyCustom);
        customBar.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(12, header, statusBox,
                ecoHeader, ecoBar,
                toggleHeader, toggles,
                presetHeader, presets,
                customHeader, customBar);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("tab-root");
        return root;
    }

    private static Spinner<Integer> spinner(int min, int max, int initial) {
        return spinner(min, max, initial, 1);
    }

    private static Spinner<Integer> spinner(int min, int max, int initial, int step) {
        Spinner<Integer> s = new Spinner<>(min, max, initial, step);
        s.setEditable(true);
        s.setPrefWidth(90);
        s.getStyleClass().add("spinner");
        return s;
    }

    private static Button btn(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("action-button");
        b.setOnAction(e -> action.run());
        return b;
    }

    private static Button dangerBtn(String text, Runnable action) {
        Button b = btn(text, action);
        b.getStyleClass().add("danger-button");
        return b;
    }

    private static Node sep() {
        Label l = new Label("  •  ");
        l.getStyleClass().add("muted-note");
        return l;
    }

    private static <T> StringConverter<T> convert(java.util.function.Function<T, String> fn) {
        return new StringConverter<>() {
            @Override public String toString(T o) { return o == null ? "" : fn.apply(o); }
            @Override public T fromString(String s) { return null; }
        };
    }

    private static void confirm(String message, Runnable onYes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Confirm action");
        alert.showAndWait().filter(bt -> bt == ButtonType.YES).ifPresent(bt -> onYes.run());
    }

    @Override
    public void onSnapshot(ServerSnapshot s) {
        code.setText("Tournament Code: " + s.seasonCode());
        current.setText("Current: " + s.configName());
        next.setText("Next: " + s.nextConfigName());
    }
}
