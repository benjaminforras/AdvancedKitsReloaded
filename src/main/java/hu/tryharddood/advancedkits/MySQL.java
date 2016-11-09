package hu.tryharddood.advancedkits;

/*****************************************************
 *              Created by TryHardDood on 2016. 10. 14..
 ****************************************************/

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {

	private String     host;
	private Integer    port;
	private String     database;
	private String     username;
	private String     password;
	private Connection con;

	public MySQL(String host, Integer port, String database, String username, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	public void connect() {
		if (!isConnected())
		{
			Bukkit.getScheduler().runTaskAsynchronously(AdvancedKits.getInstance(), () ->
			{
				try
				{
					AdvancedKits.log(ChatColor.GREEN + "Starting to connect to MYSQL. This could take some time.");
					con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
					AdvancedKits.log(ChatColor.GREEN + "MySQL - Successfully connected to the database!");
					performFirstConnectionCommands();
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			});
		}
	}

	public void disconnect() {
		if (isConnected())
		{
			try
			{
				con.close();
				AdvancedKits.log(ChatColor.GREEN + "MySQL - Successfully disconnected from the database!");
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void performFirstConnectionCommands() throws SQLException {
		PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS AdvancedKitsReloaded (UUID CHAR(36), UNLOCKED TEXT, PRIMARY KEY (UUID)) ENGINE = InnoDB;");
		ps.executeUpdate();
	}

	public boolean isConnected() {
		return (con != null);
	}

	public Connection getConnection() {
		return con;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
