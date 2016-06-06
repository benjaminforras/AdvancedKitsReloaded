package advancedkits.Commands.SubCommands;

import advancedkits.AdvancedKits;
import advancedkits.Commands.Subcommand;
import advancedkits.Kits.Kit;
import advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class DeleteCommand extends Subcommand
{
	@Override
	public String getPermission()
	{
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/kit delete <kit>";
	}

	@Override
	public String getDescription()
	{
		return "Deletes a kit.";
	}

	@Override
	public int getArgs()
	{
		return 2;
	}

	@Override
	public boolean playerOnly()
	{
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Kit kit = AdvancedKits.getKitManager().getKit(args[1]);
		if (kit == null)
		{
			sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		AdvancedKits.getKitManager().deleteKit(kit);
		sender.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + tl("kit_delete"));

		if (sender instanceof Player)
		{
			closeGUI((Player) sender, "Details");
		}
	}
}
