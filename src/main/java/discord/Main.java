package discord;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord.modules.command.Commands;
import discord.modules.config.Configuration;
import discord.modules.database.Database;
import discord.modules.welcome.Welcome;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;

public class Main {
	//https://discordapp.com/api/oauth2/authorize?client_id=273659666054119425&scope=bot
	private final static String PERSONAL_TOKEN = "MjczNjU5NjY2MDU0MTE5NDI1.C2mw-Q.5lI1_lHcWWx5oRfHPQtGcVV4tnA";
	private final static String CLIENT_ID = "273659666054119425";
	private final static String SERVER_ADD_LINK = "https://discordapp.com/api/oauth2/authorize?client_id=" + CLIENT_ID
			+ "&scope=bot";

	public static Main INSTANCE;
	public static String PREFIX = "";
	public static String HOME_DIR = "/root/tryhardbot/";
	public IDiscordClient client;
	public static Commands commandsModule;
	public static Configuration configModule;
	public static Database databaseModule;

	public static void main(String[] args) {
		INSTANCE = login();
		EventDispatcher dispatcher = INSTANCE.client.getDispatcher();
		dispatcher.registerListener(INSTANCE);
		dispatcher.registerListener(new Welcome());
		activateModules();


	}

	public Main(IDiscordClient dclient) {
		this.client = dclient;
	}

	private static Main login() {
		return new Main(createClient(PERSONAL_TOKEN));
	}

	private static void activateModules() {
		commandsModule = new Commands();
		commandsModule.enable(INSTANCE.client);
		configModule = new Configuration();
		configModule.enable(INSTANCE.client);
		databaseModule = new Database();
		databaseModule.enable(INSTANCE.client);
	}

	private static IDiscordClient createClient(String token) { // Returns a new instance of the Discord client
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			return clientBuilder.login(); // Creates the client instance and logs the client in
		} catch (DiscordException e) { // This is thrown if there was a problem building the client
			e.printStackTrace();
			return null;
		}

	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		List<IGuild> guilds = INSTANCE.client.getGuilds();
		if (guilds.size() == 0) {
			System.out.println("Looks like you haven't added the bot to a server! Click this link to register him.");
			System.out.println(SERVER_ADD_LINK);
		}
		PREFIX = Configuration.ConfigValue.PREFIX.getValue(); //Just to make it more accessible
		try {
			Image i = Image.forFile(new File(HOME_DIR + "tryhardfriends.png")); //TODO
			INSTANCE.client.changeAvatar(i);
			
			INSTANCE.client.changeUsername("Try Hard With Friends"); //If restarted too often, throws error
			INSTANCE.client.changeStatus(Status.game("V1.1"));

		} catch (Exception e) {
			//e.printStackTrace(); RateLimitError sometimes
		}
		databaseModule.load();
	}
}
