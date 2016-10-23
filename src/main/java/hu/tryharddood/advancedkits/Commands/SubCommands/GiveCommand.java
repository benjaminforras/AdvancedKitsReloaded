package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;

/*****************************************************
 *              Created by TryHardDood on 2016. 08. 07..
 ****************************************************/
public class GiveCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.KIT_GIVE_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit give <player> <kit>";
	}

	@Override
	public String getDescription() {
		return "It gives a kit to the selected player";
	}

	@Override
	public int getArgs() {
		return 3;
	}

	@Override
	public boolean playerOnly() {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String targetPlayer = args[1];
		Kit    kit          = AdvancedKits.getKitManager().getKit(args[2]);
		if (kit == null)
		{
			sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayer);
		if (offlinePlayer == null || !offlinePlayer.isOnline())
		{
			sendMessage(sender, tl("error_player_not_found"), ChatColor.RED);
			return;
		}

		UseCommand.GiveItems(Bukkit.getPlayer(offlinePlayer.getUniqueId()), kit, true);
		sendMessage(sender, tl("success_give_kit", kit.getName(), offlinePlayer.getName()), ChatColor.GREEN);
	}
}
