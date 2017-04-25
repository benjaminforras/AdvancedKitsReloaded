package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class CreateCommand
{
	@CommandManager.Cmd(cmd = "create", help = "Create kit", longhelp = "This command opens up a gui where you can create kits.", permission = "create", args = "<kitname>", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished createCommand(CommandSender sender, Object[] args)
	{
		Player player = (Player) sender;
		if (Objects.nonNull(KitManager.getKit(String.valueOf(args[0]), player.getWorld().getName()))) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitAlreadyExists"));
			return CommandManager.CommandFinished.DONE;
		}
		Kit kit = new Kit(String.valueOf(args[0]));

		if (player.getInventory().getContents().length == 0) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("emptyInventory"));
			return CommandManager.CommandFinished.DONE;
		}

		PlayerInventory playerInventory = player.getInventory();
		kit.setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		if (player.getInventory().getArmorContents().length != 0) {
			kit.setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		}

		kit.save();
		KitManager.getKits().add(kit);

		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyCreated", kit.getName()));
		return CommandManager.CommandFinished.DONE;
	}
}
