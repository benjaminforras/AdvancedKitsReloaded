package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.Config;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.kits.flags.InvalidFlagValueException;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import hu.tryharddevs.advancedkits.utils.MessagesApi;
import hu.tryharddevs.advancedkits.utils.VaultUtil;
import hu.tryharddevs.advancedkits.utils.menuapi.components.ActionListener;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Coordinates;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.components.MenuObject;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@SuppressWarnings("ConstantConditions") @CommandAlias("kit|akit|advancedkits|kits|akits")
public class MainCommand extends BaseCommand {
	private AdvancedKitsMain instance;

	private ConcurrentHashMap<UUID, ItemStack[]> inEdit     = new ConcurrentHashMap<>();
	private ConcurrentHashMap<UUID, Kit>         currentKit = new ConcurrentHashMap<>();

	private ActionListener useInventoryListener = (clickType, menuObject, whoClicked) -> {
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			sendMessage(whoClicked, getMessage("kitNotFound"));
			return;
		}

		Bukkit.dispatchCommand(whoClicked, "kit use " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
		menuObject.setIcon(Material.MINECART, (byte) 0, kit.getDisplayName(whoClicked.getWorld().getName()), KitManager.getKitDescription(whoClicked, kit, whoClicked.getWorld().getName()));
	};

	private ActionListener editCommandListener = (clickType, menuObject, whoClicked) -> {
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		if (clickedItem.getType().equals(Material.STAINED_GLASS)) {
			if (menuObject.getCoordinates().asSlotNumber() == 2) {
				Bukkit.dispatchCommand(whoClicked, "kit edit " + "cancel");
			} else if (menuObject.getCoordinates().asSlotNumber() == 6) {
				Bukkit.dispatchCommand(whoClicked, "kit edit " + currentKit.get(whoClicked.getUniqueId()));
			}
			menuObject.getMenu().close(whoClicked);
			return;
		}

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			sendMessage(whoClicked, getMessage("kitNotFound"));
			return;
		}

		Bukkit.dispatchCommand(whoClicked, "kit edit " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
		menuObject.getMenu().close(whoClicked);
	};

	private ActionListener viewInventoryListener = (clickType, menuObject, whoClicked) -> {
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		if (clickedItem.getType() == Material.STAINED_GLASS_PANE) {
			if (clickedItem.getDurability() == (short) 13) {
				Bukkit.dispatchCommand(whoClicked, "kit use " + currentKit.get(whoClicked.getUniqueId()));
			} else if (clickedItem.getDurability() == (short) 14) {
				Bukkit.dispatchCommand(whoClicked, "kit buy " + currentKit.get(whoClicked.getUniqueId()));
			}
		}
		menuObject.getMenu().close(whoClicked);
	};

	private ActionListener buyInventoryListener = (clickType, menuObject, whoClicked) -> {
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			sendMessage(whoClicked, getMessage("kitNotFound"));
			return;
		}

		Bukkit.dispatchCommand(whoClicked, "kit buy " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
		menuObject.getMenu().close(whoClicked);
	};

	public MainCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("buy") @CommandPermission("advancedkits.buy") @CommandCompletion("@kits") @Syntax("[kitname]")
	public void onBuyCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {
			Inventory inventory = Bukkit.createInventory(player, ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9), "AdvancedKitsReborn - Buy kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			for (Kit _kit : KitManager.getKits()) {
				if (!_kit.getFlag(VISIBLE, world)) continue;
				if (_kit.getFlag(FREE, world) || user.isUnlocked(_kit)) continue;

				menuObject = new MenuObject(_kit.getFlag(ICON, world), ChatColor.RED + _kit.getDisplayName(player.getWorld().getName()), KitManager.getKitDescription(player, _kit, world));
				menuObject.setActionListener(buyInventoryListener);

				menu.addMenuObject(menuObject);
			}
			menu.openForPlayer(player);

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

			if (kit.getFlag(USEONBUY, world)) Bukkit.dispatchCommand(player, "kit use " + kit.getName());
		} else {
			sendMessage(player, getMessage("notEnoughMoney", r.amount));
		}
	}


	@Subcommand("use") @CommandPermission("advancedkits.use") @CommandCompletion("@kits") @Syntax("[kitname]")
	public void onUseCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {
			Inventory inventory = Bukkit.createInventory(player, ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9), "AdvancedKitsReborn - Use kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			for (Kit _kit : KitManager.getKits()) {
				if (!_kit.getFlag(VISIBLE, world)) continue;
				if (!_kit.getFlag(FREE, world) && !user.isUnlocked(_kit)) continue;

				menuObject = new MenuObject(_kit.getFlag(ICON, world), ChatColor.GREEN + _kit.getDisplayName(player.getWorld().getName()), KitManager.getKitDescription(player, _kit, world));
				menuObject.setActionListener(useInventoryListener);

				menu.addMenuObject(menuObject);
			}
			menu.openForPlayer(player);

			return;
		}

		if (!player.hasPermission(kit.getPermission())) {
			sendMessage(player, getMessage("noKitPermission"));
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

		PlayerInventory playerInventory = player.getInventory();

		int freeSpace = getEmptySpaces(player);
		int spaceneed = kit.getItems().size();

		if (kit.getFlag(AUTOEQUIPARMOR, world)) {
			spaceneed += player.getInventory().getArmorContents().length;
		}
		if (!kit.getFlag(SPEWITEMS, world) && spaceneed > freeSpace) {
			sendMessage(player, getMessage("notEnoughSpace"));
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

	@Subcommand("create") @CommandPermission("advancedkits.create") @Syntax("<kitname>")
	public void onCreateCommand(Player player, String kitName) {
		/*Player player = sender instanceof Player ? (Player) sender : null;
		if (player == null) {
			sendMessage(sender, getMessage("onlyPlayer"));
			return;
		}*/

		if (Objects.nonNull(KitManager.getKit(kitName, player.getWorld().getName()))) {
			sendMessage(player, getMessage("kitAlreadyExists"));
			return;
		}
		Kit kit = new Kit(kitName);

		if (player.getInventory().getContents().length == 0) {
			sendMessage(player, getMessage("emptyInventory"));
			return;
		}

		PlayerInventory playerInventory = player.getInventory();
		kit.setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		if (player.getInventory().getArmorContents().length != 0) {
			kit.setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		}

		kit.save();
		KitManager.getKits().add(kit);

		sendMessage(player, getMessage("successfullyCreated", kit.getName()));
	}

	@Subcommand("flag") @CommandPermission("advancedkits.flag") @CommandCompletion("@kits @flags")
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

	@Subcommand("give") @CommandPermission("advancedkits.give") @CommandCompletion("@kits @players true|false")
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

		if (forceuse) Bukkit.dispatchCommand(player, "kit use " + kit.getName());
	}

	@Subcommand("reload") @CommandPermission("advancedkits.reload") public void onReloadCommand(CommandSender sender) {
		sendMessage(sender, "Starting to reload configuration.");
		Config.loadConfigurationValues(instance);
		sendMessage(sender, "Done reloading the configuration.");

		sendMessage(sender, "Loading KitManager.");
		instance.getKitManager().loadKits();
		sendMessage(sender, "Done loading KitManager.");
	}

	@Subcommand("edit") @CommandPermission("advancedkits.edit") @CommandCompletion("@kits @worlds")
	@Syntax("[kitname] [world] [action]")
	public void onEditCommand(Player player, @Optional Kit kit, @Optional @Default("global") World world, @Optional String action) {
		if (Objects.isNull(kit)) {
			Inventory inventory = Bukkit.createInventory(player, !inEdit.containsKey(player.getUniqueId()) ? ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9) : 9, "AdvancedKitsReborn - Edit kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			if (!inEdit.containsKey(player.getUniqueId())) {
				for (Kit _kit : KitManager.getKits()) {
					menuObject = new MenuObject(_kit.getFlag(ICON, player.getWorld().getName()), ChatColor.GREEN + _kit.getDisplayName(player.getWorld().getName()), Arrays.asList(ChatColor.BLACK + "", ChatColor.GREEN + "Click to edit"));
					menuObject.setActionListener(editCommandListener);

					menu.addMenuObject(menuObject);
				}
			} else {
				menuObject = new MenuObject(Material.STAINED_GLASS, (byte) 14, ChatColor.RED + getMessage("cancelEdit"), Collections.emptyList());
				menuObject.setActionListener(editCommandListener);
				menu.setMenuObjectAt(new Coordinates(menu, 2), menuObject);

				menuObject = new MenuObject(Material.STAINED_GLASS, (byte) 13, ChatColor.GREEN + getMessage("saveAndExitEdit"), Collections.emptyList());
				menuObject.setActionListener(editCommandListener);
				menu.setMenuObjectAt(new Coordinates(menu, 6), menuObject);
			}
			menu.openForPlayer(player);

			return;
		}

		if (Objects.nonNull(action) && action.equalsIgnoreCase("cancel")) {
			player.getEquipment().clear();
			player.getInventory().clear();

			player.getInventory().setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());

			sendMessage(player, getMessage("inventoryRestored"));
			sendMessage(player, getMessage("exitedEditMode"));
			return;
		}

		currentKit.put(player.getUniqueId(), kit);

		PlayerInventory playerInventory = player.getInventory();
		if (inEdit.containsKey(player.getUniqueId())) {
			currentKit.get(player.getUniqueId()).setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
			if (player.getInventory().getArmorContents().length != 0) {
				currentKit.get(player.getUniqueId()).setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
			}

			currentKit.get(player.getUniqueId()).save();

			player.getEquipment().clear();
			playerInventory.clear();

			playerInventory.setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());

			sendMessage(player, getMessage("successfullyEdited", currentKit.get(player.getUniqueId()).getDisplayName(world.getName())));
			sendMessage(player, getMessage("inventoryRestored"));
			sendMessage(player, getMessage("exitedEditMode"));
		} else {
			sendMessage(player, getMessage("enteredEditMode"));
			sendMessage(player, getMessage("editModeHint", currentKit.get(player.getUniqueId()).getName()));
			sendMessage(player, getMessage("editModeHint2", currentKit.get(player.getUniqueId()).getName()));
			sendMessage(player, getMessage("editModeHint3"));

			inEdit.put(player.getUniqueId(), playerInventory.getContents());
			player.getInventory().clear();
			player.getEquipment().clear();

			currentKit.get(player.getUniqueId()).getItems().forEach(playerInventory::addItem);
			currentKit.get(player.getUniqueId()).getArmors().forEach(itemStack -> {
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
		}
	}

	@Subcommand("view") @CommandPermission("advancedkits.view") @CommandCompletion("@kits") @Syntax("[kitname]")
	public void onViewCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();


		if (Objects.isNull(kit)) {
			Inventory inventory = Bukkit.createInventory(player, ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9), "AdvancedKitsReborn - View kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			for (Kit _kit : KitManager.getKits()) {
				if (!_kit.getFlag(VISIBLE, world)) continue;

				menuObject = new MenuObject(_kit.getFlag(ICON, world), ChatColor.WHITE + _kit.getDisplayName(player.getWorld().getName()), KitManager.getKitDescription(player, _kit, world));
				menuObject.setActionListener((clickType, menuObject1, whoClicked) -> {
					ItemStack clickedItem = menuObject1.toItemStack();
					if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

					Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
					if (Objects.isNull(clickedKit)) {
						sendMessage(whoClicked, getMessage("kitNotFound"));
						return;
					}

					menuObject1.getMenu().close(whoClicked);
					Bukkit.dispatchCommand(whoClicked, "kit view " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
				});

				menu.addMenuObject(menuObject);
			}
			menu.openForPlayer(player);

			return;
		}
		currentKit.put(player.getUniqueId(), kit);

		Inventory inventory = Bukkit.createInventory(player, 54, "AdvancedKitsReborn - View kit");
		Menu      menu      = new Menu(inventory);

		MenuObject menuObject;
		for (ItemStack itemStack : currentKit.get(player.getUniqueId()).getItems()) {
			menuObject = new MenuObject(itemStack);
			menu.addMenuObject(menuObject);
		}

		int x = 1;
		for (ItemStack itemStack : currentKit.get(player.getUniqueId()).getArmors()) {
			menuObject = new MenuObject(itemStack);
			menu.setMenuObjectAt(new Coordinates(menu, x, 5), menuObject);
			x++;
		}
		menuObject = new MenuObject(Material.PAPER, (byte) 0, getMessage("informations"), KitManager.getKitDescription(player, currentKit.get(player.getUniqueId()), world));
		menu.setMenuObjectAt(new Coordinates(menu, 5, 6), menuObject);

		if (user.isUnlocked(currentKit.get(player.getUniqueId())) || currentKit.get(player.getUniqueId()).getFlag(FREE, world)) {
			menuObject = new MenuObject(Material.STAINED_GLASS_PANE, (byte) 13, ChatColor.GREEN + "Use", Collections.emptyList());
			menuObject.setActionListener(viewInventoryListener);
			menu.setMenuObjectAt(new Coordinates(menu, 9, 6), menuObject);
		} else if (currentKit.get(player.getUniqueId()).getFlag(COST, world) > 0) {
			menuObject = new MenuObject(Material.STAINED_GLASS_PANE, (byte) 14, ChatColor.GREEN + "Buy", Collections.emptyList());
			menuObject.setActionListener(viewInventoryListener);
			menu.setMenuObjectAt(new Coordinates(menu, 9, 6), menuObject);
		}

		menu.openForPlayer(player);
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