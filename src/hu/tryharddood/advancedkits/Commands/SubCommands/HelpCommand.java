package advancedkits.Commands.SubCommands;

import advancedkits.Commands.Subcommand;
import advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class HelpCommand extends Subcommand
{
	@Override
	public String getPermission()
	{
		return Variables.KIT_USE_PERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/kit help";
	}

	@Override
	public String getDescription()
	{
		return "Displays all commands";
	}

	@Override
	public int getArgs()
	{
		return 1;
	}

	@Override
	public boolean playerOnly()
	{
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		sender.sendMessage(ChatColor.BLUE + "======================================");
		sender.sendMessage(ChatColor.GREEN + "");
		sender.sendMessage(ChatColor.WHITE + "/kit                                      " + ChatColor.GRAY + "Opens up the kit list.");
		sender.sendMessage(ChatColor.WHITE + "/kit version                            " + ChatColor.GRAY + "Shows the plugin infos.");
		sender.sendMessage(ChatColor.WHITE + "/kit help                                " + ChatColor.GRAY + "Shows all commands.");
		sender.sendMessage(ChatColor.WHITE + "/kit buy <kit>                           " + ChatColor.GRAY + "Buys the kit.");
		sender.sendMessage(ChatColor.WHITE + "/kit use <kit>                           " + ChatColor.GRAY + "Uses the kit.");
		sender.sendMessage(ChatColor.WHITE + "/kit view <kit>                          " + ChatColor.GRAY + "Shows the kit.");

		if (sender.hasPermission(Variables.KITADMIN_PERMISSION))
		{
			sender.sendMessage(ChatColor.WHITE + "/kit setflag <kit> <flag> [value]     " + ChatColor.GRAY + "Sets a flag for a kit.");
			sender.sendMessage(ChatColor.WHITE + "/kit create <kit>                       " + ChatColor.GRAY + "Create a kit.");
			sender.sendMessage(ChatColor.WHITE + "/kit edit <kit>                           " + ChatColor.GRAY + "Edit a kit.");
			sender.sendMessage(ChatColor.WHITE + "/kit delete <kit>                           " + ChatColor.GRAY + "Delete a kit.");
			sender.sendMessage(ChatColor.WHITE + "/kit edititem <name|addlore|dellore|amount|durability>  [value]     " + ChatColor.GRAY + "Sets an items variable.");
		}

		sender.sendMessage(ChatColor.GREEN + "");
		sender.sendMessage(ChatColor.BLUE + "======================================");
	}
}
