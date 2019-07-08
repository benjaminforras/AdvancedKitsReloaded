package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.cinventory.inventories.CSimpleInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.kits.Session;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.ICON;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.VISIBLE;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("%rootcommand")
public class EditCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public EditCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	@Subcommand("edit")
	@CommandPermission("advancedkits.edit")
	@CommandCompletion("@kits @worlds")
	@Syntax("[kitname]")
	public void onEditCommand(Player player, @Optional Kit kit) {

		String world = player.getWorld().getName();

		if (Objects.isNull(kit)) {
			CPageInventory cPageInventory = new CPageInventory("AdvancedKits - Edit Kit", player);
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world).clone()).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes().toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
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
				Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit edit " + clickedKit.getName());
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
			else if (ItemStackUtil.isShield(armor))
				cSimpleInventory.setItem(40, armor);
		}

		cSimpleInventory.setItem(45, new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setName(getMessage("saveToSession")).toItemStack());
		if (!session.getKitItems().isEmpty() || !session.getKitArmors().isEmpty()) {
			cSimpleInventory.setItem(46, new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setName(getMessage("loadFromSession")).setLore(getMessage("loadFromSessionWarning")).toItemStack());
		}

		cSimpleInventory.setItem(53, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName(getMessage("guiEditKit", kit.getName())).toItemStack());
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

		if (Objects.isNull(cSimpleInventory.getItem(40)))
		    cSimpleInventory.setItem(40, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorShield")).toItemStack());

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
						player.setItemOnCursor(null);
					}
					break;
				}

				// Chestplate
				case 37: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorChestplate")).toItemStack());
					} else if (ItemStackUtil.isChest(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						player.setItemOnCursor(null);
					}
					break;
				}

				// Leggings
				case 38: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorLeggings")).toItemStack());
					} else if (ItemStackUtil.isLegs(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						player.setItemOnCursor(null);
					}
					break;
				}

				// Boots
				case 39: {
					if (Objects.isNull(itemOnCursor) || itemOnCursor.getType() == Material.AIR) {
						event.getInventory().setItem(clickedSlot, new ItemBuilder(Material.ARMOR_STAND).setName(getMessage("armorPieceHere")).setLore(getMessage("armorType") + " " + getMessage("armorBoots")).toItemStack());
					} else if (ItemStackUtil.isBoots(itemOnCursor)) {
						event.getInventory().setItem(clickedSlot, itemOnCursor);
						player.setItemOnCursor(null);
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
						player.setItemOnCursor(null);
					}
					break;
				}

				// Save session button.
				case 45: {
					session.getKitItems().clear();
					session.getKitArmors().clear();

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
}
