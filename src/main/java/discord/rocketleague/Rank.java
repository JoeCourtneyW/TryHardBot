package discord.rocketleague;

import discord.utils.MessageUtils;
import sun.net.www.content.image.png;

public enum Rank {
    UNRANKED(0, "<:Unranked:304680293200297985>"),
    BRONZE_I(1, "<:Bronze1:303016636045066250>"),
    BRONZE_II(2, "<:Bronze2:303016625148264448>"),
    BRONZE_III(3, "<:Bronze3:303016613152555008>"),
    SILVER_I(4, "<:Silver1:303016599038722048>"),
    SILVER_II(5, "<:Silver2:303016588360155137>"),
    SILVER_III(6, "<:Silver3:303016577803091968>"),
    GOLD_I(7, "<:Gold1:303016563466960906>"),
    GOLD_II(8, "<:Gold2:303016548254220288>"),
    GOLD_III(9, "<:Gold3:303016537151897601>"),
    PLATINUM_I(10, "<:Platinum1:303016525496057858>"),
    PLATINUM_II(11, "<:Platinum2:303016513990950913>"),
    PLATINUM_III(12, "<:Platinum3:303016502745890827>"),
    DIAMOND_I(13, "<:Diamond1:303016490523688960>"),
    DIAMOND_II(14, "<:Diamond2:303016478158880768>"),
    DIAMOND_III(15, "<:Diamond3:303016461369212929>"),
    CHAMPION_I(16, "<:Champion1:303016385158709248>"),
    CHAMPION_II(17, "<:Champion2:303016372508688384>"),
    CHAMPION_III(18, "<:Champion3:303016354502410248>"),
    GRAND_CHAMPION(19, "<:GrandChampion:303016340975910923>");
    public int val;
    String emojiID;


    Rank(int val, String emoji) {
        this.emojiID = emoji;
        this.val = val;
    }

    public static Rank fromVal(int val) {
        for (Rank r : values()) {
            if (r.val == val) {
                return r;
            }
        }
        return UNRANKED;
    }

    public static Rank getRankFromString(String r) {
        return valueOf(r.toUpperCase().replaceAll(" ", "_"));
    }

    public String getString() {
        if (this == UNRANKED)
            return "Unranked";
        String name = name().split("_")[0];
        String num = name().split("_")[1];
        if (num.length() < 4)
            return MessageUtils.initialCap(name) + " " + num.toUpperCase(); //Actual num
        else
            return MessageUtils.initialCap(name) + " " + MessageUtils.initialCap(num); //Grand Champion
    }

    public String getStringNum() {
        if (this == UNRANKED)
            return "Unranked";
        String name = name().split("_")[0];
        String num = name().split("_")[1];
        if (num.length() < 4)
            return MessageUtils.initialCap(name) + " " + num.length(); //Actual num
        else
            return MessageUtils.initialCap(name) + " " + MessageUtils.initialCap(num); //Grand Champion
    }

    public String getStringBroad() {
        if (this == UNRANKED)
            return "Unranked";
        String name = name().split("_")[0];
        String num = name().split("_")[1];
        if (num.length() < 4)
            return MessageUtils.initialCap(name).trim();
        else
            return MessageUtils.initialCap(name).trim() + " " + MessageUtils.initialCap(num).trim(); //Grand Champion
    }

    public String getEmoji() {
        return emojiID;
    }

    public String getImageURL(){
        return "http://rocketleague.tracker.network/Images/RL/ranked/s4-" + val + ".png";
    }
}