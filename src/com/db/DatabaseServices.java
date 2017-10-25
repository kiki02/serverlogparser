package com.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ef.BlockedIP;
import com.ef.Parser;

public class DatabaseServices {
	private String ACCESS_LOG_TABLE = "ACCESS_LOG_TABLE";
	private String BLOCKED_LOG_TABLE = "BLOCKED_LOG_TABLE";
	
	//core functions
	// Execute update queries, return number of rows changed
	public int executeSQLUpdate (Connection connection, String sqlStr, Statement statement) {
		int ret = 0;
		try {
			if (Parser.isDebug)
				System.out.println(sqlStr);
			ret = statement.executeUpdate(sqlStr);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}
	
	// Execute select queries, return ResultSet object
	public ResultSet executeSQLSelect (Connection connection, String sqlStr, Statement statement) {
		ResultSet resultSet = null;
		try {
			if (Parser.isDebug)
				System.out.println(sqlStr);
            resultSet = statement.executeQuery(sqlStr);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return resultSet;
	}
	// end core functions
	
	//Getter functions
	// checking the DB is valid structure, if not, create it
	public void checkDbValid(Connection connection) {
		String checkingSQL = "SHOW TABLES LIKE '" + this.ACCESS_LOG_TABLE + "';";
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rs = executeSQLSelect(connection, checkingSQL, statement);
			if (!rs.next()) {
				createDbAccessLogTable(connection);
				createDbBlockedTable(connection);
			} else {
				if (Parser.isDebug)
					System.out.println("DB is OK!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
			}
		}
	}
	
	// finding IPs which should be blocked
	public ArrayList<BlockedIP> findingBlockedIPs(Connection connection, String startDate, int duration, int threshold) {
		ArrayList<BlockedIP> listIPs = new ArrayList<BlockedIP>();
		String sqlStr = "SELECT * FROM (SELECT IP,COUNT(*) as count FROM " + this.ACCESS_LOG_TABLE + " WHERE DATE >= '" + startDate 
				+ "' AND DATE < DATE_ADD('" + startDate + "', INTERVAL " + duration + " HOUR) GROUP BY IP) AS ListIP WHERE ListIP.count >= " 
				+ threshold + " ORDER BY ListIP.count DESC;";
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rs = executeSQLSelect(connection, sqlStr, statement);
			while (rs.next()) {
				int count = rs.getInt("count");
				String ip = rs.getString("IP");
				
				BlockedIP blockedIP = new BlockedIP(ip, count);
				listIPs.add(blockedIP);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
			}
		}
		return listIPs;
	}
	// end Getter functions
	
	//Setter functions
	// create table for storing access logs
	public void createDbAccessLogTable(Connection connection) throws SQLException {
		Statement statement = null;
		String createTableSQL = "CREATE TABLE " + this.ACCESS_LOG_TABLE + " ("
				+ "DATE DATETIME(3) NOT NULL, "
				+ "IP VARCHAR(50) NOT NULL, "
				+ "REQUEST VARCHAR(30) NOT NULL, "
				+ "STATUS VARCHAR(3) NOT NULL, "
				+ "USER_AGENT VARCHAR(500) NOT NULL "
				+ ")";
		try {
			statement = connection.createStatement();
			if (Parser.isDebug)
				System.out.println(createTableSQL);
            // execute the SQL stetement
			statement.execute(createTableSQL);
			if (Parser.isDebug)
				System.out.println("Table \"" + this.ACCESS_LOG_TABLE + "\" is created!");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	// create table for storing the blocked reasons
	public void createDbBlockedTable(Connection connection) throws SQLException {
		Statement statement = null;
		String createTableSQL = "CREATE TABLE " + this.BLOCKED_LOG_TABLE + " ("
				+ "ID INT NOT NULL AUTO_INCREMENT, "
				+ "IP VARCHAR(50) NOT NULL, "
				+ "START_DATE DATETIME NOT NULL, "	
				+ "DURATION INT NOT NULL, "
				+ "TOTAL_REQUESTS INT NOT NULL, "
				+ "THRESHOLD INT NOT NULL, "
				+ "STATUS VARCHAR(10) NOT NULL, " 
				+ "COMMENTS VARCHAR(200) NOT NULL, " + "PRIMARY KEY (ID) "
				+ ")";
		try {
			statement = connection.createStatement();
			if (Parser.isDebug)
				System.out.println(createTableSQL);
            // execute the SQL stetement
			statement.execute(createTableSQL);
			if (Parser.isDebug)
				System.out.println("Table \"" + this.BLOCKED_LOG_TABLE + "\" is created!");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	// load access log to access_log_table
	public void loadAccessLogToTable(Connection connection, String filePath) {
		String sqlResetStr = "truncate " + this.ACCESS_LOG_TABLE;
		String sqlStr = "load data infile \"" + filePath + "\" into table " + this.ACCESS_LOG_TABLE + " fields terminated by '|' lines terminated by '\\n';";
		
		Statement statement = null;
		try {
			statement = connection.createStatement();
			executeSQLUpdate(connection, sqlResetStr, statement);
			int ret = executeSQLUpdate(connection, sqlStr, statement);
			if (ret > 0 && Parser.isDebug)
				System.out.println("load total " + ret + " access logs done!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
			}
		}
	}
	
	public void insertBlockedIPToTable(Connection connection, ArrayList<BlockedIP> listBlockedIPs, String startDate, int duration, int threshold) {
		String sqlStr = "INSERT INTO blocked_log_table (IP, START_DATE, DURATION, TOTAL_REQUESTS, THRESHOLD, STATUS, COMMENTS) VALUES(?,?,?,?,?,'BLOCKED',?);";

		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sqlStr);
			for (int i=0; i<listBlockedIPs.size(); i++) {
				BlockedIP blockedIP = listBlockedIPs.get(i);
				
				preparedStatement.setString(1, blockedIP.IP);
				preparedStatement.setString(2, startDate);
				preparedStatement.setInt(3, duration);
				preparedStatement.setInt(4, blockedIP.TotalRequest);
				preparedStatement.setInt(5, threshold);
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
				Date date = dateFormat.parse(startDate);
				Long time = date.getTime();
				time +=(duration*60*60*1000);
				Date new_date = new Date(time);
				String commentStr = "This IP has " + blockedIP.TotalRequest + " requests between " + startDate 
						+ " and " + dateFormat.format(new_date) + ", exceeding the limit threshold " + threshold + ".";
				preparedStatement.setString(6, commentStr);
				
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
			}
		}
	}
	// end Setter functions
}
