package discord.modules.command.commands;

import discord.modules.command.Category;
import discord.modules.command.CommandA;
import discord.modules.command.Commands;
import discord.utils.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;

public class CommandHelp{

	@CommandA(label = "help", name = "Help", description="List all commands on the server")
	public static void helpCommand(IMessage im, boolean na) {
		String message = "```";
		for (Category cat : Category.values()) {
			if(cat == Category.NONE || cat ==Category.MUSIC || cat == Category.MODERATION || cat == Category.ADMINISTRATION)
				continue;
			message += cat.getFormal() + ": \n";
			for (CommandA ca : Commands.commands.keySet()) {
				if(ca.category() != cat)
					continue;
				String spacer = "";
				for (int i = 0; i < 15 - ca.label().length(); i++)
					spacer += " ";
				message += "  " + ca.label() + spacer + ca.description() + "\n";
			}
		}
		MessageUtils.sendPrivateMessage(message + "```", im.getAuthor());
	}

}
