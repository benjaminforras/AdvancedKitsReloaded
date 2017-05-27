package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
@CommandAlias("kit|akit|advancedkits|kits|akits")
public class MainCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public MainCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}


	@Default
	public void onDefault(CommandSender sender) {
		Player player = sender instanceof Player ? (Player) sender : null;
		if (Objects.isNull(player)) onHelp(sender);
		else Bukkit.dispatchCommand(player, "kit view");
	}

	@Subcommand("help")
	public void onHelp(CommandSender sender) {
		ArrayList<String> helpList = new ArrayList<>();
		sender.sendMessage(ChatColor.YELLOW + "--------- " + ChatColor.WHITE + Config.CHAT_PREFIX + " Help " + ChatColor.YELLOW + " ---------------------");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "use <kitname>" + ": " + ChatColor.WHITE + "Uses the free or bought kit.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "view <kitname>" + ": " + ChatColor.WHITE + "Views the kit items and armor.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "create <kitname>" + ": " + ChatColor.WHITE + "Creates the kit with the items and armor in your inventory");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "delete <kitname>" + ": " + ChatColor.WHITE + "Deletes the kit");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "edit <kitname>" + ": " + ChatColor.WHITE + "Edits the kit.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "flag <kitname> <flag> <value> [world]" + ": " + ChatColor.WHITE + "Sets a flag.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "give <kitname> <player> [forceuse]" + ": " + ChatColor.WHITE + "Gives the kit to the player.");
		helpList.add(ChatColor.GOLD + "/kit" + " " + "reload" + ": " + ChatColor.WHITE + "Reloads the kits and the configuration");

		sender.sendMessage(helpList.toArray(new String[helpList.size()]));
	}
}