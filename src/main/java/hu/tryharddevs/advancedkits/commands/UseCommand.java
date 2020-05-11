package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import hu.tryharddevs.advancedkits.utils.MessagesApi;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.*;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("%rootcommand")
public class UseCommand extends BaseCommand {

	private static AdvancedKitsMain instance;

	public UseCommand(AdvancedKitsMain instance) {
		UseCommand.instance = instance;
	}

	@Subcommand("use")
	@CommandPermission("advancedkits.use")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onUseCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Config.DISABLED_WORLDS.contains(world)) {
			sendMessage(player, getMessage("kitUseDisabledInWorld"));
			return;
		}

		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("Kits", player);
			cPageInventory.setPages(KitManager.getKits().stream()
			        .filter(_kit -> _kit.getFlag(VISIBLE, world) && (_kit.getFlag(FREE, world) && player.hasPermission(_kit.getPermission()) || user.isUnlocked(_kit)))
			        .sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world).clone()).setName(ChatColor.WHITE + _kit.getDisplayName(world))
			                .setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes().toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;
				if (Arrays.asList(cPageInventory.getBackPage(), cPageInventory.getForwardsPage()).contains(clickedItem))
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				if(_event.getClick().isRightClick()) {
				    Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit view " + clickedKit.getName());
				} else {
				    Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit use " + clickedKit.getName());
				    if(kit.getFlag(KEEPINVENTORYOPEN, world)) {
                        Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit use");
                    }
				}
			});
			return;
		}

		if (!player.hasPermission(kit.getPermission())) {
			sendMessage(player, getMessage("noKitPermission", kit.getPermission()));
			return;
		}

		if (kit.getFlag(DISABLEDWORLDS, world).contains(player.getWorld().getName())) {
			sendMessage(player, getMessage("cantUseWorld"));
			return;
		}

		if (kit.getFlag(MAXUSES, world) != 0) {
			if (user.getTimesUsed(kit, world) >= kit.getFlag(MAXUSES, world)) {
				sendMessage(player, getMessage("cantUseNoMore"));
				return;
			}
		}

		if (kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			if (!user.checkDelay(kit, world)) {
				sendMessage(player, getMessage("cantUseDelay", user.getDelay(kit, world)));
				return;
			}
		}

		if (kit.getFlag(PERUSECOST, world) != 0) {
			EconomyResponse r = instance.getEconomy().withdrawPlayer(player, kit.getFlag(PERUSECOST, world));
			if (r.transactionSuccess()) {
				sendMessage(player, getMessage("moneyLowered", instance.getEconomy().format(r.balance), instance.getEconomy().format(r.amount), "PerUseCost"));
			} else {
				sendMessage(player, getMessage("notEnoughMoney", instance.getEconomy().format(r.amount)));
				return;
			}
		}

		if (giveKitToPlayer(player, kit)) {
			if (kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
				user.setDelay(kit, world, kit.getFlag(DELAY, world));
			}

			if (kit.getFlag(MAXUSES, world) > 0) user.addUse(kit, world);
		}
		sendMessage(player, getMessage("successfullyUsed", kit.getDisplayName(world)));
	}

	private static boolean hasInventorySpace(Player player, ItemStack item) {
		int free = 0;
		for (ItemStack itemStack : player.getInventory().getStorageContents()) {
			if (itemStack == null || itemStack.getType() == Material.AIR) {
				free += item.getMaxStackSize();
			} else if (itemStack.isSimilar(item)) {
				free += item.getMaxStackSize() - itemStack.getAmount();
			}
		}
		return free >= item.getAmount();
	}

	private static int getEmptySpaces(Player player) {
		return (int) Arrays.stream(player.getInventory().getStorageContents()).filter(item -> Objects.isNull(item) || item.getType() == Material.AIR).count();
	}

	public static boolean giveKitToPlayer(Player player, Kit kit) {
		String world = player.getWorld().getName();

		if (kit.getFlag(ITEMSINCONTAINER, world)) {
			ItemStack chestItem = new ItemBuilder(Material.CHEST).setName(kit.getDisplayName(world)).setLore("Place it down to get your items.").toItemStack();
			player.getInventory().addItem(chestItem);
		} else {
			PlayerInventory playerInventory = player.getInventory();

			int freeSpace = getEmptySpaces(player);
			int spaceneed = kit.getItems().size();

			if (kit.getFlag(AUTOEQUIPARMOR, world)) {
				spaceneed += player.getInventory().getArmorContents().length;
			}
			if (!kit.getFlag(SPEWITEMS, world) && spaceneed > freeSpace) {
				sendMessage(player, getMessage("notEnoughSpace", spaceneed));
				return false;
			}
			ItemStack[] equipment = playerInventory.getArmorContents();

			if (kit.getFlag(CLEARINVENTORY, world)) {
				player.getInventory().clear();
				player.getEquipment().clear();
			}

			kit.getItems().forEach(itemStack -> {
				if (hasInventorySpace(player, itemStack)) {
					player.getInventory().addItem(itemStack);
				} else if (kit.getFlag(SPEWITEMS, world)) {
					player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
				}
			});

			// AutoEquip
			if (kit.getFlag(AUTOEQUIPARMOR, world)) {
				for (ItemStack armor : equipment) { //If player had prev armor on
					if (Objects.isNull(armor)) continue;

					if (hasInventorySpace(player, armor)) {
						playerInventory.addItem(armor); // add it to his inventory
					} else if (kit.getFlag(SPEWITEMS, world)) {
						player.getWorld().dropItemNaturally(player.getLocation(), armor); // Or drop it
					}
				}

				//Equip armor
				kit.getArmors().forEach(itemStack -> {
					if (ItemStackUtil.isHelmet(itemStack)) {
						playerInventory.setHelmet(itemStack);
					} else if (ItemStackUtil.isChest(itemStack)) {
						playerInventory.setChestplate(itemStack);
					} else if (ItemStackUtil.isLegs(itemStack)) {
						playerInventory.setLeggings(itemStack);
					} else if (ItemStackUtil.isBoots(itemStack)) {
						playerInventory.setBoots(itemStack);
					}

					if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) && ItemStackUtil.isShield(itemStack)) {
						playerInventory.setItemInOffHand(itemStack);
					}
				});
			} else {
				for (ItemStack armor : kit.getArmors()) { //Add items to the player's inventory.
					if (hasInventorySpace(player, armor)) {
						playerInventory.addItem(armor);
					} else if (kit.getFlag(SPEWITEMS, world)) {
						player.getWorld().dropItemNaturally(player.getLocation(), armor);
					}
				}
			}
		}

		String temp;
		for (String command : kit.getFlag(COMMANDS, world)) {
			temp = (instance.isPlaceholderAPIEnabled() ? PlaceholderAPI.setPlaceholders(player, command) : command.replace("%player_name%", player.getName()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp);
		}

		for (String message : kit.getFlag(MESSAGES, world)) {
			temp = (instance.isPlaceholderAPIEnabled() ? PlaceholderAPI.setPlaceholders(player, message) : message.replace("%player_name%", player.getName()));

			if (temp.contains("subtitle:") && Config.TITLES_ENABLED) {
				MessagesApi.sendTitle(player, "", temp.replace("subtitle:", ""));
			} else if (temp.contains("title:") && Config.TITLES_ENABLED) {
				MessagesApi.sendTitle(player, temp.replace("title:", ""), "");
			} else if (temp.contains("actionbar:") && Config.ACTIONBARS_ENABLED) {
				MessagesApi.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', temp.replace("actionbar:", "")));
			} else {
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
		return true;
	}
}
