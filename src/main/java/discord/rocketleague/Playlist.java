package discord.rocketleague;

public enum Playlist {
    SOLO("Solo Duel", "playlist_10"),
    DOUBLES("Doubles", "playlist_11"),
    SOLO_STANDARD("Solo Standard", "playlist_12"),
    STANDARD("Standard", "playlist_13");

    private String display;
    private String div;

    Playlist(String display, String div) {
        this.display = display;
        this.div = div;
    }

    public String getDisplay() {
        return display;
    }

    public String getDiv() {
        return div;
    }

    public static Playlist fromTrackerNetwork(String tn) {
        if (tn.contains("Solo"))
            return SOLO_STANDARD;
        else if (tn.contains("Duel"))
            return SOLO;
        else if (tn.contains("Standard"))
            return STANDARD;
        else if (tn.contains("Doubles"))
            return DOUBLES;
        else
            return null;
    }
}
