package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import hu.tryharddevs.advancedkits.utils.MessagesApi;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import hu.tryharddevs.advancedkits.utils.menuapi.components.ActionListener;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.components.MenuObject;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@SuppressWarnings("ConstantConditions")
public class UseCommand implements ActionListener
{
	private static UseCommand useInventoryListener = new UseCommand();

	@CommandManager.Cmd(cmd = "use", help = "Use kit", longhelp = "This command opens up a gui where you can use kits.", permission = "use", args = "[kitname]", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished useCommand(CommandSender sender, Object[] args)
	{
		Player player = (Player) sender;
		User   user   = User.getUser(player.getUniqueId());
		String world  = player.getWorld().getName();

		if (args.length == 0) {
			Inventory inventory = Bukkit.createInventory(player, ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9), "AdvancedKitsReborn - Use kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			for (Kit kit : KitManager.getKits()) {
				if (!kit.getFlag(VISIBLE, world)) continue;
				if (!kit.getFlag(FREE, world) && !user.isUnlocked(kit)) continue;

				menuObject = new MenuObject(kit.getFlag(ICON, world), (byte) 0, ChatColor.GREEN + kit.getDisplayName(player.getWorld().getName()), KitManager.getKitDescription(player, kit, world));
				menuObject.setActionListener(useInventoryListener);

				menu.addMenuObject(menuObject);
			}
			menu.openForPlayer(player);

			return CommandManager.CommandFinished.DONE;
		}

		Kit kit = KitManager.getKit(String.valueOf(args[0]), world);
		if (Objects.isNull(kit)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return CommandManager.CommandFinished.DONE;
		}

		if (!player.hasPermission(kit.getPermission())) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("noKitPermission"));
			return CommandManager.CommandFinished.DONE;
		}

		if (kit.getFlag(DISABLEDWORLDS, world).contains(player.getWorld().getName())) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseWorld"));
			return CommandManager.CommandFinished.DONE;
		}

		if (kit.getFlag(MAXUSES, world) != 0) {
			if (user.getTimesUsed(kit, world) >= kit.getFlag(MAXUSES, world)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseNoMore"));
				return CommandManager.CommandFinished.DONE;
			}
		}

		if (kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			if (!user.checkDelay(kit, world)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseDelay", user.getDelay(kit, world)));
				return CommandManager.CommandFinished.DONE;
			}
		}

		if (kit.getFlag(PERUSECOST, world) != 0) {
			EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(PERUSECOST, world));
			if (r.transactionSuccess()) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
			}
			else {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
				return CommandManager.CommandFinished.DONE;
			}
		}

		PlayerInventory playerInventory = player.getInventory();

		int freeSpace = getEmptySpaces(player);
		int spaceneed = kit.getItems().size();

		if (kit.getFlag(AUTOEQUIPARMOR, world)) {
			spaceneed += player.getInventory().getArmorContents().length;
		}
		if (!kit.getFlag(SPEWITEMS, world) && spaceneed > freeSpace) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughSpace"));
			return CommandManager.CommandFinished.DONE;
		}
		//ItemStack[] storage = playerInventory.getStorageContents();
		ItemStack[] equipment = playerInventory.getArmorContents();

		if (kit.getFlag(CLEARINVENTORY, world)) {
			player.getInventory().clear();
			player.getEquipment().clear();
		}

		kit.getItems().forEach(itemStack -> {
			if (hasInventorySpace(player, itemStack)) {
				player.getInventory().addItem(itemStack);
			}
			else if (kit.getFlag(SPEWITEMS, world)) {
				player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
			}
		});

		// AutoEquip
		if (kit.getFlag(AUTOEQUIPARMOR, world)) {
			for (ItemStack armor : equipment) { //If player had prev armor on
				if (Objects.isNull(armor)) continue;

				if (hasInventorySpace(player, armor)) {
					playerInventory.addItem(armor); // add it to his inventory
				}
				else if (kit.getFlag(SPEWITEMS, world)) {
					player.getWorld().dropItemNaturally(player.getLocation(), armor); // Or drop it
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
				if (hasInventorySpace(player, armor)) {
					playerInventory.addItem(armor);
				}
				else if (kit.getFlag(SPEWITEMS, world)) {
					player.getWorld().dropItemNaturally(player.getLocation(), armor);
				}
			}
		}

		String temp;
		for (String command : kit.getFlag(COMMANDS, world)) {
			temp = (AdvancedKitsMain.usePlaceholderAPI ? PlaceholderAPI.setPlaceholders(player, command) : command.replace("%player_name%", player.getName()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp);
		}

		for (String message : kit.getFlag(MESSAGES, world)) {
			temp = (AdvancedKitsMain.usePlaceholderAPI ? PlaceholderAPI.setPlaceholders(player, message) : message.replace("%player_name%", player.getName()));

			if (temp.contains("subtitle:")) {
				MessagesApi.sendTitle(player, "", temp.replace("subtitle:", ""));
			}
			else if (temp.contains("title:")) {
				MessagesApi.sendTitle(player, temp.replace("title:", ""), "");
			}
			else if (temp.contains("actionbar:")) {
				MessagesApi.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', temp.replace("actionbar:", "")));
			}
			else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', temp));
			}
		}

		for (PotionEffect potionEffect : kit.getFlag(POTIONEFFECTS, world)) {
			player.addPotionEffect(potionEffect);
		}

		for (ParticleEffect particleEffect : kit.getFlag(PARTICLEEFFECTS, world)) {
			particleEffect.send(Collections.singletonList(player), player.getLocation().clone(), 0, 0, 0, 0, 1);
		}

		for (Sound sound : kit.getFlag(SOUNDEFFECTS, world)) {
			player.playSound(player.getLocation().clone(), sound, 1.0F, 1.0F);
		}

		if (kit.getFlag(FIREWORK, world) != null) {
			ItemStack    firework       = kit.getFlag(FIREWORK, world);
			Firework     fireworkEntity = player.getWorld().spawn(player.getLocation().clone(), Firework.class);
			FireworkMeta data           = (FireworkMeta) firework.getItemMeta();
			if (data != null) fireworkEntity.setFireworkMeta(data);
		}

		if (kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			user.setDelay(kit, world, kit.getFlag(DELAY, world));
		}

		if (kit.getFlag(MAXUSES, world) > 0) user.addUse(kit, world);

		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyUsed", kit.getName()));
		return CommandManager.CommandFinished.DONE;
	}

	public static boolean hasInventorySpace(Player player, ItemStack item)
	{
		int free = 0;
		for (ItemStack itemStack : player.getInventory().getStorageContents()) {
			if (itemStack == null || itemStack.getType() == Material.AIR) {
				free += item.getMaxStackSize();
			}
			else if (itemStack.isSimilar(item)) {
				free += item.getMaxStackSize() - itemStack.getAmount();
			}
		}
		return free >= item.getAmount();
	}

	public static int getEmptySpaces(Player player)
	{
		return (int) Arrays.stream(player.getInventory().getStorageContents()).filter(item -> Objects.isNull(item) || item.getType() == Material.AIR).count();
	}

	@Override
	public void onClick(ClickType clickType, MenuObject menuObject, Player whoClicked)
	{
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			whoClicked.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}

		Bukkit.dispatchCommand(whoClicked, "kit use " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
		menuObject.setIcon(Material.MINECART, (byte) 0, kit.getDisplayName(whoClicked.getWorld().getName()), KitManager.getKitDescription(whoClicked, kit, whoClicked.getWorld().getName()));
	}
}