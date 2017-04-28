package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.User;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
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
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@SuppressWarnings("ConstantConditions")
@CommandAlias("kit|akit|advancedkits|kits|akits")
public class MainCommand extends BaseCommand
{
	private AdvancedKitsMain           instance   = AdvancedKitsMain.advancedKits;
	private HashMap<UUID, ItemStack[]> inEdit     = new HashMap<>();
	private Kit                        currentKit = null;

	private ActionListener useInventoryListener = (clickType, menuObject, whoClicked) -> {
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			whoClicked.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
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
			}
			else if (menuObject.getCoordinates().asSlotNumber() == 6) {
				Bukkit.dispatchCommand(whoClicked, "kit edit " + currentKit.getName());
			}
			menuObject.getMenu().close(whoClicked);
			return;
		}

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			whoClicked.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
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
				Bukkit.dispatchCommand(whoClicked, "kit use " + currentKit.getName());
			}
			else if (clickedItem.getDurability() == (short) 14) {
				Bukkit.dispatchCommand(whoClicked, "kit buy " + currentKit.getName());
			}
		}
		menuObject.getMenu().close(whoClicked);
	};

	private ActionListener buyInventoryListener = (clickType, menuObject, whoClicked) -> {
		ItemStack clickedItem = menuObject.toItemStack();
		if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

		Kit kit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
		if (Objects.isNull(kit)) {
			whoClicked.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}

		Bukkit.dispatchCommand(whoClicked, "kit buy " + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
		menuObject.getMenu().close(whoClicked);
	};

	@Subcommand("buy")
	@CommandPermission("advancedkits.buy")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onBuyCommand(CommandSender sender, @Optional Kit kit)
	{
		Player player = (Player) sender;
		User   user   = User.getUser(player.getUniqueId());
		String world  = player.getWorld().getName();

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

		/*if (Objects.isNull(kit)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}*/

		if (!player.hasPermission(kit.getPermission())) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("noKitPermission"));
			return;
		}


		EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(COST, world));
		if (r.transactionSuccess()) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyBought", kit.getDisplayName(world)));

			user.addToUnlocked(kit);
			user.save();

			if (kit.getFlag(USEONBUY, world)) Bukkit.dispatchCommand(player, "kit use " + kit.getName());
		}
		else {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", r.amount));
		}
	}


	@Subcommand("use")
	@CommandPermission("advancedkits.use")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onGiveCommand(CommandSender sender, @Optional Kit kit)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("not player.");
			return;
		}
		Player player = (Player) sender;
		User   user   = User.getUser(player.getUniqueId());
		String world  = player.getWorld().getName();

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
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("noKitPermission"));
			return;
		}

		if (kit.getFlag(DISABLEDWORLDS, world).contains(player.getWorld().getName())) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseWorld"));
			return;
		}

		if (kit.getFlag(MAXUSES, world) != 0) {
			if (user.getTimesUsed(kit, world) >= kit.getFlag(MAXUSES, world)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseNoMore"));
				return;
			}
		}

		if (kit.getFlag(DELAY, world) > 0 && !player.hasPermission(kit.getDelayPermission())) {
			if (!user.checkDelay(kit, world)) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseDelay", user.getDelay(kit, world)));
				return;
			}
		}

		if (kit.getFlag(PERUSECOST, world) != 0) {
			EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(PERUSECOST, world));
			if (r.transactionSuccess()) {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
			}
			else {
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
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
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughSpace"));
			return;
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
	}

	@Subcommand("create")
	@CommandPermission("advancedkits.create")
	@Syntax("<kitname>")
	public void onGiveCommand(CommandSender sender, String kitName)
	{
		Player player = (Player) sender;
		if (Objects.nonNull(KitManager.getKit(kitName, player.getWorld().getName()))) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitAlreadyExists"));
			return;
		}
		Kit kit = new Kit(kitName);

		if (player.getInventory().getContents().length == 0) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("emptyInventory"));
			return;
		}

		PlayerInventory playerInventory = player.getInventory();
		kit.setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		if (player.getInventory().getArmorContents().length != 0) {
			kit.setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		}

		kit.save();
		KitManager.getKits().add(kit);

		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyCreated", kit.getName()));
	}

	@Subcommand("flag")
	@CommandPermission("advancedkits.flag")
	@CommandCompletion("@kits @flags")
	@Syntax("<kitname> <flag> <value>")
	public void onFlagCommand(CommandSender sender, Kit kit, Flag flag, String value)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("not player.");
			return;
		}
		Player player = (Player) sender;
		String  world  = player.getWorld().getName();
		if (Objects.isNull(kit)) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}

		if (Objects.isNull(flag)) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("flagNotFound"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("availableFlags", Arrays.stream(DefaultFlags.getFlags()).map(Flag::getName).sorted(String::compareToIgnoreCase).collect(Collectors.joining(","))));
			return;
		}

		if (value.equalsIgnoreCase("hand")) {
			if (flag.getName().equalsIgnoreCase("firework")) {
				if (Objects.isNull(player.getInventory().getItemInMainHand()) || !player.getInventory().getItemInMainHand().getType().equals(Material.FIREWORK)) {
					player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notFirework"));
					return;
				}

				try {
					kit.setFlag(flag, world, flag.parseItem(player));
				}
				catch (InvalidFlagValueException e) {
					player.sendMessage(e.getMessages());
					return;
				}

				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
				return;
			}
			else if (flag.getName().equalsIgnoreCase("icon")) {
				if (Objects.isNull(player.getInventory().getItemInMainHand()) || player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
					player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notValidIcon"));
					return;
				}
				try {
					kit.setFlag(flag, world, flag.parseItem(player));
				}
				catch (InvalidFlagValueException e) {
					player.sendMessage(e.getMessages());
					return;
				}

				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
				return;
			}
		}

		if (flag.getName().equalsIgnoreCase("firework") || flag.getName().equalsIgnoreCase("icon")) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + ChatColor.GRAY + "Usage: /kit flag <kitname> <flag> hand");
			return;
		}

		try {
			kit.setFlag(flag, world, flag.parseInput(value));
		}
		catch (InvalidFlagValueException e) {
			player.sendMessage(e.getMessages());
			return;
		}

		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("flagSet", flag.getName(), value, kit.getDisplayName(world), world));
	}

	@Subcommand("give")
	@CommandPermission("advancedkits.give")
	@CommandCompletion("@kits @players true|false")
	@Syntax("<kitname> <player> [forceuse]")
	public void onGiveCommand(CommandSender sender, Kit kit, Player player, @Optional @Default("false") Boolean forceuse)
	{
		if (Objects.isNull(player)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("playerNotFound"));
			return;
		}

		if (player.isDead()) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("playerIsDead"));
			return;
		}

		String world = player.getWorld().getName();

		//Kit kit = KitManager.getKit(String.valueOf(args[0]), world);
		if (Objects.isNull(kit)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}

		User user = User.getUser(player.getUniqueId());

		if (kit.getFlag(DISABLEDWORLDS, world).contains(player.getWorld().getName())) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseWorld"));
			return;
		}

		if (forceuse && kit.getFlag(MAXUSES, world) != 0) {
			if (user.getTimesUsed(kit, world) >= kit.getFlag(MAXUSES, world)) {
				sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseNoMore"));
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseNoMore"));
				return;
			}
		}

		if (forceuse && kit.getFlag(DELAY, world) > 0 && !sender.hasPermission(kit.getDelayPermission())) {
			if (!user.checkDelay(kit, world)) {
				sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseDelay", user.getDelay(kit, world)));
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("cantUseDelay", user.getDelay(kit, world)));
				return;
			}
		}

		if (kit.getFlag(PERUSECOST, world) != 0) {
			EconomyResponse r = VaultUtil.getEconomy().withdrawPlayer(player, kit.getFlag(PERUSECOST, world));
			if (r.transactionSuccess()) {
				sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("moneyLowered", VaultUtil.getEconomy().format(r.balance), VaultUtil.getEconomy().format(r.amount), "PerUseCost"));
			}
			else {
				sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
				player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughMoney", VaultUtil.getEconomy().format(r.amount)));
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
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughSpace"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notEnoughSpace"));
			return;
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

		if (forceuse && kit.getFlag(DELAY, world) > 0 && !sender.hasPermission(kit.getDelayPermission())) {
			user.setDelay(kit, world, kit.getFlag(DELAY, world));
		}

		if (forceuse && kit.getFlag(MAXUSES, world) > 0) user.addUse(kit, world);

		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyUsed", kit.getName()));
		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyUsed", kit.getName()));
	}

	@Subcommand("reload")
	@CommandPermission("advancedkits.reload")
	public void onReloadCommand(CommandSender sender)
	{
		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Starting to reload configuration.");
		instance.loadConfiguration();
		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Done reloading the configuration.");

		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Loading KitManager.");
		KitManager.loadKits();
		sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + "Done loading KitManager.");
	}

	@Subcommand("edit")
	@CommandPermission("advancedkits.edit")
	@CommandCompletion("@kits @worlds")
	@Syntax("[kitname] [world] [action]")
	public void onEditCommand(CommandSender sender, @Optional Kit kit, @Optional World world, @Optional String action)
	{
		if (Objects.isNull(kit)) {
			Player    player    = (Player) sender;
			Inventory inventory = Bukkit.createInventory(player, !inEdit.containsKey(player.getUniqueId()) ? ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9) : 9, "AdvancedKitsReborn - Edit kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			if (!inEdit.containsKey(player.getUniqueId())) {
				for (Kit _kit : KitManager.getKits()) {
					menuObject = new MenuObject(_kit.getFlag(ICON, player.getWorld().getName()), ChatColor.GREEN + _kit.getDisplayName(player.getWorld().getName()), Arrays.asList(ChatColor.BLACK + "", ChatColor.GREEN + "Click to edit"));
					menuObject.setActionListener(editCommandListener);

					menu.addMenuObject(menuObject);
				}
			}
			else {
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

		Player player = (Player) sender;
		if (Objects.nonNull(action) && action.equalsIgnoreCase("cancel")) {
			player.getEquipment().clear();
			player.getInventory().clear();

			player.getInventory().setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());

			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("inventoryRestored"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("exitedEditMode"));
			return;
		}

		currentKit = kit;
		/*if (Objects.isNull(currentKit)) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}*/

		PlayerInventory playerInventory = player.getInventory();
		if (inEdit.containsKey(player.getUniqueId())) {
			currentKit.setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
			if (player.getInventory().getArmorContents().length != 0) {
				currentKit.setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
			}

			currentKit.save();

			player.getEquipment().clear();
			playerInventory.clear();

			playerInventory.setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());

			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyEdited", currentKit.getDisplayName(world.getName())));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("inventoryRestored"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("exitedEditMode"));
		}
		else {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("enteredEditMode"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("editModeHint", currentKit.getName()));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("editModeHint2", currentKit.getName()));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("editModeHint3"));

			inEdit.put(player.getUniqueId(), playerInventory.getContents());
			player.getInventory().clear();
			player.getEquipment().clear();

			currentKit.getItems().forEach(playerInventory::addItem);
			currentKit.getArmors().forEach(itemStack -> {
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
	}

	@Subcommand("view")
	@CommandPermission("advancedkits.view")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onViewCommand(CommandSender sender, @Optional Kit kit)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("not player.");
			return;
		}
		Player player = (Player) sender;
		User   user   = User.getUser(player.getUniqueId());
		String world  = player.getWorld().getName();


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

					Kit kit1 = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), whoClicked.getWorld().getName());
					if (Objects.isNull(kit1)) {
						whoClicked.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
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
		currentKit = kit;

		/*currentKit = KitManager.getKit(String.valueOf(args[0]), world);
		if (Objects.isNull(currentKit)) {
			sender.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return;
		}*/

		Inventory inventory = Bukkit.createInventory(player, 54, "AdvancedKitsReborn - View kit");
		Menu      menu      = new Menu(inventory);

		MenuObject menuObject;
		for (ItemStack itemStack : currentKit.getItems()) {
			menuObject = new MenuObject(itemStack);
			menu.addMenuObject(menuObject);
		}

		int x = 1;
		for (ItemStack itemStack : currentKit.getArmors()) {
			menuObject = new MenuObject(itemStack);
			menu.setMenuObjectAt(new Coordinates(menu, x, 5), menuObject);
			x++;
		}
		menuObject = new MenuObject(Material.PAPER, (byte) 0, getMessage("informations"), KitManager.getKitDescription(player, currentKit, world));
		menu.setMenuObjectAt(new Coordinates(menu, 5, 6), menuObject);

		if (user.isUnlocked(currentKit) || currentKit.getFlag(FREE, world)) {
			menuObject = new MenuObject(Material.STAINED_GLASS_PANE, (byte) 13, ChatColor.GREEN + "Use", Collections.emptyList());
			menuObject.setActionListener(viewInventoryListener);
			menu.setMenuObjectAt(new Coordinates(menu, 9, 6), menuObject);
		}
		else if (currentKit.getFlag(COST, world) > 0) {
			menuObject = new MenuObject(Material.STAINED_GLASS_PANE, (byte) 14, ChatColor.GREEN + "Buy", Collections.emptyList());
			menuObject.setActionListener(viewInventoryListener);
			menu.setMenuObjectAt(new Coordinates(menu, 9, 6), menuObject);
		}

		menu.openForPlayer(player);
		player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitView", currentKit.getDisplayName(world)));
	}


	private static boolean hasInventorySpace(Player player, ItemStack item)
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

	private static int getEmptySpaces(Player player)
	{
		return (int) Arrays.stream(player.getInventory().getStorageContents()).filter(item -> Objects.isNull(item) || item.getType() == Material.AIR).count();
	}
}