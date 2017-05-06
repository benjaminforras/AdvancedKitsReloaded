package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("kit|akit|advancedkits|kits|akits")
public class GiveCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public GiveCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("give")
	@CommandPermission("advancedkits.give")
	@CommandCompletion("@kits @players true|false")
	@Syntax("<kitname> <player> [forceuse]")
	public void onGiveCommand(CommandSender sender, Kit kit, Player player, @Optional @Default("false") Boolean forceuse) {

		if (Objects.isNull(player)) {
			sendMessage(sender, getMessage("playerNotFound"));
			return;
		}
		if (player.isDead()) {
			sendMessage(sender, getMessage("playerIsDead"));
			return;
		}

		User user = User.getUser(player.getUniqueId());
		if (!user.isUnlocked(kit)) {
			user.addToUnlocked(kit);
			user.save();
			sendMessage(sender, getMessage("successfullyGiven", kit.getName(), player.getName()));
		} else {
			sendMessage(sender, getMessage("giveAlreadyUnlocked", kit.getName()));
			return;
		}

		if (forceuse) {
			Bukkit.dispatchCommand(player, "advancedkitsreloaded:kit use " + kit.getName());
		}
	}
}
