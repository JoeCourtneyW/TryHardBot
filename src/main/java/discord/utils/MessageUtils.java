package discord.utils;

import discord.Main;
import discord.modules.command.CommandA;
import discord.modules.command.Commands;
import org.apache.commons.lang3.exception.ExceptionUtils;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.MessageBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MessageUtils {

	public static IMessage sendChannelMessage(String message, IChannel channel){
		IMessage im = Main.INSTANCE.client.getMessageByID("0");
		try {
			im = new MessageBuilder(Main.INSTANCE.client).appendContent(message)
			.withChannel(channel).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return im;
	}
	public static void stackTrace(Exception e){
		IGuild g = Main.INSTANCE.client.getGuildByID("107131596355698688");
		try{
			String exception = ExceptionUtils.getStackTrace(e);
			new MessageBuilder(Main.INSTANCE.client).appendContent(exception)
					.withChannel(g.getChannelByID("309750962149392384")).build();
		}catch (Exception ex) {
			//Don't chain these stupid fucking exceptions
		}
	}
	public static void sendPrivateMessage(String message, IUser user){
		try {
			IPrivateChannel pc = Main.INSTANCE.client.getOrCreatePMChannel(user);
			new MessageBuilder(Main.INSTANCE.client).appendContent(message)
			.withChannel(pc).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendSyntax(String commandName, IChannel channel){
		//TODO: Incorporate ability for subcommands
		String usage = "";
		String desc = "";
		for(CommandA c : Commands.commands.keySet()){
			if(c.name().equalsIgnoreCase(commandName)){
				usage = c.usage();
				desc = c.description();
			}	
		}
		sendChannelMessage(formatString(usage.replaceAll("\\.", Main.PREFIX) + "\n \n" + desc, FormatOption.MULTI_LINE_CODE_BLOCK), channel);
		
	}
	public static String formatString(String message, FormatOption format){
		return format.getTag() + message + reverseString(format.getTag());
	}
	public static String reverseString(String message){
		char[] chars = message.toCharArray();
		String newMessage = "";
		for(int i = chars.length-1; i >= 0; i--){
			newMessage += chars[i];
		}
		return newMessage;
	}
	public static String initialCap(String message){
		String nMessage = "";
		for(String word : message.split(" ")){
			nMessage = nMessage + word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase() + " ";
		}
		return nMessage;
	}
	public enum FormatOption{
		ITALICS("*"),
		BOLD("**"),
		BOLD_ITALICS("***"),
		STRIKEOUT("~~"),
		UNDERLINE("__"),
		UNDERLINE_ITALICS("__*"),
		UNDERLINE_BOLD("__**"),
		UNDERLINE_BOLD_ITALICS("__***"),
		CODE_BLOCK("`"),
		MULTI_LINE_CODE_BLOCK("```");
		
		private String tag;
		FormatOption(String tag){
			this.tag = tag;
		}
		public String getTag(){
			return tag;
		}
	}
}
