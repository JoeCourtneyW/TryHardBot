package discord.modules.command.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import discord.Main;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import discord.modules.command.Category;
import discord.modules.command.CommandA;
import discord.modules.command.PermissionLevel;
import discord.modules.database.UserValue;
import discord.utils.MessageUtils;
import org.jsoup.select.Elements;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class StatCommands {

	public enum Playlist {
		SOLO("Solo Duel", "playlist_10"),
		DOUBLES("Doubles", "playlist_11"),
		SOLO_STANDARD("Solo Standard", "playlist_12"),
		STANDARD("Standard", "playlist_13");

		private String display;
		private String div;

		Playlist(String display, String div) {
			this.display = display;
			this.div = div;
		}

		public String getDisplay() {
			return display;
		}

		public String getDiv() {
			return div;
		}

		public static Playlist fromTrackerNetwork(String tn){
			if(tn.contains("Solo"))
				return SOLO_STANDARD;
			else if(tn.contains("Duel"))
				return SOLO;
			else if(tn.contains("Standard"))
				return STANDARD;
			else if(tn.contains("Doubles"))
				return DOUBLES;
			else
				return null;
		}
	}

	public enum Rank {
		UNRANKED(0, "<:Unranked:304680293200297985>"),
		BRONZE_I(1, "<:Bronze1:303016636045066250>"),
		BRONZE_II(2, "<:Bronze2:303016625148264448>"),
		BRONZE_III(3, "<:Bronze3:303016613152555008>"),
		SILVER_I(4, "<:Silver1:303016599038722048>"),
		SILVER_II(5, "<:Silver2:303016588360155137>"),
		SILVER_III(6, "<:Silver3:303016577803091968>"),
		GOLD_I(7, "<:Gold1:303016563466960906>"),
		GOLD_II(8, "<:Gold2:303016548254220288>"),
		GOLD_III(9, "<:Gold3:303016537151897601>"),
		PLATINUM_I(10, "<:Platinum1:303016525496057858>"),
		PLATINUM_II(11, "<:Platinum2:303016513990950913>"),
		PLATINUM_III(12, "<:Platinum3:303016502745890827>"),
		DIAMOND_I(13, "<:Diamond1:303016490523688960>"),
		DIAMOND_II(14, "<:Diamond2:303016478158880768>"),
		DIAMOND_III(15, "<:Diamond3:303016461369212929>"),
		CHAMPION_I(16, "<:Champion1:303016385158709248>"),
		CHAMPION_II(17, "<:Champion2:303016372508688384>"),
		CHAMPION_III(18, "<:Champion3:303016354502410248>"),
		GRAND_CHAMPION(19, "<:GrandChampion:303016340975910923>");
		int val;
		String emojiID;

		Rank(int val, String emoji) {
			this.emojiID = emoji;
			this.val = val;
		}

		public static Rank fromVal(int val){
			for(Rank r : values()){
				if(r.val == val){
					return r;
				}
			}
			return UNRANKED;
		}
		public static Rank getRankFromString(String r) {
			return valueOf(r.toUpperCase().replaceAll(" ", "_"));
		}

		public String getString() {
			if (this == UNRANKED)
				return "Unranked";
			String name = name().split("_")[0];
			String num = name().split("_")[1];
			if (num.length() < 4)
				return MessageUtils.initialCap(name) + " " + num.toUpperCase(); //Actual num
			else
				return MessageUtils.initialCap(name) + " " + MessageUtils.initialCap(num); //Grand Champion
		}

		public String getStringNum() {
			if (this == UNRANKED)
				return "Unranked";
			String name = name().split("_")[0];
			String num = name().split("_")[1];
			if (num.length() < 4)
				return MessageUtils.initialCap(name) + " " + num.length(); //Actual num
			else
				return MessageUtils.initialCap(name) + " " + MessageUtils.initialCap(num); //Grand Champion
		}

		public String getEmoji() {
			return emojiID;
		}
	}

	public enum Region {

		US_EAST("US-East"),
		US_WEST("US-West"),
		EUROPE("Europe"),
		OCEANIA("Oceania"),
		ASIA_EAST("Asia-East"),
		ASIA_SE_MAINLAND("Asia-SE Mainland"),
		SOUTH_AMERICA("South America"),
		MIDDLE_EAST("Middle East");

		String display;

		Region(String display) {
			this.display = display;
		}
		public String getDisplay(){
			return display;
		}
	}

	@CommandA(label = "stat", name = "Stat", description = "Find your Rocket League stats", category = Category.GENERAL, usage = ".stat [System] [Username]", alias = "stats")
	public static void statCommand(IMessage im) {
		String[] args = im.getContent().split(" ");
		if (args.length < 3) {
			MessageUtils.sendSyntax("Stat", im.getChannel());
			return;
		}
		String system = args[1].toLowerCase();
		if (system.equalsIgnoreCase("pc"))
			system = "steam";
		else if (system.equalsIgnoreCase("psn") || system.equalsIgnoreCase("ps4"))
			system = "ps";
		else if (!(system.equalsIgnoreCase("steam") || system.equalsIgnoreCase("xbox")
				|| system.equalsIgnoreCase("ps"))) {
			MessageUtils.sendChannelMessage("You must set your system to either: [PS4, XBOX, or STEAM]",
					im.getChannel());
			return;
		}
		String user = args[2];
		for (int i = 2; i < args.length; i++) {
			if (i != 2)
				user += "%20" + args[i];
		}
		try {
			String link = "https://rocketleague.tracker.network/profile/" + system + "/" + user;
			Document doc = Jsoup.connect(link).get();
			Element season_table;
			if (doc.getElementsByClass("season-table").size() < 1) {
				MessageUtils.sendChannelMessage(
						"That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
				return;
			}
			Element season4 = doc.getElementsByClass("card-table").get(0);
			season4.getElementsByTag("img").get(0);
			StringBuilder sb = new StringBuilder();
			Elements trs = season4.getElementsByTag("tr");
			for(Element tr : trs){
				String play = trs.get(1).text();
				Playlist list = Playlist.fromTrackerNetwork(play);
				if(list == null)
					continue;
				sb.append(list.getDisplay()); //Only four recognized Playlist values
				sb.append(": ");

				Element img = tr.getElementsByAttributeValue("style", "width:30px;").get(0); //img
				String html = img.outerHtml();
				System.out.println(html);
				Pattern p = Pattern.compile("([0-9]+)\\.png");

				Matcher m = p.matcher(img.outerHtml());
				int rank = 0;
				while(m.find()){
					rank = Integer.parseInt(html.substring(m.start(), m.end()-4));
				}
				Rank r = Rank.fromVal(rank);
				sb.append(r.getStringNum());
				sb.append(" ");
				sb.append(r.getEmoji());
				sb.append("\n");
			}
			MessageUtils.sendChannelMessage(sb.toString(), im.getChannel());
		} catch (IOException e) {
			MessageUtils.stackTrace(e);
		}
	}
	@CommandA(label = "me", name = "Me", description = "Find your stats on your linked account", category = Category.GENERAL, usage = ".me")
	public static void meCommand(IMessage im) {
		String account = UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString();
		if(UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("")){
			MessageUtils.sendChannelMessage("You must link an account using " + Main.PREFIX + "link before you can use this command!", im.getChannel());
			return;
		}
		String system = UserValue.LINKED_PLATFORM.getFor(im.getAuthor()).asString();

	}
	@CommandA(label = "link", name = "Link", description = "Link your account to your discord", category = Category.GENERAL, usage = ".link [System] [Username]")
	public static void linkCommand(IMessage im) {
		if(!UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("")){
			MessageUtils.sendChannelMessage("You have already linked your account! Type " + Main.PREFIX + "unlink" + " to unlink your current account", im.getChannel());
			return;
		}
		String[] args = im.getContent().split(" ");
		if (args.length < 3) {
			MessageUtils.sendSyntax("Link", im.getChannel());
			return;
		}
		String system = args[1].toLowerCase();
		if (system.equalsIgnoreCase("steam"))
			system = "pc";
		else if (system.equalsIgnoreCase("psn"))
			system = "ps4";
		else if (!(system.equalsIgnoreCase("pc") || system.equalsIgnoreCase("xbox")
				|| system.equalsIgnoreCase("ps4"))) {
			MessageUtils.sendChannelMessage("You must set your system to either: [PS4, XBOX, or STEAM]",
					im.getChannel());
			return;
		}
		system = system.toUpperCase();
		StringBuilder userB = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			if (i != 2)
				userB.append("%20");
			userB.append(args[i]);
		}
		String user = userB.toString().trim();
		if(user.toLowerCase().startsWith("https://") || user.toLowerCase().startsWith("http://")){
			MessageUtils.sendChannelMessage("Enter only your steam ID, not the link",
					im.getChannel());
			return;
		}
		Rank highestRank = Rank.UNRANKED;
		HashMap<Playlist, Rank> ranks = new HashMap<Playlist, Rank>();
		IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());

		try {
			String systemLink = system.toLowerCase();
			if (systemLink.equalsIgnoreCase("pc"))
				systemLink = "steam";
			String link = "http://rltracker.pro/profiles/" + user + "/" + systemLink;
			Document doc = Jsoup.connect(link).get();
			if (doc.getElementsByClass("player_avatar_col").size() < 1) {
				/*HtmlPage page = lookupProfile(user, system);
				if (page != null && page.getDocumentElement().getElementsByAttribute("div", "class", "col-xs-2 player_avatar_col")
						.size() > 0) {
					doc = Jsoup.connect(link).get();
					if(doc.getElementsByClass("player_avatar_col").size() < 1){
						MessageUtils.sendChannelMessage(
								"That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
						return;
					}
				} else{
					MessageUtils.sendChannelMessage(
							"That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
				return;
				}*/
				MessageUtils.sendChannelMessage(
						"That user does not exist on that platform! Usernames are CaSe-SenSItiVe", im.getChannel());
			return;
			}

			Element season4 = doc.getElementsByClass("season4_div").get(0);
			for (Playlist p : Playlist.values()) {
				ranks.put(p, Rank
						.getRankFromString(getDataFromDiv(season4.getElementsByClass(p.getDiv()).get(0), "tier_name")));
			}
		} catch (IOException e) {
			MessageUtils.stackTrace(e);
		}
		for (Rank r : ranks.values()) {
			if (r.val > highestRank.val)
				highestRank = r;
		}

		try {
			String role = highestRank.getString().split(" ")[0].trim();
			List<IRole> roles = im.getAuthor().getRolesForGuild(im.getGuild());
			roleloop: for (IRole r : roles) {
				for (Rank rank : Rank.values()) {
					if (r.getName().contains(rank.getString().split(" ")[0])) {
						im.getAuthor().removeRole(r);
						break roleloop;
					}
				}
			}
			if (im.getGuild().getRolesByName("New Member").size() > 0)
				im.getAuthor().removeRole(im.getGuild().getRolesByName("New Member").get(0));
			if (role.equalsIgnoreCase("Grand"))
				role = "Grand Champion";
			im.getAuthor().addRole(im.getGuild().getRolesByName(role).get(0));
			im.getAuthor().addRole(im.getGuild().getRolesByName(system).get(0));
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			MessageUtils.stackTrace(e);
		}
		//if (highestRank.val > Rank.DIAMOND_III.val) {
			//TODO: notify online admins
		//}
		try {
			m.edit(im.getAuthor().mention() + ", you have linked your discord account to " + user + " on " + system);
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			MessageUtils.stackTrace(e);
		}
		UserValue.LINKED_ACCOUNT.setFor(im.getAuthor(), user);
		UserValue.LINKED_PLATFORM.setFor(im.getAuthor(), system);
		UserValue.RANK.setFor(im.getAuthor(), highestRank.toString());
	}

	@CommandA(label = "unlink", name = "Unlink", description = "Unlink your account from your discord", category = Category.GENERAL, usage = ".link [System] [Username]")
	public static void unlinkCommand(IMessage im) {
		try {
			List<IRole> roles = im.getAuthor().getRolesForGuild(im.getGuild());
			roleloop: for (IRole r : roles) {
				for (Rank rank : Rank.values()) {
					if (r.getName().contains(rank.getString().split(" ")[0])) {
						im.getAuthor().removeRole(r);
						break roleloop;
					}
				}
			}
			im.getAuthor().removeRole(im.getGuild().getRolesByName("PS4").get(0));
			im.getAuthor().removeRole(im.getGuild().getRolesByName("PC").get(0));
			im.getAuthor().removeRole(im.getGuild().getRolesByName("XBOX").get(0));
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			MessageUtils.stackTrace(e);
		}
		MessageUtils.sendChannelMessage("You have unlinked your Rocket League account from your discord",
				im.getChannel());
		UserValue.LINKED_ACCOUNT.setFor(im.getAuthor(), "");
		UserValue.LINKED_PLATFORM.setFor(im.getAuthor(), "");
		UserValue.RANK.setFor(im.getAuthor(), "");
	}
	@CommandA(label = "find", name = "Find", description = "Find a rocket league account",
			category = Category.NONE, usage = ".find [System] [Username]",
			permissionLevel=PermissionLevel.MODERATOR)
	public static void testCommand(IMessage im) {
		//if(!UserValue.LINKED_ACCOUNT.getFor(im.getAuthor()).asString().equalsIgnoreCase("")){
		//	MessageUtils.sendChannelMessage("You have already linked your account! Type " + Main.PREFIX + "unlink" + " to unlink your current account", im.getChannel());
		//	return;
		//}
		String[] args = im.getContent().split(" ");
		if (args.length < 10) {
			MessageUtils.sendSyntax("Find", im.getChannel());
			return;
		}
		String system = args[1].toLowerCase();
		if (system.equalsIgnoreCase("steam"))
			system = "pc";
		else if (system.equalsIgnoreCase("psn"))
			system = "ps4";
		else if (!(system.equalsIgnoreCase("pc") || system.equalsIgnoreCase("xbox")
				|| system.equalsIgnoreCase("ps4"))) {
			MessageUtils.sendChannelMessage("You must set your system to either: [PS4, XBOX, or STEAM]",
					im.getChannel());
			return;
		}
		system = system.toUpperCase();
		String user;
		StringBuilder u = new StringBuilder(args[2]);
		for (int i = 2; i < args.length; i++) {
			if (i != 2)
				u.append("%20");
				u.append(args[i]);
		}
		user = u.toString().trim();
		IMessage m = MessageUtils.sendChannelMessage("Loading...", im.getChannel());
			String systemLink = system.toLowerCase();
			if (systemLink.equalsIgnoreCase("pc"))
				systemLink = "steam";
			lookupProfile(user, system);
			try{
				String link = "http://rltracker.pro/profiles/" + user + "/" + systemLink;
				Document doc = Jsoup.connect(link).get();
				if (doc.getElementsByClass("player_avatar_col").size() > 0) {
					m.edit("User found!");
				} else{
					m.edit("That user does not exist on that platform! Usernames are CaSe-SenSItiVe");
					
				}
				}catch(IOException | MissingPermissionsException | RateLimitException | DiscordException e){
				MessageUtils.stackTrace(e);
			}
	}
	/*
	 * playlist_name | Doubles tier_name | Gold III division | Division I rating
	 * | Rating 670 ranking_percents | Top 57.05% matches | 143 Matches
	 * 
	 */
	private static String getDataFromDiv(Element div, String className) {
		return div.getElementsByClass(className).get(0).text();
	}

	private static void lookupProfile(String user, String system) {
		final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
		try {

			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setDownloadImages(false);
			//webClient.getOptions().setThrowExceptionOnScriptError(false);
			/* turn off annoying htmlunit warnings */
	        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

			HtmlPage mainPage = webClient.getPage("http://rltracker.pro/");

			HtmlForm lookup = mainPage.getForms().get(0);
			for (HtmlForm form : mainPage.getForms()) {
				if (form.getActionAttribute().equalsIgnoreCase("/profiles/lookup")) {
					lookup = form;
					break;
				}
			}
			final HtmlTextInput accountField = lookup.getInputByName("account");
			final HtmlHiddenInput platformField = lookup.getInputByName("platform_id");
			final HtmlElement button = lookup.getElementsByAttribute("div", "class", "send_form").get(0);
			accountField.setText(user.replaceAll("%20", " "));

			String platform_id = (system.length()-1) + "";
			platformField.setValueAttribute(platform_id);
			webClient.getOptions().setJavaScriptEnabled(false);
			button.click(); //submit form
			webClient.close();
		} catch (IOException ioe) {
			MessageUtils.stackTrace(ioe);
			webClient.close();
		}
	}

	public String getUserFromURL(String url) {
		String regex = "http://steamcommunity.com/[a-z]+/(.+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(url);
		if (matcher.matches())
			return matcher.group(1);
		return "";
	}
}
