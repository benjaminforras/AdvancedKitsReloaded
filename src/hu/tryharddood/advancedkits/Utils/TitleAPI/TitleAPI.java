package hu.tryharddood.advancedkits.Utils.TitleAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class TitleAPI extends JavaPlugin implements Listener
{

    public static void clearTitle(Player player)
    {
        sendTitle(player, 0, 0, 0, "", "");
    }

    public static Class<?> getNMSClass(String name)
    {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try
        {
            return Class.forName("net.minecraft.server." + version + "." + name);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    public static void sendFullTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle)
    {
        sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
    }

    public static void sendPacket(Player player, Object packet)
    {
        try
        {
            Object handle           = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static void sendSubtitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message)
    {
        sendTitle(player, fadeIn, stay, fadeOut, null, message);
    }

    public static void sendTabTitle(Player player, String header, String footer)
    {
        if (header == null) header = "";
        header = ChatColor.translateAlternateColorCodes('&', header);

        if (footer == null) footer = "";
        footer = ChatColor.translateAlternateColorCodes('&', footer);

        TabTitleSendEvent tabTitleSendEvent = new TabTitleSendEvent(player, header, footer);
        Bukkit.getPluginManager().callEvent(tabTitleSendEvent);
        if (tabTitleSendEvent.isCancelled()) return;

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
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Deprecated
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message)
    {
        sendTitle(player, fadeIn, stay, fadeOut, message, null);
    }

    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle)
    {
        TitleSendEvent titleSendEvent = new TitleSendEvent(player, title, subtitle);
        Bukkit.getPluginManager().callEvent(titleSendEvent);
        if (titleSendEvent.isCancelled()) return;

        try
        {
            Object         e;
            Object         chatTitle;
            Object         chatSubtitle;
            Constructor<?> subtitleConstructor;
            Object         titlePacket;
            Object         subtitlePacket;

            if (title != null)
            {
                title = ChatColor.translateAlternateColorCodes('&', title);
                title = title.replaceAll("%player%", player.getDisplayName());
                // Times packets
                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
                chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
                titlePacket = subtitleConstructor.newInstance(e, chatTitle, fadeIn, stay, fadeOut);
                sendPacket(player, titlePacket);

                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
                chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"));
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
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
                subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
                sendPacket(player, subtitlePacket);
            }
        }
        catch (Exception var11)
        {
            var11.printStackTrace();
        }
    }

    @Override
    public void onEnable()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
        Server               server  = getServer();
        ConsoleCommandSender console = server.getConsoleSender();
        console.sendMessage(ChatColor.AQUA + getDescription().getName() + " V" + getDescription().getVersion() + " has been enabled!");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (getConfig().getBoolean("Title On Join"))
        {
            sendTitle(event.getPlayer(), 20, 50, 20, getConfig().getString("Title Message"), getConfig().getString("Subtitle Message"));
        }

        if (getConfig().getBoolean("Tab Header Enabled"))
        {
            sendTabTitle(event.getPlayer(), getConfig().getString("Tab Header Message"), getConfig().getString("Tab Footer Message"));
        }
    }

}
