package discord.modules.command;

import discord.modules.autodel.AutoDelete;
import org.apache.http.HttpException;

import discord.Main;
import discord.utils.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class CommandsHandler {

    @EventSubscriber
    public void OnMesageEvent(MessageReceivedEvent event)
            throws HttpException, DiscordException, MissingPermissionsException, RateLimitException {
        IMessage message = event.getMessage();
        IChannel channel = event.getMessage().getChannel();
		
        if (!message.getContent().startsWith(Main.PREFIX))
            return;

        boolean privateMessage = false;
        for (String channels : AutoDelete.CHANNELS) {
            if((privateMessage = channels.equalsIgnoreCase(channel.getID())))
                break;
        }
        if (privateMessage) {
            channel = Main.INSTANCE.client.getOrCreatePMChannel(message.getAuthor());
        }

        if (message.getContent().equalsIgnoreCase(Main.PREFIX)) {
            MessageUtils.sendChannelMessage("Type '" + Main.PREFIX + "help' to show a list of commands",
                    channel);
            return;
        }
        String commandLabel = message.getContent().split(" ")[0].substring(Main.PREFIX.length());
        for (CommandA ca : Commands.commands.keySet()) {
            if (ca.label().equalsIgnoreCase(commandLabel) || ca.alias().equalsIgnoreCase(commandLabel)) {
                if (PermissionLevel.hasPermissionLevel(message.getGuild(), message.getAuthor(), ca.permissionLevel())
                        || (ca.permissionLevel().equals(PermissionLevel.SLY) && message.getAuthor().getName().equalsIgnoreCase("SlyBro3")
                        && message.getAuthor().getDiscriminator().equalsIgnoreCase("8695"))) {
                    try {
                        Main.databaseModule.tryNewPlayer(message.getAuthor());
                        Commands.commands.get(ca).setAccessible(true);
                        Commands.commands.get(ca).invoke(null, message, privateMessage);
                    } catch (Exception e) {
                        MessageUtils.sendChannelMessage("An error occured in the execution of the command",
                                channel);
                        e.printStackTrace();
                        //MessageUtils.stackTrace(e);
                    }
                } else {
                    MessageUtils.sendChannelMessage("You don't have permission to execute this command!", channel);
                }
                return;
            }

        }
        MessageUtils.sendChannelMessage("Type '" + Main.PREFIX + "help' to show a list of commands",
                channel);
    }

}
