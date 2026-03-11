package io.dm.deadman.tournament;

public class Tournament {

    public enum StageName {
        LOBBY("Lobby"),
        MAIN("Running"),
        FINAL("Final");

        public final String text;
        StageName(String text) {
            this.text = text;
        }
    }

    public enum Event {
        BREACH
    }
}
