package discord.modules.autodel;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class AutoDelete {

    public static final String[] CHANNELS = {"313423879567572992", "313423959682973697",
    "296732748477890561"};

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
