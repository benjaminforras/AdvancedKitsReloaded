package hu.tryharddood.advancedkits.Utils;

import hu.tryharddood.advancedkits.AdvancedKits;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*****************************************************
 *
 *              MessagesAPI
 *                      by ConnorLinfoot {@see https://github.com/ConnorLinfoot/MessagesAPI/}
 **
 *
 ****************************************************/
public class MessagesAPI {
	public static String nmsver;
	private static boolean useOldMethods = false;

	@Deprecated
	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
		sendTitle(player, fadeIn, stay, fadeOut, message, null);
	}

	@Deprecated
	public static void sendSubtitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
		sendTitle(player, fadeIn, stay, fadeOut, null, message);
	}

	@Deprecated
	public static void sendFullTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
	}

	@Deprecated
	public static Integer getPlayerProtocol(Player player) {
		/* Returns the 1.8 protocol version as this is the only protocol a player can possibly be on with Spigot 1.8 */
		return 47;
	}

	private static void sendPacket(Player player, Object packet) {
		try
		{
			Object handle           = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Class<?> getNMSClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try
		{
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void sendTitle(Player player, String title, String subtitle) {
		sendTitle(player, 4, 23, 4, title, subtitle);
	}

	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		try
		{
			Object      e;
			Object      chatTitle;
			Object      chatSubtitle;
			Constructor subtitleConstructor;
			Object      titlePacket;
			Object      subtitlePacket;

			if (title != null)
			{
				title = ChatColor.translateAlternateColorCodes('&', title);
				title = title.replaceAll("%player%", player.getDisplayName());
				// Times packets
				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
				chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
				//subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				//titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle, fadeIn, stay, fadeOut});
				titlePacket = subtitleConstructor.newInstance(e, chatTitle, fadeIn, stay, fadeOut);
				sendPacket(player, titlePacket);

				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
				chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
				//subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent")});
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"));
				//titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle});
				titlePacket = subtitleConstructor.newInstance(e, chatTitle);
				sendPacket(player, titlePacket);
			}

			if (subtitle != null)
			{
				subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
				subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
				// Times packets
				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
				chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
				sendPacket(player, subtitlePacket);

				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
				chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + subtitle + "\"}");
				//subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				//subtitlePacket = subtitleConstructor.newInstance(new Object[]{e, chatSubtitle, fadeIn, stay, fadeOut});
				subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
				sendPacket(player, subtitlePacket);
			}
		} catch (Exception var11)
		{
			var11.printStackTrace();
		}
	}

	public static void clearTitle(Player player) {
		sendTitle(player, 0, 0, 0, "", "");
	}

	public static void sendTabTitle(Player player, String header, String footer) {
		if (header == null) header = "";
		header = ChatColor.translateAlternateColorCodes('&', header);

		if (footer == null) footer = "";
		footer = ChatColor.translateAlternateColorCodes('&', footer);

		header = header.replaceAll("%player%", player.getDisplayName());
		footer = footer.replaceAll("%player%", player.getDisplayName());

		try
		{
			Object         tabHeader        = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + header + "\"}");
			Object         tabFooter        = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + footer + "\"}");
			Constructor<?> titleConstructor = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(getNMSClass("IChatBaseComponent"));
			Object         packet           = titleConstructor.newInstance(tabHeader);
			Field          field            = packet.getClass().getDeclaredField("b");
			field.setAccessible(true);
			field.set(packet, tabFooter);
			sendPacket(player, packet);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void sendActionBar(Player player, String message) {
		try
		{
			Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
			Object   p  = c1.cast(player);
			Object   ppoc;
			Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
			if (useOldMethods)
			{
				Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
				Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				//Method   m3  = c2.getDeclaredMethod("a", new Class<?>[]{String.class});
				Method m3  = c2.getDeclaredMethod("a", String.class);
				Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
				//ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(new Object[]{cbc, (byte) 2});
				ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(cbc, (byte) 2);
			}
			else
			{
				Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
				Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				//Object o = c2.getConstructor(new Class<?>[]{String.class}).newInstance(new Object[]{message});
				//ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(new Object[]{o, (byte) 2});
				Object o = c2.getConstructor(new Class<?>[]{String.class}).newInstance(message);
				ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(o, (byte) 2);
			}
			//Method m1 = c1.getDeclaredMethod("getHandle", new Class<?>[]{});
			Method m1 = c1.getDeclaredMethod("getHandle");
			Object h  = m1.invoke(p);
			Field  f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			//Method m5 = pc.getClass().getDeclaredMethod("sendPacket", new Class<?>[]{c5});
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void sendActionBar(final Player player, final String message, int duration) {
		sendActionBar(player, message);

		if (duration >= 0)
		{
			// Sends empty message at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
			new BukkitRunnable() {
				@Override
				public void run() {
					sendActionBar(player, "");
				}
			}.runTaskLater(AdvancedKits.getInstance(), duration + 1);
		}

		// Re-sends the messages every 3 seconds so it doesn't go away from the player's screen.
		while (duration > 60)
		{
			duration -= 60;
			int sched = duration % 60;
			new BukkitRunnable() {
				@Override
				public void run() {
					sendActionBar(player, message);
				}
			}.runTaskLater(AdvancedKits.getInstance(), (long) sched);
		}
	}

	public static void sendActionBarToAllPlayers(String message) {
		sendActionBarToAllPlayers(message, -1);
	}

	public static void sendActionBarToAllPlayers(String message, int duration) {
		for (Player p : Bukkit.getOnlinePlayers())
		{
			sendActionBar(p, message, duration);
		}
	}

	public void onEnable() {
		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		if (nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.equalsIgnoreCase("v1_7_"))
		{ // Not sure if 1_7 works for the protocol hack?
			useOldMethods = true;
		}
	}
}