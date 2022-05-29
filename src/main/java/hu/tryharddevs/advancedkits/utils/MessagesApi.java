package hu.tryharddevs.advancedkits.utils;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/*****************************************************
 *
 *              MessagesAPI
 *                      by ConnorLinfoot {@see https://github.com/ConnorLinfoot/MessagesAPI/}
 **
 *
 ****************************************************/
public class MessagesApi {
	private static final OBCClassResolver obcClassResolver = new OBCClassResolver();
	private static final NMSClassResolver nmsClassResolver = new NMSClassResolver();

	private static void sendPacket(Player player, Object packet) {
		try {
			Object handle           = new MethodResolver(player.getClass()).resolveSilent("getHandle").invoke(player);
			Object playerConnection = new FieldResolver(handle.getClass()).resolveSilent("playerConnection").get(handle);
			new MethodResolver(playerConnection.getClass()).resolveSilent(new ResolverQuery("sendPacket", nmsClassResolver.resolveSilent("Packet"))).invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendTitle(Player player, String title, String subtitle) {
		sendTitle(player, 4, 23, 4, title, subtitle);
	}

	private static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		try {
			Object      e;
			Object      chatTitle;
			Object      chatSubtitle;
			Constructor subtitleConstructor;
			Object      titlePacket;
			Object      subtitlePacket;

			if (title != null) {
				title = ChatColor.translateAlternateColorCodes('&', title);
				title = title.replaceAll("%player%", player.getDisplayName());
				// Times packets
				e = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
				chatTitle = nmsClassResolver.resolveSilent("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
				subtitleConstructor = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getConstructor(nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0], nmsClassResolver.resolveSilent("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				titlePacket = subtitleConstructor.newInstance(e, chatTitle, fadeIn, stay, fadeOut);
				sendPacket(player, titlePacket);

				e = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
				chatTitle = nmsClassResolver.resolveSilent("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
				subtitleConstructor = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getConstructor(nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0], nmsClassResolver.resolveSilent("IChatBaseComponent"));
				titlePacket = subtitleConstructor.newInstance(e, chatTitle);
				sendPacket(player, titlePacket);
			}

			if (subtitle != null) {
				subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
				subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
				// Times packets
				e = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
				chatSubtitle = nmsClassResolver.resolveSilent("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
				subtitleConstructor = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getConstructor(nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0], nmsClassResolver.resolveSilent("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
				sendPacket(player, subtitlePacket);

				e = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
				chatSubtitle = nmsClassResolver.resolveSilent("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle + "\"}");
				subtitleConstructor = nmsClassResolver.resolveSilent("PacketPlayOutTitle").getConstructor(nmsClassResolver.resolveSilent("PacketPlayOutTitle").getDeclaredClasses()[0], nmsClassResolver.resolveSilent("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
				sendPacket(player, subtitlePacket);
			}
		} catch (Exception var11) {
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

		try {
			Object         tabHeader        = nmsClassResolver.resolveSilent("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + header + "\"}");
			Object         tabFooter        = nmsClassResolver.resolveSilent("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + footer + "\"}");
			Constructor<?> titleConstructor = nmsClassResolver.resolveSilent("PacketPlayOutPlayerListHeaderFooter").getConstructor(nmsClassResolver.resolveSilent("IChatBaseComponent"));
			Object         packet           = titleConstructor.newInstance(tabHeader);
			Field          field            = packet.getClass().getDeclaredField("b");
			field.setAccessible(true);
			field.set(packet, tabFooter);
			sendPacket(player, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void sendActionBar(Player player, String message) {
		try {
			Class<?> c1 = obcClassResolver.resolveSilent("entity.CraftPlayer");
			Object   p  = c1.cast(player);
			Object   ppoc;
			Class<?> c4 = nmsClassResolver.resolveSilent("PacketPlayOutChat");
			Class<?> c5 = nmsClassResolver.resolveSilent("Packet");
			Class<?> c2 = nmsClassResolver.resolveSilent("ChatComponentText");
			Class<?> c3 = nmsClassResolver.resolveSilent("IChatBaseComponent");
			Object   o  = c2.getConstructor(new Class<?>[]{String.class}).newInstance(message);
			ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(o, (byte) 2);
			Method m1 = c1.getDeclaredMethod("getHandle");
			Object h  = m1.invoke(p);
			Field  f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void sendActionBar(final Player player, final String message, int duration) {
		sendActionBar(player, message);

		if (duration >= 0) {
			// Sends empty message at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
			new BukkitRunnable() {
				@Override public void run() {
					sendActionBar(player, "");
				}
			}.runTaskLater(AdvancedKitsMain.getPlugin(), duration + 1);
		}

		// Re-sends the messages every 3 seconds so it doesn't go away from the player's screen.
		while (duration > 60) {
			duration -= 60;
			int sched = duration % 60;
			new BukkitRunnable() {
				@Override public void run() {
					sendActionBar(player, message);
				}
			}.runTaskLater(AdvancedKitsMain.getPlugin(), (long) sched);
		}
	}

	public static void sendActionBarToAllPlayers(String message) {
		sendActionBarToAllPlayers(message, -1);
	}

	private static void sendActionBarToAllPlayers(String message, int duration) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			sendActionBar(p, message, duration);
		}
	}

	public static void sendMessage(CommandSender sender, String... messages) {
		Arrays.stream(messages).forEach(message -> sender.sendMessage(Config.CHAT_PREFIX + " " + message));
	}
}