package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import hu.tryharddevs.advancedkits.utils.MessagesApi;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.Collections;
import java.util.Objects;

import static hu.tryharddevs.advancedkits.commands.UseCommand.getEmptySpaces;
import static hu.tryharddevs.advancedkits.commands.UseCommand.hasInventorySpace;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class GiveCommand
{
	@CommandManager.Cmd(cmd = "give", help = "Give kits", longhelp = "This command opens up a gui where you can give kits.", permission = "give", args = "<kit> <player> [forceuse]", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished giveCommand(CommandSender sender, Object[] args)
	{
		Player  player   = (Player) sender;
		String  world    = player.getWorld().getName();
		Boolean forceUse = false;

		if (args.length > 2) {
			forceUse = Boolean.parseBoolean(String.valueOf(args[2]));
		}

		Kit kit = KitManager.getKit(String.valueOf(args[0]), world);
		if (Objects.isNull(kit)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return CommandManager.CommandFinished.DONE;
		}

		Player target = Bukkit.getPlayer(String.valueOf(args[1]));
		if (Objects.isNull(target)) {
			sender.sendMessage("No player found."); //TODO!
			return CommandManager.CommandFinished.DONE;
		}

		if (target.isDead()) {
			sender.sendMessage("Player dead."); //TODO!
			return CommandManager.CommandFinished.DONE;
		}
		User user = User.getUser(target.getUniqueId());

		if (kit.getFlag(DISABLEDWORLDS, world).contains(player.getWorld().getName())) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseWorld"));
			return CommandManager.CommandFinished.DONE;
		}

		if (forceUse && kit.getFlag(MAXUSES, world) != 0) {
			if (user.getTimesUsed(kit, world) >= kit.getFlag(MAXUSES, world)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseNoMore"));
				target.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseNoMore"));
				return CommandManager.CommandFinished.DONE;
			}
		}

		if (forceUse && kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			if (!user.checkDelay(kit, world)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseDelay", user.getDelay(kit, world)));
				target.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseDelay", user.getDelay(kit, world)));
				return CommandManager.CommandFinished.DONE;
			}
		}

		if (kit.getFlag(PERUSECOST, world) != 0) {
			EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(PERUSECOST, world));
			if (r.transactionSuccess()) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
				target.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
			}
			else {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
				target.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
				return CommandManager.CommandFinished.DONE;
			}
		}

		PlayerInventory playerInventory = target.getInventory();

		int freeSpace = getEmptySpaces(target);
		int spaceneed = kit.getItems().size();

		if (kit.getFlag(AUTOEQUIPARMOR, world)) {
			spaceneed += target.getInventory().getArmorContents().length;
		}
		if (!kit.getFlag(SPEWITEMS, world) && spaceneed > freeSpace) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughSpace"));
			target.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughSpace"));
			return CommandManager.CommandFinished.DONE;
		}
		//ItemStack[] storage = playerInventory.getStorageContents();
		ItemStack[] equipment = playerInventory.getArmorContents();

		if (kit.getFlag(CLEARINVENTORY, world)) {
			target.getInventory().clear();
			target.getEquipment().clear();
		}

		kit.getItems().forEach(itemStack -> {
			if (hasInventorySpace(target, itemStack)) {
				target.getInventory().addItem(itemStack);
			}
			else if (kit.getFlag(SPEWITEMS, world)) {
				target.getWorld().dropItemNaturally(target.getLocation(), itemStack);
			}
		});

		// AutoEquip
		if (kit.getFlag(AUTOEQUIPARMOR, world)) {
			for (ItemStack armor : equipment) { //If player had prev armor on
				if (Objects.isNull(armor)) continue;

				if (hasInventorySpace(target, armor)) {
					playerInventory.addItem(armor); // add it to his inventory
				}
				else if (kit.getFlag(SPEWITEMS, world)) {
					target.getWorld().dropItemNaturally(target.getLocation(), armor); // Or drop it
				}
			}

			//Equip armor
			kit.getArmors().forEach(itemStack -> {
				if (ItemStackUtil.isHelmet(itemStack)) {
					playerInventory.setHelmet(itemStack);
				}
				else if (ItemStackUtil.isChest(itemStack)) {
					playerInventory.setChestplate(itemStack);
				}
				else if (ItemStackUtil.isLegs(itemStack)) {
					playerInventory.setLeggings(itemStack);
				}
				else if (ItemStackUtil.isBoots(itemStack)) {
					playerInventory.setBoots(itemStack);
				}

				if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) && ItemStackUtil.isShield(itemStack)) {
					playerInventory.setItemInOffHand(itemStack);
				}
			});
		}
		else {
			for (ItemStack armor : kit.getArmors()) { //Add items to the player's inventory.
				if (hasInventorySpace(target, armor)) {
					playerInventory.addItem(armor);
				}
				else if (kit.getFlag(SPEWITEMS, world)) {
					target.getWorld().dropItemNaturally(target.getLocation(), armor);
				}
			}
		}

		String temp;
		for (String command : kit.getFlag(COMMANDS, world)) {
			temp = (AdvancedKitsMain.usePlaceholderAPI ? PlaceholderAPI.setPlaceholders(target, command) : command.replace("%player_name%", target.getName()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp);
		}

		for (String message : kit.getFlag(MESSAGES, world)) {
			temp = (AdvancedKitsMain.usePlaceholderAPI ? PlaceholderAPI.setPlaceholders(target, message) : message.replace("%player_name%", target.getName()));

			if (temp.contains("subtitle:")) {
				MessagesApi.sendTitle(target, "", temp.replace("subtitle:", ""));
			}
			else if (temp.contains("title:")) {
				MessagesApi.sendTitle(target, temp.replace("title:", ""), "");
			}
			else if (temp.contains("actionbar:")) {
				MessagesApi.sendActionBar(target, ChatColor.translateAlternateColorCodes('&', temp.replace("actionbar:", "")));
			}
			else {
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', temp));
			}
		}

		for (PotionEffect potionEffect : kit.getFlag(POTIONEFFECTS, world)) {
			target.addPotionEffect(potionEffect);
		}

		for (ParticleEffect particleEffect : kit.getFlag(PARTICLEEFFECTS, world)) {
			particleEffect.send(Collections.singletonList(target), target.getLocation().clone(), 0, 0, 0, 0, 1);
		}

		for (Sound sound : kit.getFlag(SOUNDEFFECTS, world)) {
			target.playSound(target.getLocation().clone(), sound, 1.0F, 1.0F);
		}

		if (kit.getFlag(FIREWORK, world) != null) {
			ItemStack    firework       = kit.getFlag(FIREWORK, world);
			Firework     fireworkEntity = target.getWorld().spawn(target.getLocation().clone(), Firework.class);
			FireworkMeta data           = (FireworkMeta) firework.getItemMeta();
			if (data != null) fireworkEntity.setFireworkMeta(data);
		}

		if (forceUse && kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			user.setDelay(kit, world, kit.getFlag(DELAY, world));
		}

		if (forceUse && kit.getFlag(MAXUSES, world) > 0) user.addUse(kit, world);

		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyUsed", kit.getName()));
		target.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyUsed", kit.getName()));

		return CommandManager.CommandFinished.DONE;
	}
}
