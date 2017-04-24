package hu.tryharddevs.advancedkits.commands;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.CommandManager;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import hu.tryharddevs.advancedkits.utils.menuapi.components.ActionListener;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Coordinates;
import hu.tryharddevs.advancedkits.utils.menuapi.components.Menu;
import hu.tryharddevs.advancedkits.utils.menuapi.components.MenuObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.*;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class EditCommand implements ActionListener
{
	private static HashMap<UUID, ItemStack[]> inEdit              = new HashMap<>();
	private static EditCommand                editCommandListener = new EditCommand();

	private static Kit currentKit;

	@CommandManager.Cmd(cmd = "edit", help = "Edit kit", longhelp = "This command opens up a gui where you can edit kits.", permission = "edit", args = "[kitname] [world]", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished editCommand(CommandSender sender, Object[] args)
	{
		if (args.length == 0) {
			Player    player    = (Player) sender;
			Inventory inventory = Bukkit.createInventory(player, !inEdit.containsKey(player.getUniqueId()) ? ((int) (Math.ceil((double) KitManager.getKits().size() / 9)) * 9) : 9, "AdvancedKitsReborn - Edit kit");
			Menu      menu      = new Menu(inventory);

			MenuObject menuObject;
			if (!inEdit.containsKey(player.getUniqueId())) {
				for (Kit kit : KitManager.getKits()) {
					menuObject = new MenuObject(Material.STORAGE_MINECART, (byte) 0, ChatColor.GREEN + kit.getDisplayName(player.getWorld().getName()), Arrays.asList(ChatColor.BLACK + "", ChatColor.GREEN + "Click to edit"));
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

			return CommandManager.CommandFinished.DONE;
		}
		Player player = (Player) sender;
		if (String.valueOf(args[0]).equalsIgnoreCase("cancel")) {
			player.getEquipment().clear();
			player.getInventory().clear();

			player.getInventory().setContents(inEdit.get(player.getUniqueId()));
			inEdit.remove(player.getUniqueId());

			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("inventoryRestored"));
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("exitedEditMode"));
			return CommandManager.CommandFinished.DONE;
		}

		String world = "global";
		if (args.length == 2) {
			world = String.valueOf(args[1]);
		}

		currentKit = KitManager.getKit(String.valueOf(args[0]), world);
		if (Objects.isNull(currentKit)) {
			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("kitNotFound"));
			return CommandManager.CommandFinished.DONE;
		}

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

			player.sendMessage(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("successfullyEdited", currentKit.getDisplayName(world)));
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


		return CommandManager.CommandFinished.DONE;
	}

	@Override
	public void onClick(ClickType clickType, MenuObject menuObject, Player whoClicked)
	{
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
	}
}
