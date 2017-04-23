package discord.modules.database;

import discord.Main;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserJoinEvent;

public class DatabaseListener{


	@EventSubscriber
	public void newPlayerOnJoin(UserJoinEvent event) {
		Main.databaseModule.tryNewPlayer(event.getUser());
	}
	public static void oneTimeOnly(){
		Main.databaseModule.fixMismatch();
		/*for(IGuild g : Main.INSTANCE.client.getGuilds()){
			if(g.getName().contains("Try Hard")){
			for(IUser u : g.getUsers()){
				if(!u.isBot())
				Main.databaseModule.tryNewPlayer(u);
			}
			}
		}*/
		
	}
}
