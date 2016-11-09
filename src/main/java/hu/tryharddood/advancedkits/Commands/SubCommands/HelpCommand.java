package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Utils.LineUtils;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class HelpCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.KIT_USE_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit help";
	}

	@Override
	public String getDescription() {
		return "Displays all commands";
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
		sender.sendMessage(ChatColor.BLUE + LineUtils.line);
		sender.sendMessage(LineUtils.newline);

		sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Opens up the kit list."));
		sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit version") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Shows the plugin infos."));
		sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit help") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Shows all commands."));
		sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit buy <kit>") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Buys the kit."));
		sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit use <kit>") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Uses the kit."));
		sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit view <kit>") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Shows the kit."));

		if (sender.hasPermission(Variables.KITADMIN_PERMISSION))
		{
			sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit setflag <kit> <flag> [value]") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Sets a flag for a kit."));
			sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit create <kit>") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Create a kit."));
			sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit edit <kit>") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Edit a kit."));
			sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit delete <kit>") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Delete a kit."));
			sender.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, "/kit edititem <name|addlore|dellore|amount|durability>  [value]") + LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.GRAY + "Sets an items variable."));
		}

		sender.sendMessage(LineUtils.newline);
		sender.sendMessage(ChatColor.BLUE + LineUtils.line);
	}
}
