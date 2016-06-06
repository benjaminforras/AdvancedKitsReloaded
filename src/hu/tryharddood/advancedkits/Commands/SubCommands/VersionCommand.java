package advancedkits.Commands.SubCommands;

import advancedkits.AdvancedKits;
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
public class VersionCommand extends Subcommand
{
	@Override
	public String getPermission()
	{
		return Variables.KIT_USE_PERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/kit version";
	}

	@Override
	public String getDescription()
	{
		return "Displays the informations about the plugin";
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
		sender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Plugin:         " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getName());
		sender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Version:       " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getVersion());
		sender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Author(s):    " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getAuthors());
		sender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Website:       " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getWebsite());
	}
}
