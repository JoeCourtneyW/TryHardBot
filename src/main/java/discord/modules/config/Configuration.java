package discord.modules.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

public class Configuration implements IModule {
	public static enum ConfigValue {
		PREFIX("!"),
		ADMIN_ROLE("Admin"),
		MODERATOR_ROLE("Moderator"),
		DATABASE_HOST("localhost"),
		DATABASE_PORT("3306"),
		DATABASE_DB("database"),
		DATABASE_USERNAME("root"),
		DATABASE_PASSWORD("password"),
		WELCOME_MESSAGE("Welcome, %user%!"),
		FAREWELL_MESSAGE("Farewell, %user%!");

		private String def;

		ConfigValue(String defaultValue) {
			this.def = defaultValue;
		}

		public String getDefaultValue() {
			return def;
		}

		public String getValue() {
			if(configValues.containsKey(name()))
				return configValues.get(name());
			else{
				configValues.put(name(), getDefaultValue());
				return getDefaultValue();
			}
		}

		public void setValue(String val) {
			configValues.put(name(), val);
		}
	}

	private String moduleName = "Configuration";
	private String moduleVersion = "1.0";
	private String moduleMinimumVersion = "2.3.0";
	private String author = "SlyVitality";
	private static HashMap<String, String> configValues = new HashMap<String, String>();
	private HashMap<Integer, String> comments = new HashMap<Integer, String>();
	File f;

	public static IDiscordClient client;

	public boolean enable(IDiscordClient dclient) {
		f = new File("config.sly");
		client = dclient;
		createConfigs();
		readConfigs();
		System.out.println("[Module] Enabled " + moduleName + " V" + moduleVersion + " by " + author);
		//rewriteConfigs();
		return true;
	}

	public void disable() {
		rewriteConfigs();
	}

	private void createConfigs() { // only used in new config creation
		if (!f.exists()) {
			try {
				f.createNewFile();
				PrintWriter pw = new PrintWriter(new FileWriter(f));
				StringBuilder sb = new StringBuilder();
				for (ConfigValue cv : ConfigValue.values()) {
					sb.append(cv.name() + ": ");
					sb.append("'" + cv.getDefaultValue() + "'");
					sb.append("\n");
				}
				pw.write(sb.toString());
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void readConfigs() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			int lineNum = 0;
			while ((line = in.readLine()) != null) {
				lineNum++;
				if (!line.startsWith("#")) // start comments with #
					ConfigValue.valueOf(line.split(":")[0])
							.setValue(line.substring(line.indexOf("'") + 1, line.lastIndexOf("'")));
				else
					comments.put(lineNum, line);
			}
			in.close();
		} catch (IOException e) {
		}
	}

	private void rewriteConfigs() { // Rewrite needed in case values are changed during runtime

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ConfigValue.values().length; i++) {
			ConfigValue key = ConfigValue.values()[i];
			if (configValues.containsKey(key))
				sb.append(key.name() + ": '" + key.getValue() + "'");
			else
				sb.append(key.name() + ": '" + key.getDefaultValue() + "'");
			sb.append("\n");
		}
		for (Integer commentLine : comments.keySet()) {
			sb.insert(StringUtils.ordinalIndexOf(sb.toString(), "\n", commentLine - 1),
					"\n" + comments.get(commentLine));
		}
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.write(sb.toString());
			pw.close();
		} catch (IOException e) {
		}
	}

	public void reloadConfig() { // Use if changed manually in file
		readConfigs();
	}

	public void saveConfig() { // Use if changed during runtime
		rewriteConfigs();
	}

	// DEPRECATED
	/*
	 * public HashMap<String, String> getConfigValues() { return configValues; }
	 */

	public String getAuthor() {
		return author;
	}

	public String getMinimumDiscord4JVersion() {
		return moduleMinimumVersion;
	}

	public String getName() {
		return moduleName;
	}

	public String getVersion() {
		return moduleVersion;
	}
}
