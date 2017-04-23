package discord.modules.database;

import discord.modules.command.Category;
import discord.modules.command.CommandA;
import discord.modules.command.PermissionLevel;
import discord.utils.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;

public class DatabaseCommands {

	@CommandA(label = "database", name = "Database", permissionLevel=PermissionLevel.ADMINISTRATOR, alias="db",
			category=Category.ADMINISTRATION, description="Change a specific player's database entry")
	public static void dataCommand(IMessage im) {
		MessageUtils.sendChannelMessage("Command not yet setup!", im.getChannel());
		
		return;
	}
}
