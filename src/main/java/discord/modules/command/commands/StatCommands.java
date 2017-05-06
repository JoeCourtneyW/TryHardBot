package discord.modules.command.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord.Main;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import discord.modules.command.Category;
import discord.modules.command.CommandA;
import discord.modules.database.UserValue;
import discord.utils.MessageUtils;
import org.jsoup.select.Elements;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class StatCommands {

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
        int val;
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
                return MessageUtils.initialCap(name);
            else
                return MessageUtils.initialCap(name) + " " + MessageUtils.initialCap(num); //Grand Champion
        }
        public String getEmoji() {
            return emojiID;
        }
    }

    public enum Region {

        US_EAST("US-East"),
        US_WEST("US-West"),
        EUROPE("Europe"),
        OCEANIA("Oceania"),
        ASIA_EAST("Asia-East"),
        ASIA_SE_MAINLAND("Asia-SE Mainland"),
        SOUTH_AMERICA("South America"),
        MIDDLE_EAST("Middle East");

        String display;

        Region(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    @CommandA(label = "stat", name = "Stat", description = "Find your Rocket League stats", category = Category.GENERAL, usage = ".stat [System] [Username]", alias = "stats")
    public static void statCommand(IMessage im) {
        String[] args = im.getContent().split(" ");
        if (args.length < 3) {
            MessageUtils.sendSyntax("Stat", im.getChannel());
            return;
        }
        String system = args[1].toLowerCase();
        system = formatSystem(system);
        if (system.isEmpty()) //returns emptyset if it isn't proper format
            MessageUtils.sendChannelMessage("You must set your system to either: [PS4, XBOX, or STEAM]", im.getChannel());
        StringBuilder userB = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i != 2)
                userB.append("%20");
            userB.append(args[i]);
        }
        String user = userB.toString().trim();
        user = getUserFromURL(user);
        StringBuilder sb = new StringBuilder();
        HashMap<Playlist, Rank> ranks = getRanksFor(user, system);
        if (ranks == null) {
            MessageUtils.sendChannelMessage("That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
            return;
        }
        sb.append("**");
        sb.append(user);
        sb.append("'s** stats:");
        for (Playlist list : ranks.keySet()) {
            sb.append("**");
            sb.append(list.getDisplay());
            sb.append("**");
            sb.append(": ");
            sb.append(ranks.get(list).getStringNum());
            //sb.append(" "); extra space after stringnum already?
            sb.append(ranks.get(list).getEmoji());
            sb.append("\n");
        }
        if (sb.length() == 0)
            MessageUtils.sendChannelMessage("That user does not have any stats on the specified platform", im.getChannel());
        else
            MessageUtils.sendChannelMessage(sb.toString(), im.getChannel());
    }

    @CommandA(label = "me", name = "Me", description = "Find your stats on your linked account", category = Category.GENERAL, usage = ".me")
    public static void meCommand(IMessage im) {
        String account = UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString();
        if (UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("")) {
            MessageUtils.sendChannelMessage("You must link an account using " + Main.PREFIX + "link before you can use this command!", im.getChannel());
            return;
        }
        String system = UserValue.LINKED_PLATFORM.getFor(im.getAuthor()).asString();
        StringBuilder sb = new StringBuilder();
        HashMap<Playlist, Rank> ranks = getRanksFor(account, system);
        if (ranks == null) {
            MessageUtils.sendChannelMessage("That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
            return;
        }
        sb.append("**");
        sb.append(account);
        sb.append("'s** stats:");
        for (Playlist list : ranks.keySet()) {
            sb.append("**");
            sb.append(list.getDisplay());
            sb.append("**");
            sb.append(": ");
            sb.append(ranks.get(list).getStringNum());
            //sb.append(" "); extra space after stringnum already?
            sb.append(ranks.get(list).getEmoji());
            sb.append("\n");
        }
        if (sb.length() == 0)
            MessageUtils.sendChannelMessage("You have not yet been placed into a rank in Rocket League", im.getChannel());
        else
            MessageUtils.sendChannelMessage(sb.toString(), im.getChannel());

    }

    @CommandA(label = "link", name = "Link", description = "Link your account to your discord", category = Category.GENERAL, usage = ".link [System] [Username]")
    public static void linkCommand(IMessage im) {
        if (!UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("")) {
            MessageUtils.sendChannelMessage("You have already linked your account! Type " + Main.PREFIX + "unlink" + " to unlink your current account", im.getChannel());
            return;
        }
        String[] args = im.getContent().split(" ");
        if (args.length < 3) {
            MessageUtils.sendSyntax("Link", im.getChannel());
            return;
        }
        String system = args[1].toLowerCase();
        system = formatSystem(system);
        if (system.isEmpty()) {
            MessageUtils.sendChannelMessage("You must set your system to either: [PS4, XBOX, or STEAM]",
                    im.getChannel());
            return;
        }
        system = system.toUpperCase();
        StringBuilder userB = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i != 2)
                userB.append("%20");
            userB.append(args[i]);
        }
        String user = userB.toString().trim();
        user = getUserFromURL(user);
        Rank highestRank = Rank.UNRANKED;
        HashMap<Playlist, Rank> ranks;
        IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
        ranks = getRanksFor(user, system);
        if (ranks == null) {
            MessageUtils.sendChannelMessage("That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
            return;
        }
        for (Rank r : ranks.values()) {
            if (r.val > highestRank.val)
                highestRank = r;
        }
        removeOldRole(im.getAuthor(), im.getGuild());
        String role = highestRank.getStringBroad(); //Broad string matches the roles perfectly
        try {
            im.getAuthor().addRole(im.getGuild().getRolesByName(role).get(0));
            im.getAuthor().addRole(im.getGuild().getRolesByName(system).get(0));
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            MessageUtils.stackTrace(e);
        }
        //if (highestRank.val > Rank.DIAMOND_III.val) {
        //TODO: notify online admins
        //}
        try {
            m.edit(im.getAuthor().mention() + ", you have linked your discord account to " + user + " on " + system);
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            MessageUtils.stackTrace(e);
        }
        UserValue.LINKED_ACCOUNT.setFor(im.getAuthor(), user);
        UserValue.LINKED_PLATFORM.setFor(im.getAuthor(), system);
        UserValue.RANK.setFor(im.getAuthor(), highestRank.toString());
    }

    @CommandA(label = "unlink", name = "Unlink", description = "Unlink your account from your discord", category = Category.GENERAL, usage = ".link [System] [Username]")
    public static void unlinkCommand(IMessage im) {
        try {
            removeOldRole(im.getAuthor(), im.getGuild());
            im.getAuthor().removeRole(im.getGuild().getRolesByName("PS4").get(0));
            im.getAuthor().removeRole(im.getGuild().getRolesByName("PC").get(0));
            im.getAuthor().removeRole(im.getGuild().getRolesByName("XBOX").get(0));
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            MessageUtils.stackTrace(e);
        }
        MessageUtils.sendChannelMessage("You have unlinked your Rocket League account from your discord",
                im.getChannel());
        UserValue.LINKED_ACCOUNT.setFor(im.getAuthor(), "");
        UserValue.LINKED_PLATFORM.setFor(im.getAuthor(), "");
        UserValue.RANK.setFor(im.getAuthor(), "");
    }

    private static String getUserFromURL(String url) {
        String regex = "https?://steamcommunity.com/[a-zA-Z]+/(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return url;
    }

    private static HashMap<Playlist, Rank> getRanksFor(String user, String system) {
        HashMap<Playlist, Rank> ranks = new HashMap<>(); //TODO: Sort this hashmap so that the output is always in the same order
        try {
            String link = "https://rocketleague.tracker.network/profile/" + system + "/" + user;
            Document doc = Jsoup.connect(link).get();
            Element season_table;
            if (doc.getElementsByClass("season-table").size() < 1) { //Only existing users have a season-table
                return null;
            }
            Element season4 = doc.getElementsByClass("card-table").get(0);
            season_table = season4.getElementsByTag("tbody").get(0);
            season4.getElementsByTag("img").get(0);
            StringBuilder sb = new StringBuilder();
            Elements trs = season_table.getElementsByTag("tr");
            for (Element tr : trs) {
                Element cell = tr.getElementsByTag("td").get(1);
                String play = cell.text();
                Playlist list = Playlist.fromTrackerNetwork(play);
                if (list == null)
                    continue;
                if(cell.getElementsByTag("a").size() > 0) //Tracker Network likes to try to "estimate" your rank
                    ranks.put(list, Rank.UNRANKED);
                Element img = tr.getElementsByTag("img").get(0); //img
                String html = img.outerHtml();
                Pattern p = Pattern.compile("([0-9]+)\\.png");

                Matcher m = p.matcher(img.outerHtml());
                int rank = 0;
                while (m.find()) {
                    rank = Integer.parseInt(html.substring(m.start(), m.end() - 4)); //We find the html for a <img> tag and get the png name
                }
                Rank r = Rank.fromVal(rank);
                ranks.put(list, r);
            }
            for(Playlist p : Playlist.values()){
                if(!ranks.containsKey(p)){
                    ranks.put(p, Rank.UNRANKED); //There are no rows for playlists that the user hasn't played
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ranks;
    }

    private static String formatSystem(String system) {
        if (system.equalsIgnoreCase("pc"))
            system = "steam";
        else if (system.equalsIgnoreCase("psn") || system.equalsIgnoreCase("ps4"))
            system = "ps";
        else if (!(system.equalsIgnoreCase("steam") || system.equalsIgnoreCase("xbox")
                || system.equalsIgnoreCase("ps"))) {
            return "";
        }
        return system;
    }

    private static void removeOldRole(IUser user, IGuild guild) {
        List<IRole> roles = user.getRolesForGuild(guild);
        try {
            roleloop:
            for (IRole r : roles) {
                for (Rank rank : Rank.values()) {
                    String name = rank.getStringBroad();
                        if (r.getName().equalsIgnoreCase(name)) {
                            user.removeRole(r);
                            break roleloop;
                        }
                }
            }
            if (guild.getRolesByName("New Member").size() > 0)
                user.removeRole(guild.getRolesByName("New Member").get(0));
        }catch(MissingPermissionsException | RateLimitException | DiscordException e){
            e.printStackTrace();
        }
    }
}
