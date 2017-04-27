package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.KitManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand
{
	private static AdvancedKitsMain instance = AdvancedKitsMain.advancedKits;

	@CommandManager.Cmd(cmd = "reload", help = "Reload kits", longhelp = "Reload the configuration.", permission = "reload", only = CommandManager.CommandOnly.ALL)
	public static CommandManager.CommandFinished reloadCommand(CommandSender sender, Object[] args)
	{
		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Starting to reload configuration.");
		instance.loadConfiguration();
		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Done reloading the configuration.");

		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Loading KitManager.");
		KitManager.loadKits();
		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Done loading KitManager.");

		return CommandManager.CommandFinished.DONE;
	}
}
