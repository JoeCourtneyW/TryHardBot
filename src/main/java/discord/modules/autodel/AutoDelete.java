package discord.modules.autodel;

import discord.Main;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoDelete {

    public static final String[] CHANNELS = {"313423879567572992", "313423959682973697",
    "296732748477890561"};

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e){
        for(String channel : CHANNELS){
            if(e.getMessage().getChannel().getID().equalsIgnoreCase(channel)){
                try{
                    IMessage[] message = new IMessage[1];
                    message[0] = e.getMessage();
                    e.getMessage().getChannel().getMessages().bulkDelete(new ArrayList<>(Arrays.asList(message)));
                }catch(RateLimitException | DiscordException | MissingPermissionsException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
