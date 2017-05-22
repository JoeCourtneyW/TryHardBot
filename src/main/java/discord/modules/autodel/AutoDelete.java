package discord.modules.autodel;

import discord.Main;
import discord.modules.command.commands.StatCommands;
import discord.modules.database.DatabaseObject;
import discord.modules.database.UserValue;
import discord.rocketleague.Playlist;
import discord.rocketleague.Rank;
import discord.utils.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AutoDelete {


    /*
    set-platform: 316026587214643210
    set-rank: 313423959682973697
    set-region: 316036444533555200
    set-games: 313423879567572992
    commands-help: 296732748477890561
     */
    public static final String[] CHANNELS = {"316026587214643210", "313423959682973697",
    "316036444533555200", "313423879567572992", "296732748477890561"};

    private static final String[] PLATFORMS = {"PC", "STEAM", "PSN", "PS4", "PS", "PLAYSTATION", "XBOX"};

    private static final String[] REGIONS = {"US-East", "US-West", "South America", "Europe", "Asia-SE Mainland", "Asia-East", "Middle-East", "Oceania", "Unknown"};

    private static final String[] GAMES = {"ROCKET LEAGUE", "RL", "ROCKETLEAGUE"};

    @EventSubscriber
    public void onSetRole(MessageReceivedEvent e){
        String m = e.getMessage().getContent().trim();
        IUser user = e.getMessage().getAuthor();
        IGuild g = e.getMessage().getGuild();
        try{
        if(e.getMessage().getChannel().getID().equalsIgnoreCase("316026587214643210")) { //set-platform
            ArrayList platforms = new ArrayList<>(Arrays.asList(PLATFORMS));
            String platform;
            if(platforms.contains(m.toUpperCase())){
                if(m.equalsIgnoreCase(PLATFORMS[0]) || m.equalsIgnoreCase(PLATFORMS[1])){
                    //PC
                        user.addRole(g.getRolesByName("PC").get(0));

                        platform = "PC";
                }else if(m.equalsIgnoreCase(PLATFORMS[2]) || m.equalsIgnoreCase(PLATFORMS[3])
                        || m.equalsIgnoreCase(PLATFORMS[4]) || m.equalsIgnoreCase(PLATFORMS[5])){
                    user.addRole(g.getRolesByName("PS4").get(0));
                    platform = "PS4";
                    //PS4
                }else if(m.equalsIgnoreCase(PLATFORMS[6])){
                    user.addRole(g.getRolesByName("XBOX").get(0));
                    platform = "XBOX";
                    //XBOX
                }else{
                    MessageUtils.sendPrivateMessage("Wrong format! Check the channel's help post", user);
                    return;
                }
                UserValue.LINKED_PLATFORM.setFor(user, platform);
                MessageUtils.sendPrivateMessage("You have set your platform to: " + platform, user);
                user.removeRole(g.getRolesByName("Set Platform").get(0));
            }
        }else if(e.getMessage().getChannel().getID().equalsIgnoreCase("313423959682973697")) { //set-rank
                String platform = UserValue.LINKED_PLATFORM.getFor(user).asString();
            if(platform.equalsIgnoreCase("")){
                MessageUtils.sendPrivateMessage("You must set your platform before you link your account", user);
                return;
            }
            String acc = StatCommands.getUserFromURL(m);
            if(UserValue.LINKED_ACCOUNT.getAll().contains(new DatabaseObject(user))){
                MessageUtils.sendPrivateMessage("That user has already been linked to a discord account.", user);
                return;
            }
            Rank highestRank = Rank.UNRANKED;
            HashMap<Playlist, Rank> ranks;
            if(platform.equalsIgnoreCase("PC"))
                platform = "steam";
            ranks = StatCommands.getRanksFor(acc, platform);
            if (ranks == null) {
                MessageUtils.sendPrivateMessage("The Rocket League API is currently down, try again later", user);
                return;
            } else if (ranks.isEmpty()) {
                MessageUtils.sendPrivateMessage("That user does not exist on that platform! Usernames are CaSe-SenSItiVe", user);
                return;
            }
            for (Rank r : ranks.values()) {
                if (r.val > highestRank.val)
                    highestRank = r;
            }
            StatCommands.removeOldRole(user, g);

            String role = highestRank.getStringBroad(); //Broad string matches the roles perfectly
            user.addRole(g.getRolesByName("S4 " + role).get(0));
            user.removeRole(g.getRolesByName("Set Rank").get(0));

            MessageUtils.sendPrivateMessage("You have set your account to: " + acc + " on " + platform, user);

            UserValue.LINKED_ACCOUNT.setFor(user, acc);
            UserValue.RANK.setFor(user, highestRank.toString());
        }else if(e.getMessage().getChannel().getID().equalsIgnoreCase("316036444533555200")) { //set-region
            for(String region : REGIONS){
                if(m.equalsIgnoreCase(region)){
                    user.addRole(g.getRolesByName(region).get(0));
                    user.removeRole(g.getRolesByName("Set Region").get(0));
                    UserValue.REGION.setFor(user, region);
                    MessageUtils.sendPrivateMessage("You have set your region to: " + region, user);
                    return;
                }
            }
            MessageUtils.sendPrivateMessage("Wrong format! Check the channel's help post", user);
        }else if(e.getMessage().getChannel().getID().equalsIgnoreCase("313423879567572992")) { //set-games
            ArrayList games = new ArrayList<>(Arrays.asList(GAMES));
            if(games.contains(m.toUpperCase())){
                if(m.equalsIgnoreCase(GAMES[0]) || m.equalsIgnoreCase(GAMES[1]) || m.equalsIgnoreCase(GAMES[2])){
                    //ROCKET LEAGUE
                    user.addRole(g.getRolesByName("Rocket League").get(0));
                    user.removeRole(g.getRolesByName("Set Game").get(0));
                    MessageUtils.sendPrivateMessage("You have set your game to: " + "Rocket League", user);
                    return;
                }
            }
            MessageUtils.sendPrivateMessage("Wrong format! Check the channel's help post", user);
        }
            } catch (MissingPermissionsException | RateLimitException | DiscordException en) {
                MessageUtils.stackTrace(en);
            MessageUtils.sendPrivateMessage("Uh oh, contact an administrator", user);
            }
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e){
        for(String channel : CHANNELS){
            if(e.getMessage().getChannel().getID().equalsIgnoreCase(channel)){
                try{
                    e.getMessage().delete();
                }catch(RateLimitException | DiscordException | MissingPermissionsException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
