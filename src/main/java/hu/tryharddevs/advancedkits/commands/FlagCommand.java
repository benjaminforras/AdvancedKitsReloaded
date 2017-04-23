package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.kits.flags.InvalidFlagValueException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class FlagCommand
{
	@CommandManager.Cmd(cmd = "flag", help = "Change a flag", longhelp = "This command opens up a gui where you can change flags.", permission = "flag", args = "<kitname> <flag> <value> [world]", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished flagCommand(CommandSender sender, Object[] args)
	{
		Player player = (Player) sender;
		Kit    kit    = KitManager.getKit(String.valueOf(args[0]), player.getWorld().getName());
		if (Objects.isNull(kit)) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return CommandManager.CommandFinished.DONE;
		}
		Flag flag = DefaultFlags.fuzzyMatchFlag(String.valueOf(args[1]));
		if (Objects.isNull(flag)) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("flagNotFound"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("availableFlags", Arrays.stream(DefaultFlags.getFlags()).map(Flag::getName).collect(Collectors.joining(","))));
			return CommandManager.CommandFinished.DONE;
		}
		Object value = args[2];
		String world = "global";
		if (args.length > 3) {
			world = String.valueOf(args[3]);
			if (Objects.isNull(Bukkit.getWorld(world))) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("worldNotFound"));
				return CommandManager.CommandFinished.DONE;
			}
		}

		if (flag.getName().equalsIgnoreCase("firework")) {
			if (Objects.isNull(player.getInventory().getItemInMainHand()) || !player.getInventory().getItemInMainHand().getType().equals(Material.FIREWORK)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notFirework"));
				return CommandManager.CommandFinished.DONE;
			}
			kit.setFlag(flag, world, flag.parseItem(player));
		}
		else {
			try {
				kit.setFlag(flag, world, flag.parseInput(String.valueOf(value)));
			}
			catch (InvalidFlagValueException e) {
				player.sendMessage(e.getMessages());
				return CommandManager.CommandFinished.DONE;
			}
		}

		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("flagSet", flag.getName(), value, kit.getName(), world));
		return CommandManager.CommandFinished.DONE;
	}
}
