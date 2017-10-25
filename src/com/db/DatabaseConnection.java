package com.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.ef.Parser;

public class DatabaseConnection {
	private String db_host = "localhost";
	private String db_port = "3306";
	private String db_name = "wallethub_db";
	private String db_username = "admin";
	private String db_password = "admin";
	
	public void init(String configFile) {
		// Connect to database then querying
		// =================================
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configFile);
			// load a properties file
			prop.load(input);

			// Get database info
			this.db_host = prop.getProperty("DB_HOST");
			this.db_port = prop.getProperty("DB_PORT");
			this.db_name = prop.getProperty("DB_NAME");
			this.db_username = prop.getProperty("DB_USERNAME");
			this.db_password = prop.getProperty("DB_PASSWORD");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Connection connectAndCreate() {
		Connection connection = null;
		try {
			String connectionUrl = "jdbc:mysql://"+this.db_host + ":" + this.db_port 
					+"/" + this.db_name + "?user=" + this.db_username + "&password=" + this.db_password;
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			connection = DriverManager.getConnection(connectionUrl);
			if (connection != null) {
				if (Parser.isDebug)
					System.out.println("Connect to DB successful! ");
				return connection;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {}

		return null;
	}
	
	public void closeConnection (Connection connection) {
		if (connection != null) {
			try {
				connection.close();
				if (Parser.isDebug)
					System.out.println("Close DB successful! ");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
