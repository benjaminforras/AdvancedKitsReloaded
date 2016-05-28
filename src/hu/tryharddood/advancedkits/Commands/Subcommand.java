package hu.tryharddood.advancedkits.Commands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Utils.Title;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public abstract class Subcommand
{
    public static void sendMessage(CommandSender commandSender, String message, ChatColor color)
    {
        commandSender.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + message);

        if (commandSender instanceof Player)
        {
            Title.sendTitle((Player) commandSender, 2, 20, 2, "", color + message);
        }
    }

    protected static void closeGUI(Player player, String name)
    {
        if (player.getOpenInventory().getTitle().contains(name))
        {
            player.closeInventory();
        }
    }

    public abstract String getPermission();

    public abstract String getUsage();

    public abstract String getDescription();

    public abstract int getArgs();

    public abstract boolean playerOnly();

    public abstract void onCommand(CommandSender sender, Command cmd, String label, String[] args);

    public boolean runCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.hasPermission(getPermission()))
        {
            sendMessage(sender, tl("error_no_permission"), ChatColor.RED);
        }
        else if (getArgs() != -1 && getArgs() != args.length)
        {
            sender.sendMessage(tl("chat_usage") + ":");
            sender.sendMessage(ChatColor.GREEN + getUsage() + ChatColor.GRAY + " - " + ChatColor.BLUE + getDescription());
        }
        else
        {
            if (playerOnly())
            {
                if (sender instanceof Player)
                {
                    onCommand(sender, cmd, label, args);
                }
                else
                {
                    sender.sendMessage(tl("error_only_player"));
                }
            }
            else
            {
                onCommand(sender, cmd, label, args);
            }
        }

        return true;
    }

    protected boolean isNumeric(String s)
    {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    protected boolean isDouble(String s)
    {
        try
        {
            Double.parseDouble(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    protected String getArgString(String[] args, int start)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++)
        {
            sb.append(args[i]).append(" ");
        }

        return sb.toString().trim();
    }
}
