package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import org.bukkit.command.CommandSender;

import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;

@CommandAlias("kit|akit|advancedkits|kits|akits")
public class ReloadCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public ReloadCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("reload")
	@CommandPermission("advancedkits.reload")
	public void onReloadCommand(CommandSender sender) {
		sendMessage(sender, "Starting to reload configuration.");
		Config.loadConfigurationValues(instance);
		sendMessage(sender, "Done reloading the configuration.");

		sendMessage(sender, "Loading KitManager.");
		instance.getKitManager().loadKits();
		sendMessage(sender, "Done loading KitManager.");
	}
}
