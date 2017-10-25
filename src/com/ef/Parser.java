package com.ef;

import java.sql.Connection;
import java.util.ArrayList;

import com.db.DatabaseConnection;
import com.db.DatabaseServices;

public class Parser {
	public static boolean isDebug = false;
	private static String ConfigFilePath = "config.properties";
	private static String AccessLogFilePath = "D:\\access.log";
	private static boolean ShouldLoadLog = false;
	
	private static String startDate = "2017-01-01.00:00:00";
	private static String duration = "daily";
	private static int threshold = 500;
	private static int durationInt = 1;
	
	public static void main(String[] args) {
		if (!areArgumentsValid(args))
			return;
		
		if (isDebug) {
			System.out.println("AccessLogFilePath: "+AccessLogFilePath);
			System.out.println("startDate: "+startDate);
			System.out.println("duration: "+duration);
			System.out.println("threshold: "+threshold);
		}
		
		// TODO Auto-generated method stub
		DatabaseConnection dbConnection = new DatabaseConnection();
		dbConnection.init(ConfigFilePath);
		Connection connection = dbConnection.connectAndCreate();
		DatabaseServices dbServices = new DatabaseServices();
		dbServices.checkDbValid(connection);
		
		// we should load log only if there is a correct argument
		if (ShouldLoadLog)
			dbServices.loadAccessLogToTable(connection, AccessLogFilePath);
		
		if (duration.equals("hourly"))
			durationInt = 1;
		else if (duration.equals("daily"))
			durationInt = 24;
		
		// start finding IPs which should be blocked following the given conditions
		ArrayList<BlockedIP> listBlockedIPs = null;
		listBlockedIPs = dbServices.findingBlockedIPs(connection, startDate, durationInt, threshold);
		// now, we will add all blocked IPs to log table
		dbServices.insertBlockedIPToTable(connection, listBlockedIPs, startDate, durationInt, threshold);
		
		System.out.println("List of Blocked IPs:");
		for (int i=0; i<listBlockedIPs.size(); i++) {
			BlockedIP blockedIP = listBlockedIPs.get(i);
			
			System.out.println(i + ": IP = '" + blockedIP.IP + "', Total of Requests = " + blockedIP.TotalRequest);
		}
		
		dbConnection.closeConnection(connection);
	}

	public static boolean areArgumentsValid (String[] args) {
		ShouldLoadLog = false;
		
		for (int i=0; i<args.length; i++) {
//			System.out.println(args[i]);
			String[] arg = args[i].split("=");
			if (arg.length != 2)
				continue;
			
			if (arg[0].equals("--accesslog") && !arg[1].equals("")) {
				AccessLogFilePath = arg[1];
				ShouldLoadLog = true;
				continue;
			}
			if (arg[0].equals("--startDate")) {
				startDate = arg[1];
				continue;
			}
			if (arg[0].equals("--duration")) {
				duration = arg[1];
				if (duration.equals("hourly") || duration.equals("daily")) {
					continue;
				} else {
					System.out.println("Error! \"duration\" can take only \"hourly\" or \"daily\" as inputs");
					return false;
				}
			}
			if (arg[0].equals("--threshold")) {
				try {
					threshold = Integer.parseInt(arg[1]);
				} catch (Exception e) {
					System.out.println("Error! threshold value should be an integer");
					return false;
				}
				continue;
			}
		}
		
		return true;
	}
}
