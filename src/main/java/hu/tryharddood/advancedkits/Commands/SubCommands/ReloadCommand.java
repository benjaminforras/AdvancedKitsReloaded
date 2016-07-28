package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class ReloadCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.RELOAD_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit reload";
	}

	@Override
	public String getDescription() {
		return "Reloads the plugin's configuration.";
	}

	@Override
	public int getArgs() {
		return 1;
	}

	@Override
	public boolean playerOnly() {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		AdvancedKits.getConfiguration().load();
		AdvancedKits.getKitManager().load();
		sender.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kit_reload"));
	}
}
