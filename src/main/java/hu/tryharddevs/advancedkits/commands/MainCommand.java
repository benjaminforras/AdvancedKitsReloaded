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
import hu.tryharddevs.advancedkits.utils.invapi.*;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
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

			PageInventory pageInventory = new PageInventory("AdvancedKits - Buy Kit", player);
			pageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world) && (!_kit.getFlag(FREE, world) && !user.isUnlocked(_kit))).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			pageInventory.openInventory();

			pageInventory.onPagesItemClickEvent((_pageInventory, _event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_pageInventory.closeInventory();
				Bukkit.dispatchCommand(_player, "kit buy " + clickedKit.getName());
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

			if (kit.getFlag(USEONBUY, world)) Bukkit.dispatchCommand(player, "kit use " + kit.getName());
		} else {
			sendMessage(player, getMessage("notEnoughMoney", r.amount));
		}
	}


	@Subcommand("use")
	@CommandPermission("advancedkits.use")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onUseCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {
			PageInventory pageInventory = new PageInventory("AdvancedKits - Use Kit", player);
			pageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world) && (_kit.getFlag(FREE, world) || user.isUnlocked(_kit))).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			pageInventory.openInventory();

			pageInventory.onPagesItemClickEvent((_pageInventory, _event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_pageInventory.closeInventory();
				Bukkit.dispatchCommand(_player, "kit use " + clickedKit.getName());
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

		if (getEmptySpaces(player) == 0) {
			sendMessage(player, getMessage("emptyInventory"));
			return;
		}

		Kit kit = new Kit(kitName);

		PlayerInventory playerInventory = player.getInventory();
		kit.setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		if (player.getInventory().getArmorContents().length != 0) {
			kit.setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
		}

		kit.save();
		KitManager.getKits().add(kit);

		sendMessage(player, getMessage("successfullyCreated", kit.getName()));
	}

	@Subcommand("delete")
	@CommandPermission("advancedkits.delete")
	@Syntax("<kitname>")
	@CommandCompletion("@kits")
	public void onDeleteCommand(CommandSender sender, @Optional Kit kit) {

		Player player = sender instanceof Player ? (Player) sender : null;

		if (Objects.isNull(kit) && Objects.nonNull(player)) {
			String world = player.getWorld().getName();

			PageInventory pageInventory = new PageInventory("AdvancedKits - View Kit", (Player) sender);
			pageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			pageInventory.openInventory();

			pageInventory.onPagesItemClickEvent((_pageInventory, _event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_pageInventory.closeInventory();
				Bukkit.dispatchCommand(_player, "kit delete " + clickedKit.getName());
			});
			return;
		} else if (Objects.isNull(kit) && Objects.isNull(player)) {
			sendMessage(sender, "Syntax: /kit delete <kitname>");
			return;
		}

		String name = kit.getName();
		if (Objects.nonNull(player)) {
			NamedInventory namedInventory = new NamedInventory("AdvancedKits - Delete Kit", player);
			PageLayout     pageLayout     = new PageLayout("XXOXXXOXX");

			namedInventory.setPage(new Page("deleteKitPage", "AdvancedKits - Delete Kit"), pageLayout.generate(new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(getMessage("guiConfirm")).toItemStack(), new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 14).setName(getMessage("guiCancel")).toItemStack()));
			namedInventory.openInventory();

			namedInventory.onNamedClickEvent((_namedInventory, _event) -> {
				if (_event.getCurrentItem() == null) return;

				ItemStack item = _event.getCurrentItem();
				if (item.getDurability() == (short) 14) //Cancel
				{
					_namedInventory.closeInventory();
				} else if (item.getDurability() == (short) 13) //Delete
				{
					_namedInventory.closeInventory();
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

		if (forceuse) Bukkit.dispatchCommand(player, "kit use " + kit.getName());
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
	@Syntax("[kitname] [world] [action]")
	public void onEditCommand(Player player, @Optional Kit kit, @Optional @Default("global") World world, @Optional String action) {
		if (Objects.nonNull(action) && action.equalsIgnoreCase("cancel")) {
			player.getEquipment().clear();
			player.getInventory().clear();

			player.getInventory().setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());
			currentKit.remove(player.getUniqueId());

			sendMessage(player, getMessage("inventoryRestored"));
			sendMessage(player, getMessage("exitedEditMode"));
			return;
		}

		PlayerInventory playerInventory = player.getInventory();
		if (inEdit.containsKey(player.getUniqueId()) && currentKit.containsKey(player.getUniqueId()) && (kit == currentKit.get(player.getUniqueId()))) {
			kit.setItems(Arrays.stream(playerInventory.getStorageContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
			if (player.getInventory().getArmorContents().length != 0) {
				kit.setArmors(Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));
			}

			kit.save();

			player.getEquipment().clear();
			playerInventory.clear();

			playerInventory.setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());
			currentKit.remove(player.getUniqueId());

			sendMessage(player, getMessage("successfullyEdited", kit.getDisplayName(world.getName())));
			sendMessage(player, getMessage("inventoryRestored"));
			sendMessage(player, getMessage("exitedEditMode"));
		} else {
			inEdit.put(player.getUniqueId(), playerInventory.getContents());
			currentKit.put(player.getUniqueId(), kit);

			sendMessage(player, getMessage("enteredEditMode"));
			sendMessage(player, getMessage("editModeHint", kit.getName()));
			sendMessage(player, getMessage("editModeHint2", kit.getName()));
			sendMessage(player, getMessage("editModeHint3"));

			player.getInventory().clear();
			player.getEquipment().clear();

			kit.getItems().forEach(playerInventory::addItem);
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
		}
	}

	private Page viewPage = new Page("AdvancedKits - View Kit");

	@Subcommand("view")
	@CommandPermission("advancedkits.view")
	@CommandCompletion("@kits")
	@Syntax("[kitname]")
	public void onViewCommand(Player player, @Optional Kit kit) {
		User   user  = User.getUser(player.getUniqueId());
		String world = player.getWorld().getName();


		if (Objects.isNull(kit)) {
			PageInventory pageInventory = new PageInventory("AdvancedKits - View Kit", player);
			pageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world)).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
			pageInventory.openInventory();

			pageInventory.onPagesItemClickEvent((_pageInventory, _event) -> {
				ItemStack clickedItem = _event.getCurrentItem();
				if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
					return;

				Player _player = (Player) _event.getWhoClicked();

				Kit clickedKit = KitManager.getKit(clickedItem.getItemMeta().getDisplayName(), _player.getWorld().getName());
				if (Objects.isNull(clickedKit)) {
					sendMessage(_player, getMessage("kitNotFound"));
					return;
				}

				_pageInventory.closeInventory();
				Bukkit.dispatchCommand(_player, "kit view " + clickedKit.getName());
			});
			return;
		}
		currentKit.put(player.getUniqueId(), kit);

		NamedInventory       namedInventory = new NamedInventory("AdvancedKits - View Kit", player);
		PageLayout           pageLayout     = new PageLayout("OOOOOOOOO", "OOOOOOOOO", "OOOOOOOOO", "OOOOOOOOO", "OOOOXXXXX", "XXXXOXXXO");
		ArrayList<ItemStack> items          = new ArrayList<>(kit.getItems());
		if (items.size() < 36) {
			for (int i = items.size(); i < 36; i++) {
				items.add(new ItemStack(Material.AIR));
			}
		}

		if (kit.getArmors().size() < 4) {
			for (int i = kit.getArmors().size(); i < 4; i++) {
				items.add(new ItemStack(Material.AIR));
			}
		}
		items.addAll(kit.getArmors());
		items.add(new ItemBuilder(Material.PAPER).setName(getMessage("informations")).setLore(KitManager.getKitDescription(player, kit, world)).toItemStack());

		if (user.isUnlocked(currentKit.get(player.getUniqueId())) || currentKit.get(player.getUniqueId()).getFlag(FREE, world)) {
			items.add(new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 13).setName(ChatColor.GREEN + getMessage("guiUse")).toItemStack());
		} else if (currentKit.get(player.getUniqueId()).getFlag(COST, world) > 0) {
			items.add(new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 14).setName(ChatColor.GREEN + getMessage("guiBuy")).toItemStack());
		}
		namedInventory.setPage(viewPage, pageLayout.generate(items));
		namedInventory.openInventory();

		namedInventory.onNamedClickEvent((_namedInventory, _event) -> {
			ItemStack clickedItem = _event.getCurrentItem();
			if (Objects.isNull(clickedItem) || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName())
				return;

			Player _player = (Player) _event.getWhoClicked();
			if (clickedItem.getType() == Material.STAINED_GLASS_PANE) {
				if (clickedItem.getDurability() == (short) 13) {
					Bukkit.dispatchCommand(_player, "kit use " + currentKit.get(_player.getUniqueId()).getName());
				} else if (clickedItem.getDurability() == (short) 14) {
					Bukkit.dispatchCommand(_player, "kit buy " + currentKit.get(_player.getUniqueId()).getName());
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