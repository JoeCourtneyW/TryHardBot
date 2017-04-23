package discord.modules.command.commands;

import discord.modules.command.Category;
import discord.modules.command.CommandA;
import discord.modules.command.PermissionLevel;
import discord.utils.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;

public class CommandInvite {

	@CommandA(label="invite", name="Invite", description="Check the metadata of invites", category=Category.MODERATION,
			usage=".invite [check, list, delete] <user>", permissionLevel=PermissionLevel.MODERATOR)
	public static void inviteCommand(IMessage im){
		String[] args = im.getContent().split(" ");
		if(args.length < 1){
			MessageUtils.sendSyntax("Invite", im.getChannel());
			return;
		}
		if(args[1].equalsIgnoreCase("check")){
			
		}
	}
}
