package discord.modules.welcome;

import discord.modules.config.Configuration.ConfigValue;
import discord.utils.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;

public class Welcome {

	
	@EventSubscriber
	public void onJoin(UserJoinEvent e){
		String message = ConfigValue.WELCOME_MESSAGE.getValue();
		message = message.replaceAll("%user%", e.getUser().getName());
		message = message.replaceAll("%server%", e.getGuild().getName());
		//message = message.replaceAll("\\{#getting-started\\}", e.getGuild().getChannelsByName("getting-started").get(0).mention());
		/*Pattern pattern = Pattern.compile("\\{[^]+\\}");
		Matcher matcher = pattern.matcher(message);
		while (matcher.find())
		{
		    message = message.replaceFirst("{[^]+}", e.getGuild().getChannelsByName(matcher.group(1)).get(0).mention());
		}	*/
		MessageUtils.sendPrivateMessage(message, e.getUser());
		//TODO: Get invite that player join was attached to
	}
	@EventSubscriber
	public void onLeave(UserLeaveEvent e){
		String message = ConfigValue.FAREWELL_MESSAGE.getValue();
		message = message.replaceAll("%user%", e.getUser().getName());
		//MessageUtils.sendPrivateMessage(message, e.getUser());
	}
}
