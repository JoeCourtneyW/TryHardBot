package discord.modules.welcome;

import discord.modules.config.Configuration.ConfigValue;
import discord.utils.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Welcome {

	
	@EventSubscriber
	public void onJoin(UserJoinEvent e){
		IUser user = e.getUser();
		IGuild g = e.getGuild();
		try {
			if(g.getID().equalsIgnoreCase("295679840659439638")) {
				user.addRole(g.getRolesByName("Set Platform").get(0));
				user.addRole(g.getRolesByName("Set Games").get(0));
				user.addRole(g.getRolesByName("Set Rank").get(0));
				user.addRole(g.getRolesByName("Set Region").get(0));
			}
		}catch(MissingPermissionsException | RateLimitException | DiscordException en){
			en.printStackTrace();
		}
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
