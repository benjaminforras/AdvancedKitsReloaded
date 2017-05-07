package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.kits.flags.InvalidFlagValueException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("kit|akit|advancedkits|kits|akits")
public class FlagCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public FlagCommand(AdvancedKitsMain instance)
	{
		this.instance = instance;
	}

	@Subcommand("flag")
	@CommandPermission("advancedkits.flag")
	@CommandCompletion("@kits @flags")
	@Syntax("<kitname> <flag> <value> [world]")
	public void onFlagCommand(CommandSender sender, Kit kit, Flag flag, String value, @Optional String world) {

		String   tempValue     = String.join(" ", value, Objects.isNull(world) ? "" : world);
		String[] splittedValue = tempValue.split(" ");

		if (Objects.nonNull(Bukkit.getWorld(splittedValue[splittedValue.length - 1]))) {
			world = splittedValue[splittedValue.length - 1];
			value = String.join(" ", Arrays.copyOf(splittedValue, splittedValue.length - 1));
		} else {
			world = "global";
			value = String.join(" ", splittedValue);
		}

		if (value.equalsIgnoreCase("hand")) {
			Player player = sender instanceof Player ? (Player)sender : null;
			if(Objects.isNull(player))
			{
				sendMessage(sender, getMessage("playerOnly"));
				return;
			}

			if (flag.getName().equalsIgnoreCase("firework")) {
				if (Objects.isNull(player.getInventory().getItemInMainHand()) || !player.getInventory().getItemInMainHand().getType().equals(Material.FIREWORK)) {
					sendMessage(player, getMessage("notFirework"));
					return;
				}

				try {
					kit.setFlag(flag, world, flag.parseItem(player));
				} catch (InvalidFlagValueException e) {
					player.sendMessage(e.getMessages());
					return;
				}

				sendMessage(player, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
				return;
			} else if (flag.getName().equalsIgnoreCase("icon")) {
				if (Objects.isNull(player.getInventory().getItemInMainHand()) || player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
					sendMessage(player, getMessage("notValidIcon"));
					return;
				}
				try {
					kit.setFlag(flag, world, flag.parseItem(player));
				} catch (InvalidFlagValueException e) {
					player.sendMessage(e.getMessages());
					return;
				}

				sendMessage(player, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
				return;
			}
		}

		if (flag.getName().equalsIgnoreCase("firework") || flag.getName().equalsIgnoreCase("icon")) {
			sendMessage(sender, ChatColor.GRAY + "Usage: /kit flag <kitname> <flag> hand");
			return;
		}

		try {
			kit.setFlag(flag, world, flag.parseInput(value));
		} catch (InvalidFlagValueException e) {
			sender.sendMessage(e.getMessages());
			return;
		}

		sendMessage(sender, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
	}
}
