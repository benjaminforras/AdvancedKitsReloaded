package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.cinventory.inventories.CSimpleInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.Session;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.kits.flags.InvalidFlagValueException;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import hu.tryharddevs.advancedkits.utils.MessagesApi;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@SuppressWarnings("ConstantConditions")
@CommandAlias("kit|akit|advancedkits|kits|akits")
public class MainCommand extends BaseCommand {
	private AdvancedKitsMain instance;

	private ConcurrentHashMap<UUID, ItemStack[]> inEdit     = new ConcurrentHashMap<>();
	private ConcurrentHashMap<UUID, Kit>         currentKit = new ConcurrentHashMap<>();

	public MainCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("buy")
	@CommandPermission("advancedkits.buy")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onBuyCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {

			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - Buy Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world) && (!_kit.getFlag(FREE, world) && !user.isUnlocked(_kit))).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "akit buy " + clickedKit.getName());
			});
			return;
		}

		if (!player.hasPermission(kit.getPermission())) {
			sendMessage(player, getMessage("noKitPermission"));
			return;
		}


		if (user.isUnlocked(kit)) {
			sendMessage(player, getMessage("alreadyUnlocked", kit.getName()));
			return;
		}

		EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(COST, world));
		if (r.transactionSuccess()) {
			sendMessage(player, getMessage("successfullyBought", kit.getDisplayName(world)));

			user.addToUnlocked(kit);
			user.save();

			if (kit.getFlag(USEONBUY, world)) Bukkit.dispatchCommand(player, "akit use " + kit.getName());
		} else {
			sendMessage(player, getMessage("notEnoughMoney", r.amount));
		}
	}

	@Subcommand("test")
	public void onTestCommand(Player player) {
		CPageInventory       cPageInventory = new CPageInventory("AdvancedKits - Test", player);
		ArrayList<ItemStack> itemStacks     = new ArrayList<>();
		for (int i = 0; i < 90; i++) {
			itemStacks.add(new ItemBuilder(Material.SIGN).setName(ChatColor.YELLOW + "#" + i).toItemStack());
		}
		cPageInventory.setPages(itemStacks);
		cPageInventory.openInventory();
		cPageInventory.onInventoryClickEvent(event -> {
			event.getWhoClicked().sendMessage(event.getCurrentItem().toString());
		});
	}

	@Subcommand("test2")
	public void onTest2Command(Player player) {
		CSimpleInventory     cSimpleInventory = new CSimpleInventory("AdvancedKits - Test2", player, 54);
		ArrayList<ItemStack> itemStacks       = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			itemStacks.add(new ItemBuilder(Material.SIGN).setName(ChatColor.YELLOW + "#" + i).toItemStack());
		}
		cSimpleInventory.addItems(itemStacks);
		cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 14).setName("Test").toItemStack());
		cSimpleInventory.openInventory();

		cSimpleInventory.onInventoryClickEvent(event -> {
			event.getWhoClicked().sendMessage(event.getCurrentItem().toString());
		});
	}

	@Subcommand("use")
	@CommandPermission("advancedkits.use")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onUseCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - Use Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world) && (_kit.getFlag(FREE, world) || user.isUnlocked(_kit))).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "akit use " + clickedKit.getName());
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
			EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(PERUSECOST, world));
			if (r.transactionSuccess()) {
				sendMessage(player, getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
			} else {
				sendMessage(player, getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
				return;
			}
		}

		if(kit.getFlag(ITEMSINCONTAINER, world))
		{
			ItemStack chestItem = new ItemBuilder(Material.CHEST).setName(kit.getDisplayName(world)).setLore("Place it down to get your items.").toItemStack();
			player.getInventory().addItem(chestItem);
		}
		else {
			PlayerInventory playerInventory = player.getInventory();

			int freeSpace = getEmptySpaces(player);
			int spaceneed = kit.getItems().size();

			if (kit.getFlag(AUTOEQUIPARMOR, world)) {
				spaceneed += player.getInventory().getArmorContents().length;
			}
			if (!kit.getFlag(SPEWITEMS, world) && spaceneed > freeSpace) {
				sendMessage(player, getMessage("notEnoughSpace", spaceneed));
				return;
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

			if (temp.contains("subtitle:")) {
				MessagesApi.sendTitle(player, "", temp.replace("subtitle:", ""));
			} else if (temp.contains("title:")) {
				MessagesApi.sendTitle(player, temp.replace("title:", ""), "");
			} else if (temp.contains("actionbar:")) {
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

		if (kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			user.setDelay(kit, world, kit.getFlag(DELAY, world));
		}

		if (kit.getFlag(MAXUSES, world) > 0) user.addUse(kit, world);

		sendMessage(player, getMessage("successfullyUsed", kit.getName()));
	}

	@Subcommand("create")
	@CommandPermission("advancedkits.create")
	@Syntax("<kitname>")
	public void onCreateCommand(Player player, String kitName) {
		if (Objects.nonNull(KitManager.getKit(kitName, player.getWorld().getName()))) {
			sendMessage(player, getMessage("kitAlreadyExists"));
			return;
		}

		Session session = Session.getSession(player.getUniqueId());

		CSimpleInventory cSimpleInventory = new CSimpleInventory("AdvancedKits - Create", player, 54);
		cSimpleInventory.setModifiable(true);

		cSimpleInventory.setItem(36, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorHelmet")).toItemStack());
		cSimpleInventory.setItem(37, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorChestplate")).toItemStack());
		cSimpleInventory.setItem(38, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorLeggings")).toItemStack());
		cSimpleInventory.setItem(39, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorBoots")).toItemStack());

		if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
			cSimpleInventory.setItem(40, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorShield")).toItemStack());
		}

		cSimpleInventory.setItem(45, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 3).setName(getMessage("saveToSession")).toItemStack());
		if (!session.getKitItems().isEmpty() || !session.getKitArmors().isEmpty()) {
			cSimpleInventory.setItem(46, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 3).setName(getMessage("loadFromSession")).toItemStack());
		}

		cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(getMessage("guiCreateKit", kitName)).toItemStack());

		cSimpleInventory.openInventory();

		cSimpleInventory.onInventoryDragEvent(event -> {
			if (!Stream.of(
					// Armor slots:
					36, 37, 38, 39, 40,
					// Other space slots:
					41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
					// Create kit button slot
					53).filter(event.getInventorySlots()::contains).collect(Collectors.toList()).isEmpty()) {

				event.setCancelled(true);
				event.setResult(Event.Result.DENY);
			}
		});

		cSimpleInventory.onInventoryClickEvent(event -> {
			if (Arrays.asList(
					// Armor slots:
					36, 37, 38, 39, 40,
					// Other space slots:
					41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
					// Create kit button slot
					53).contains(event.getSlot())) {

				event.setCancelled(true);
				event.setResult(Event.Result.DENY);
			}
			int       clickedSlot  = event.getSlot();
			ItemStack itemOnCursor = event.getCursor();

			switch (clickedSlot) {

				// Helmet
				case 36: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorHelmet")).toItemStack());
					} else if (ItemStackUtil.isHelmet(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Chestplate
				case 37: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorChestplate")).toItemStack());
					} else if (ItemStackUtil.isChest(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Leggings
				case 38: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorLeggings")).toItemStack());
					} else if (ItemStackUtil.isLegs(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Boots
				case 39: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorBoots")).toItemStack());
					} else if (ItemStackUtil.isBoots(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Shield
				case 40: {
					if (!Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) break;

					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorShield")).toItemStack());
					} else if (ItemStackUtil.isShield(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Save session button.
				case 45: {
					ItemStack itemStack;
					for (int i = 0; i < 36; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack)) session.addItems(itemStack);
					}

					for (int i = 36; i < 41; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack) && itemStack.getType() != Material.ARMOR_STAND)
							session.addArmor(itemStack);
					}

					sendMessage(player, getMessage("sessionSaved"));
					player.closeInventory();
					break;
				}

				// Load session button.
				case 46: {
					for (ItemStack itemStack : session.getKitItems()) {
						event.getInventory().addItem(itemStack);
					}

					for (ItemStack armor : session.getKitArmors()) {
						if (ItemStackUtil.isHelmet(armor)) event.getInventory().setItem(36, armor);
						else if (ItemStackUtil.isChest(armor)) event.getInventory().setItem(37, armor);
						else if (ItemStackUtil.isLegs(armor)) event.getInventory().setItem(38, armor);
						else if (ItemStackUtil.isBoots(armor)) event.getInventory().setItem(39, armor);
						else if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) && ItemStackUtil.isShield(armor))
							event.getInventory().setItem(40, armor);
					}

					sendMessage(player, getMessage("sessionLoaded"));
					event.getInventory().setItem(46, null);
					break;
				}
				// Create kit button.
				case 53: {
					Kit kit = new Kit(kitName);

					ItemStack            itemStack;
					ArrayList<ItemStack> itemStacks = new ArrayList<>();
					for (int i = 0; i < 36; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack)) itemStacks.add(itemStack);
					}
					kit.setItems(itemStacks);

					itemStacks = new ArrayList<>();
					for (int i = 36; i < 41; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack) && itemStack.getType() != Material.ARMOR_STAND)
							itemStacks.add(itemStack);
					}
					kit.setArmors(itemStacks);

					kit.save();
					KitManager.getKits().add(kit);

					sendMessage(player, getMessage("successfullyCreated", kitName));
					player.closeInventory();
					break;
				}
				default: {
				}
			}
		});
	}

	@Subcommand("delete")
	@CommandPermission("advancedkits.delete")
	@Syntax("<kitname>")
	@CommandCompletion("@kits")
	public void onDeleteCommand(CommandSender sender, @Optional Kit kit) {

		Player player = sender instanceof Player ? (Player) sender : null;

		if (Objects.isNull(kit) && Objects.nonNull(player)) {
			String world = player.getWorld().getName();

			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - View Kit", (Player) sender);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "akit delete " + clickedKit.getName());
			});
			return;
		} else if (Objects.isNull(kit) && Objects.isNull(player)) {
			sendMessage(sender, "Syntax: /kit delete <kitname>");
			return;
		}

		String name = kit.getName();
		if (Objects.nonNull(player)) {

			CSimpleInventory cSimpleInventory = new CSimpleInventory("AdvancedKits - Delete Kit", player);

			cSimpleInventory.setItem(2, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(getMessage("guiConfirm")).toItemStack());
			cSimpleInventory.setItem(6, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 14).setName(getMessage("guiCancel")).toItemStack());
			cSimpleInventory.openInventory();

			cSimpleInventory.onInventoryClickEvent((_event) -> {
				if (_event.getCurrentItem() == null) return;

				ItemStack item = _event.getCurrentItem();
				if (item.getDurability() == (short) 14) //Cancel
				{
					_event.getWhoClicked().closeInventory();
				} else if (item.getDurability() == (short) 13) //Delete
				{
					_event.getWhoClicked().closeInventory();
					instance.getKitManager().deleteKit(kit);
					sendMessage(player, getMessage("successfullyDeleted", name));
				}
			});
		} else {
			instance.getKitManager().deleteKit(kit);
			sendMessage(sender, getMessage("successfullyDeleted", name));
		}
	}

	@Subcommand("flag")
	@CommandPermission("advancedkits.flag")
	@CommandCompletion("@kits @flags")
	@Syntax("<kitname> <flag> <value> [world]")
	public void onFlagCommand(Player player, Kit kit, Flag flag, String value, @Optional String world) {

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
			sendMessage(player, ChatColor.GRAY + "Usage: /kit flag <kitname> <flag> hand");
			return;
		}

		try {
			kit.setFlag(flag, world, flag.parseInput(value));
		} catch (InvalidFlagValueException e) {
			player.sendMessage(e.getMessages());
			return;
		}

		sendMessage(player, getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
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
			sendMessage(sender, getMessage("successfullyGiven", kit.getName(), player.getName()));
		} else {
			sendMessage(sender, getMessage("giveAlreadyUnlocked", kit.getName()));
			return;
		}

		if (forceuse) Bukkit.dispatchCommand(player, "akit use " + kit.getName());
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

	@Subcommand("edit")
	@CommandPermission("advancedkits.edit")
	@CommandCompletion("@kits @worlds")
	@Syntax("[kitname]")
	public void onEditCommand(Player player, @Optional Kit kit) {

		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - Edit Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "akit edit " + clickedKit.getName());
			});
			return;
		}

		Session session = Session.getSession(player.getUniqueId());

		CSimpleInventory cSimpleInventory = new CSimpleInventory("AdvancedKits - Edit", player, 54);
		cSimpleInventory.setModifiable(true);

		//Load kit items to the gui
		cSimpleInventory.addItems(kit.getItems());

		//Load kit armor to the gui
		for (ItemStack armor : kit.getArmors()) {
			if (ItemStackUtil.isHelmet(armor)) cSimpleInventory.setItem(36, armor);
			else if (ItemStackUtil.isChest(armor)) cSimpleInventory.setItem(37, armor);
			else if (ItemStackUtil.isLegs(armor)) cSimpleInventory.setItem(38, armor);
			else if (ItemStackUtil.isBoots(armor)) cSimpleInventory.setItem(39, armor);
			else if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) && ItemStackUtil.isShield(armor)) cSimpleInventory.setItem(40, armor);
		}

		cSimpleInventory.setItem(45, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 3).setName(getMessage("saveToSession")).toItemStack());
		if (!session.getKitItems().isEmpty() || !session.getKitArmors().isEmpty()) {
			cSimpleInventory.setItem(46, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 3).setName(getMessage("loadFromSession")).setLore(getMessage("loadFromSessionWarning")).toItemStack());
		}

		cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(getMessage("guiEditKit", kit.getName())).toItemStack());
		cSimpleInventory.openInventory();

		//Check if there's a missing armor piece from the gui. If so replace it with the holder
		if (Objects.isNull(cSimpleInventory.getItem(36)))
			cSimpleInventory.setItem(36, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorHelmet")).toItemStack());
		if (Objects.isNull(cSimpleInventory.getItem(37)))
			cSimpleInventory.setItem(37, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorChestplate")).toItemStack());
		if (Objects.isNull(cSimpleInventory.getItem(38)))
			cSimpleInventory.setItem(38, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorLeggings")).toItemStack());
		if (Objects.isNull(cSimpleInventory.getItem(39)))
			cSimpleInventory.setItem(39, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorBoots")).toItemStack());

		if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
			if (Objects.isNull(cSimpleInventory.getItem(40)))
				cSimpleInventory.setItem(40, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorShield")).toItemStack());
		}

		cSimpleInventory.onInventoryDragEvent(event -> {
			if (!Stream.of(
					// Armor slots:
					36, 37, 38, 39, 40,
					// Other space slots:
					41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
					// Edit kit button slot
					53).filter(event.getInventorySlots()::contains).collect(Collectors.toList()).isEmpty()) {

				event.setCancelled(true);
				event.setResult(Event.Result.DENY);
			}
		});

		cSimpleInventory.onInventoryClickEvent(event -> {
			if (Arrays.asList(
					// Armor slots:
					36, 37, 38, 39, 40,
					// Other space slots:
					41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
					// Edit kit button slot
					53).contains(event.getSlot())) {

				event.setCancelled(true);
				event.setResult(Event.Result.DENY);
			}
			int       clickedSlot  = event.getSlot();
			ItemStack itemOnCursor = event.getCursor();

			switch (clickedSlot) {

				// Helmet
				case 36: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorHelmet")).toItemStack());
					} else if (ItemStackUtil.isHelmet(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Chestplate
				case 37: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorChestplate")).toItemStack());
					} else if (ItemStackUtil.isChest(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Leggings
				case 38: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorLeggings")).toItemStack());
					} else if (ItemStackUtil.isLegs(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Boots
				case 39: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorBoots")).toItemStack());
					} else if (ItemStackUtil.isBoots(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Shield
				case 40: {
					if (!Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) break;

					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorShield")).toItemStack());
					} else if (ItemStackUtil.isShield(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						event.setCursor(null);
					}
					break;
				}

				// Save session button.
				case 45: {
					ItemStack itemStack;
					for (int i = 0; i < 36; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack)) session.addItems(itemStack);
					}

					for (int i = 36; i < 41; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack) && itemStack.getType() != Material.ARMOR_STAND)
							session.addArmor(itemStack);
					}

					sendMessage(player, getMessage("sessionSaved"));
					player.closeInventory();
					break;
				}

				// Load session button.
				case 46: {

					// Clearing current items
					for (int i = 0; i < 41; i++) {
						event.getInventory().setItem(i, null);
					}

					for (ItemStack itemStack : session.getKitItems()) {
						event.getInventory().addItem(itemStack);
					}

					for (ItemStack armor : session.getKitArmors()) {
						if (ItemStackUtil.isHelmet(armor)) event.getInventory().setItem(36, armor);
						else if (ItemStackUtil.isChest(armor)) event.getInventory().setItem(37, armor);
						else if (ItemStackUtil.isLegs(armor)) event.getInventory().setItem(38, armor);
						else if (ItemStackUtil.isBoots(armor)) event.getInventory().setItem(39, armor);
						else if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) && ItemStackUtil.isShield(armor))
							event.getInventory().setItem(40, armor);
					}

					sendMessage(player, getMessage("sessionLoaded"));
					event.getInventory().setItem(46, null);
					break;
				}

				// Edit kit button.
				case 53: {
					ItemStack            itemStack;
					ArrayList<ItemStack> itemStacks = new ArrayList<>();
					for (int i = 0; i < 36; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack)) itemStacks.add(itemStack);
					}
					kit.setItems(itemStacks);

					itemStacks = new ArrayList<>();
					for (int i = 36; i < 41; i++) {
						itemStack = event.getInventory().getItem(i);
						if (Objects.nonNull(itemStack) && itemStack.getType() != Material.ARMOR_STAND)
							itemStacks.add(itemStack);
					}
					kit.setArmors(itemStacks);

					kit.save();

					sendMessage(player, getMessage("successfullyEdited", kit.getDisplayName(world)));
					player.closeInventory();
					break;
				}
				default: {
				}
			}
		});
	}

	@Subcommand("view")
	@CommandPermission("advancedkits.view")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onViewCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();


		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - View Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			cPageInventory.openInventory();

			cPageInventory.onInventoryClickEvent((_event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_player.closeInventory();
				Bukkit.dispatchCommand(_player, "akit view " + clickedKit.getName());
			});
			return;
		}
		currentKit.put(player.getUniqueId(), kit);

		CSimpleInventory cSimpleInventory = new CSimpleInventory("AdvancedKits - View Kit", player, 54);
		cSimpleInventory.addItems(kit.getItems());

		int i = 36;
		for (ItemStack armor : kit.getArmors()) {
			cSimpleInventory.setItem(i, armor);
			i++;
		}

		cSimpleInventory.setItem(49, new ItemBuilder(Material.PAPER).setName(getMessage("informations")).setLore(KitManager.getKitDescription(player, kit, world)).toItemStack());

		if (user.isUnlocked(currentKit.get(player.getUniqueId())) || currentKit.get(player.getUniqueId()).getFlag(FREE, world)) {
			cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(ChatColor.GREEN + getMessage("guiUse")).toItemStack());
		} else if (currentKit.get(player.getUniqueId()).getFlag(COST, world) > 0) {
			cSimpleInventory.setItem(53, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 14).setName(ChatColor.GREEN + getMessage("guiBuy")).toItemStack());
		}
		cSimpleInventory.openInventory();

		cSimpleInventory.onInventoryClickEvent((_event) -> {
			ItemStack clickedItem = _event.getCurrentItem();
			if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
				return;

			Player _player = (Player) _event.getWhoClicked();
			if (clickedItem.getType() == Material.STAINED_GLASS_PANE) {
				if (clickedItem.getDurability() == (short) 13) {
					Bukkit.dispatchCommand(_player, "akit use " + currentKit.get(_player.getUniqueId()).getName());
				} else if (clickedItem.getDurability() == (short) 14) {
					Bukkit.dispatchCommand(_player, "akit buy " + currentKit.get(_player.getUniqueId()).getName());
				}
			}
		});
		sendMessage(player, getMessage("kitView", currentKit.get(player.getUniqueId()).getDisplayName(world)));
	}


	private boolean hasInventorySpace(Player player, ItemStack item) {
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

	private int getEmptySpaces(Player player) {
		return (int) Arrays.stream(player.getInventory().getStorageContents()).filter(item -> Objects.isNull(item) || item.getType() == Material.AIR).count();
	}

	private void sendMessage(CommandSender sender, String... messages) {
		Arrays.stream(messages).forEach(message -> sender.sendMessage(Config.CHAT_PREFIX + " " + message));
	}
}