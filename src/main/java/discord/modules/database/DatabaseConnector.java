package discord.modules.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import discord.modules.config.Configuration.ConfigValue;

public class DatabaseConnector {

	private String HOSTNAME, PORT, DATABASE, USERNAME, PASSWORD;
	public Connection connection = null;

	public DatabaseConnector(){
	}
	public void loadValues() {
		PORT = ConfigValue.DATABASE_PORT.getValue();
		HOSTNAME = ConfigValue.DATABASE_HOST.getValue();
		DATABASE = ConfigValue.DATABASE_DB.getValue();
		USERNAME = ConfigValue.DATABASE_USERNAME.getValue();
		PASSWORD = ConfigValue.DATABASE_PASSWORD.getValue();
		System.out.println("Connecting to " + HOSTNAME + ":" + PORT + "/" + DATABASE + " with username: " + USERNAME + " and password: " + PASSWORD);
	}

	public void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver"); //Make sure Driver is loaded in
			connection = DriverManager.getConnection("jdbc:mysql://" + HOSTNAME +":" + PORT + "/" + DATABASE, USERNAME,
					PASSWORD);
			System.out.println("Database successfully connected!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		if (connection != null) {
			return connection;
		} else {
			throw new DatabaseException("Connection was referenced as null");
		}
	}

	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			throw new DatabaseException("No connection available to close");
		}
	}
}
