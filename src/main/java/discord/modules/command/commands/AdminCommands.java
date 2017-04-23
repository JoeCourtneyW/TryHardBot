package discord.modules.command.commands;

import java.io.IOException;

import discord.modules.command.Category;
import discord.modules.command.CommandA;
import discord.modules.command.PermissionLevel;
import discord.utils.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class AdminCommands {

	@CommandA(label = "shutdown", name = "Shutdown", permissionLevel=PermissionLevel.SLY,
			category=Category.ADMINISTRATION, description="Shutdown the bot client")
	public static void shutdownCommand(IMessage im) {
		MessageUtils.sendChannelMessage("Shutting down!", im.getChannel());
		try {
			Runtime.getRuntime().exec("sudo pkill java");
		} catch (IOException e) {
		}
		return;
	}
	@CommandA(label = "reboot", name = "Reboot", permissionLevel=PermissionLevel.SLY,
			category=Category.ADMINISTRATION, description="Reboot the bot client")
	public static void rebootCommand(IMessage im) {
		MessageUtils.sendChannelMessage("Rebooting!", im.getChannel());
		try {
			Runtime.getRuntime().exec("sudo reboot");
		} catch (IOException e) {
		}
		return;
	}
	/*@CommandA(label = "update", name = "Update", permissionLevel=PermissionLevel.SLY,
			category=Category.ADMINISTRATION, description="Update and reboot the bot client")
	public static void updateAndRebootCommand(IMessage im) {
		MessageUtils.sendChannelMessage("Updating client.. Please wait", im.getChannel());
		try {
			Process p = Runtime.getRuntime().exec("sudo /home/sly/CynicalRepo/update.sh");
			p.waitFor();
			Process p2 = Runtime.getRuntime().exec("sudo /home/sly/CynicalRepo/update.sh");
			p2.waitFor();
			rebootCommand(im);
		} catch (Exception e) {
			MessageUtils.sendChannelMessage(e.getMessage(), im.getChannel());
		}
		return;
	}*/
	@CommandA(label = "purge", name = "Purge", permissionLevel=PermissionLevel.MODERATOR,
			category=Category.MODERATION, description="Remove messages from a channel")
	public static void purgeCommand(IMessage im) {
		int messagesToPurge = 0;
		if(im.getContent().split(" ").length > 1){
			String arg = im.getContent().split(" ")[1];
			if(arg.equalsIgnoreCase("ALL")){
				messagesToPurge = im.getChannel().getMessages().size();
			}else{
			try{
				messagesToPurge=Integer.parseInt(arg);
			}catch(NumberFormatException nfe){
				MessageUtils.sendChannelMessage("Please input a number of messages to purge or use 'ALL'", im.getChannel());
				return;			}
			}
		}
			try {
				im.getChannel().getMessages().deleteAfter(0, messagesToPurge);
			} catch (RateLimitException e) {
				MessageUtils.sendChannelMessage("An internal error occured (RLE)", im.getChannel());
			} catch (DiscordException e) {
				MessageUtils.sendChannelMessage("An internal error occured (DE)", im.getChannel());
			} catch (MissingPermissionsException e) {
				MessageUtils.sendChannelMessage("An internal error occured (MPE)", im.getChannel());
			}
		return;
	}
}
