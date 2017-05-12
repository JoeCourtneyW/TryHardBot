package discord.rocketleague;

public enum Playlist {
    SOLO("Solo Duel", 10),
    DOUBLES("Doubles", 11),
    SOLO_STANDARD("Solo Standard", 13),
    STANDARD("Standard", 0);

    private String display;
    private int rtn_id;

    Playlist(String display, int rtn_id) {
        this.display = display;
        this.rtn_id = rtn_id;
    }

    public String getDisplay() {
        return display;
    }

    public int getRtn_id() {
        return rtn_id;
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
    public static Playlist fromUser(String user_input){
        String i = user_input.toLowerCase();
        if(i.equalsIgnoreCase("1v1") || i.equalsIgnoreCase("solo") || i.contains("duel") || i.contains("1"))
            return Playlist.SOLO;
        else if(i.equalsIgnoreCase("2v2") || i.equalsIgnoreCase("doubles") || i.equalsIgnoreCase("double") || i.contains("2"))
            return Playlist.DOUBLES;
        else if(i.equalsIgnoreCase("3v3") || i.equalsIgnoreCase("standard") || i.equalsIgnoreCase("standards") || i.contains("3"))
            return Playlist.STANDARD;
        else if(i.equalsIgnoreCase("solostandard") || i.contains("solo3"))
            return Playlist.SOLO_STANDARD;
        return null;
    }
}
