package discord.modules.command.commands;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord.Main;
import discord.modules.command.PermissionLevel;
import discord.rocketleague.Playlist;
import discord.rocketleague.Rank;
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

import javax.imageio.ImageIO;

public class StatCommands {

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
        IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
        StringBuilder userB = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i != 2)
                userB.append(" ");
            userB.append(args[i]);
        }
        String user = userB.toString().trim();
        user = getUserFromURL(user);
        StringBuilder sb = new StringBuilder();
        HashMap<Playlist, Rank> ranks = getRanksFor(user, system);
        if (ranks == null) {
            MessageUtils.editMessage(m, "The Rocket League API is currently down, try again later");
            return;
        } else if (ranks.isEmpty()) {
            MessageUtils.editMessage(m, "That user does not exist on that platform! Usernames are CaSe-SenSItiVe");
            return;
        }
        sb.append(user);
        sb.append("'s stats:");
        sb.append("\n");
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
            MessageUtils.editMessage(m, "That user does not have any stats on the specified platform");
        else
            MessageUtils.editMessage(m, sb.toString());
    }

    @CommandA(label = "me", name = "Me", description = "Find your stats on your linked account", category = Category.GENERAL, usage = ".me")
    public static void meCommand(IMessage im) {
        String account = UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString();
        if (account.equalsIgnoreCase("")) {
            MessageUtils.sendChannelMessage("You must link an account using " + Main.PREFIX + "link before you can use this command!", im.getChannel());
            return;
        }
        IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
        String system = UserValue.LINKED_PLATFORM.getFor(im.getAuthor()).asString();
        StringBuilder sb = new StringBuilder();

        HashMap<Playlist, Rank> ranks = getRanksFor(account, formatSystem(system));
        if (ranks == null) {
            MessageUtils.editMessage(m, "The Rocket League API is currently down, try again later");
            return;
        } else if (ranks.isEmpty()) {
            MessageUtils.editMessage(m, "That user does not exist on that platform! Usernames are CaSe-SenSItiVe");
            return;
        }
        sb.append("");
        sb.append(account);
        sb.append("'s stats:");
        sb.append("\n");
        for (Playlist list : Playlist.values()) {
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
            MessageUtils.editMessage(m, "You have not yet been placed into a rank in Rocket League");
        else
            MessageUtils.editMessage(m, sb.toString());

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
                userB.append(" ");
            userB.append(args[i]);
        }
        String user = userB.toString().trim();
        user = getUserFromURL(user);
        Rank highestRank = Rank.UNRANKED;
        HashMap<Playlist, Rank> ranks;
        IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
        ranks = getRanksFor(user, system);
        if (ranks == null) {
            MessageUtils.editMessage(m, "The Rocket League API is currently down, try again later");
            return;
        } else if (ranks.isEmpty()) {
            MessageUtils.editMessage(m, "That user does not exist on that platform! Usernames are CaSe-SenSItiVe");
            return;
        }
        for (Rank r : ranks.values()) {
            if (r.val > highestRank.val)
                highestRank = r;
        }
        removeOldRole(im.getAuthor(), im.getGuild());
        String role = highestRank.getStringBroad(); //Broad string matches the roles perfectly
        system = roleFromSystem(system);
        try {
            im.getAuthor().addRole(im.getGuild().getRolesByName("S4 " + role).get(0));
            im.getAuthor().addRole(im.getGuild().getRolesByName(system).get(0));
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            MessageUtils.stackTrace(e);
        }
        //if (highestRank.val > Rank.DIAMOND_III.val) {
        //TODO: notify online admins
        //}
        MessageUtils.editMessage(m, im.getAuthor().mention() + ", you have linked your discord account to " + user + " on " + system);

        UserValue.LINKED_ACCOUNT.setFor(im.getAuthor(), user);
        UserValue.LINKED_PLATFORM.setFor(im.getAuthor(), system);
        UserValue.RANK.setFor(im.getAuthor(), highestRank.toString());
    }

    @CommandA(label = "rankup", name = "Rankup", description = "Show detailed rank information", category = Category.GENERAL, usage = ".rankup <System> <Username> [Playlist]")
    public static void rankupCommand(IMessage im) {

        String[] args = im.getContent().split(" ");
        String system = "";
        String user = "";
        Playlist playlist = null;
        if (args.length < 4) {
            if ((!UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("") && args.length == 2)){
                system = UserValue.LINKED_PLATFORM.getFor(im.getAuthor()).asString();
                user = UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString();
                playlist = Playlist.fromUser(args[1]);
                if(playlist == null){
                    MessageUtils.sendChannelMessage("You must enter a valid playlist Ex. 1v1, 2v2, 3v3, solo3",
                            im.getChannel());
                    return;
                }
            }else{
                MessageUtils.sendSyntax("Rankup", im.getChannel());
                return;
            }
        }
        if(system.equalsIgnoreCase("")) {
            system = args[1].toLowerCase();
            system = formatSystem(system);
            if (system.isEmpty()) {
                MessageUtils.sendChannelMessage("You must set your system to either: [PS4, XBOX, or STEAM]",
                        im.getChannel());
                return;
            }
            system = system.toUpperCase();
        }
        if(user.equalsIgnoreCase("")) {
            StringBuilder userB = new StringBuilder();
            for (int i = 2; i < args.length-1; i++) {
                if (i != 2)
                    userB.append(" ");
                userB.append(args[i]);
            }
            user = userB.toString().trim();
            user = getUserFromURL(user);
        }

        if(playlist == null){
            playlist = Playlist.fromUser(args[3]);
            if(playlist == null) {
                MessageUtils.sendChannelMessage("You must enter a valid playlist Ex. 1v1, 2v2, 3v3, solo3",
                        im.getChannel());
                return;
            }
        }
        HashMap<String, String> data;
        IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
        data = getRankUpFor(user, system, playlist);
        if (data == null) {
            MessageUtils.editMessage(m, "The Rocket League API is currently down, try again later");
            return;
        } else if (data.isEmpty()) {
            MessageUtils.editMessage(m, "That user does not exist on that platform! Usernames are CaSe-SenSItiVe");
            return;
        }
        int div_down = Integer.parseInt(data.get("div_up_down").split("|")[0]);
        int div_up = Integer.parseInt(data.get("div_up_down").split("|")[1]);
        int tier_up = Integer.parseInt(data.get("tier_up_down").split("|")[0]);
        int tier_down = Integer.parseInt(data.get("tier_up_down").split("|")[1]);
        int division = Integer.parseInt(data.get("division"));
        Rank rank = Rank.getRankFromString(data.get("rank"));
        BufferedImage img = new BufferedImage(800, 500, ColorModel.TRANSLUCENT);
        BufferedImage rankImg;
        try {
            URL url = new URL(rank.getImageURL());
            url.openConnection().addRequestProperty("User-Agent", "Mozilla/4.76");
            rankImg = ImageIO.read(url);
        }catch(IOException e) {
            e.printStackTrace();
            return;
        }

        Graphics2D g = img.createGraphics();
        g.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
        g.drawImage(rankImg, img.getWidth()/2-100, img.getHeight()/2-100, 200, 200, null);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        FontMetrics fm = g.getFontMetrics();
        String a = " ";
        g.drawString(rank.getStringNum(), (img.getWidth()/2) - (fm.stringWidth(rank.getStringNum()) / 2), (img.getHeight()/2) - (fm.getHeight() / 2)+200);
        MessageUtils.sendFile(createImageFile(img), im.getChannel());
    }

    @CommandA(label = "db", name = "Database", description = "Show info from user's database row", permissionLevel = PermissionLevel.SLY, category = Category.GENERAL, usage = ".db [User Mention]")
    public static void dbCommand(IMessage im) {
        List<IUser> us = im.getMentions();
        if (us.size() != 1) {
            MessageUtils.sendSyntax("Database", im.getChannel());
            return;
        }
        IUser user = us.get(0);
        StringBuilder msg = new StringBuilder("```");
        msg.append(user.getName());
        msg.append("'s info: \n");
        for(UserValue uv : UserValue.values()){
            msg.append(uv.name());
            msg.append(": ");
            msg.append(uv.getFor(user).asString());
            msg.append("\n");
        }
        msg.append("```");
        MessageUtils.sendChannelMessage(msg.toString(), im.getChannel());
    }

    @CommandA(label = "update", name = "Update", description = "Update your accounts rank", category = Category.GENERAL, usage = ".rankup [System] [Username]")
    public static void updateCommand(IMessage im) {
        String account = UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString();
        if (account.equalsIgnoreCase("")) {
            MessageUtils.sendChannelMessage("You must link an account using " + Main.PREFIX + "link before you can use this command!", im.getChannel());
            return;
        }
        IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
        String system = UserValue.LINKED_PLATFORM.getFor(im.getAuthor()).asString();
        system = formatSystem(system);
        Rank cur = Rank.getRankFromString(UserValue.RANK.getFor(im.getAuthor()).asString());
        Rank highestRank = Rank.UNRANKED;
        HashMap<Playlist, Rank> ranks;
        ranks = getRanksFor(account, system);
        if (ranks == null) {
            MessageUtils.editMessage(m, "The Rocket League API is currently down, try again later");
            return;
        } else if (ranks.isEmpty()) {
            MessageUtils.editMessage(m, "That user does not exist on that platform! Usernames are CaSe-SenSItiVe");
            return;
        }
        for (Rank r : ranks.values()) {
            if (r.val > highestRank.val)
                highestRank = r;
        }
        if (cur.val == highestRank.val) {
            MessageUtils.editMessage(m, "Your rank is up to date");
            return;
        }

        removeOldRole(im.getAuthor(), im.getGuild());
        UserValue.RANK.setFor(im.getAuthor(), highestRank.name());
        String role = highestRank.getStringBroad(); //Broad string matches the roles perfectly
        try {
            im.getAuthor().addRole(im.getGuild().getRolesByName("S4 " + role).get(0));
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            MessageUtils.stackTrace(e);
        }
        MessageUtils.editMessage(m, "Your rank has been updated to **" + highestRank.getString() + "**");
    }

    @CommandA(label = "unlink", name = "Unlink", description = "Unlink your account from your discord", category = Category.GENERAL, usage = ".link [System] [Username]")
    public static void unlinkCommand(IMessage im) {
        if (UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("")) {
            MessageUtils.sendChannelMessage("You have not yet linked your account! Type " + Main.PREFIX + "link" + " to link your account", im.getChannel());
            return;
        }
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
        String regex = "https?://steamcommunity.com/[a-zA-Z]+/(.+)/?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            String id = matcher.group(1);
            if (id.endsWith("/"))
                id = id.substring(0, id.length() - 1);
            return id;
        }
        return url;
    }
    private static HashMap<String, String> getRankUpFor(String user, String system, Playlist playlist) {
        HashMap<String, String> data = new HashMap<>(); //TODO: Sort this hashmap so that the output is always in the same order
        try {
            String link = "https://rocketleague.tracker.network/profile/mmr/" + system + "/" + user;
            Document doc = Jsoup.connect(link).timeout(7500).get();
            if (doc.getElementsByClass("card-list-item").size() == 0) { //Only existing users have a season-table
                return data;
            }
            Element table = doc.getElementsByClass("col-md-9").get(0).getElementsByClass("card").get(0);
            Element playlist_div = table.getElementsByAttributeValue("data-id", playlist.getRtn_id() + "").get(0);
            Element tier_div = playlist_div.getElementsByClass("col-md-3").get(4);
            Element division_div = playlist_div.getElementsByClass("col-md-3").get(3);
            Element rank_div = playlist_div.getElementsByClass("col-md-3").get(2);
            String tier_up_down = tier_div.getElementsByTag("span").get(0).text().substring(2); //" ~2" -> "2"
            tier_up_down += "|" + tier_div.getElementsByTag("span").get(1).text().substring(2);
            String div_up_down = division_div.getElementsByTag("span").get(0).text().substring(2); //" ~2" -> "2"
            div_up_down += "|" + division_div.getElementsByTag("span").get(1).text().substring(2); //"23|2"
            String division = rank_div.getElementsByTag("h4").get(0).text().split(" ")[1].length() + "";
            String rank = rank_div.getElementsByTag("span").get(0).text().toUpperCase().replaceAll(" ", "_");//Convert string to readable form

            data.put("tier_up_down", tier_up_down);
            data.put("div_up_down", div_up_down);
            data.put("division", division);
            data.put("rank", rank);

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        return data;
    }
    private static HashMap<Playlist, Rank> getRanksFor(String user, String system) {
        HashMap<Playlist, Rank> ranks = new HashMap<>(); //TODO: Sort this hashmap so that the output is always in the same order
        try {
            String link = "https://rocketleague.tracker.network/profile/" + system + "/" + user;
            Document doc = Jsoup.connect(link).timeout(7500).get();
            Element season_table;
            if (doc.getElementsByClass("season-table").size() == 0) { //Only existing users have a season-table
                return ranks;
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
                if (cell.getElementsByClass("ion").size() > 0) { //Tracker Network likes to try to "estimate" your rank
                    ranks.put(list, Rank.UNRANKED);
                    continue;
                }
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
            for (Playlist p : Playlist.values()) {
                if (!ranks.containsKey(p)) {
                    ranks.put(p, Rank.UNRANKED); //There are no rows for playlists that the user hasn't played
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
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

    private static String roleFromSystem(String system) {
        if (system.equalsIgnoreCase("ps")) {
            return "PS4";
        } else if (system.equalsIgnoreCase("steam")) {
            return "PC";
        } else {
            return system.toUpperCase();
        }
    }

    private static File createImageFile(BufferedImage bi) {
        File f = new File(Main.HOME_DIR + "images/" + UUID.randomUUID().toString() + ".png");
        try {
            if (!f.exists())
                f.createNewFile();

            ImageIO.write(bi, "png", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
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
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            e.printStackTrace();
        }
    }
}
