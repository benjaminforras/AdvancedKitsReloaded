package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.cinventory.inventories.CPageInventory;
import hu.tryharddevs.advancedkits.cinventory.inventories.CSimpleInventory;
import hu.tryharddevs.advancedkits.kits.Kit;
import hu.tryharddevs.advancedkits.kits.KitManager;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.ICON;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.VISIBLE;
import static hu.tryharddevs.advancedkits.utils.MessagesApi.sendMessage;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

@CommandAlias("kit|akit|advancedkits|kits|akits")
public class DeleteCommand extends BaseCommand {
	private final AdvancedKitsMain instance;

	public DeleteCommand(AdvancedKitsMain instance)
	{
		this.instance = instance;
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
			cPageInventory.setPages(KitManager.getKits().stream().filter(_kit -> _kit.getFlag(VISIBLE, world)).sorted(Comparator.comparing(Kit::getName)).map(_kit -> new ItemBuilder(_kit.getFlag(ICON, world).clone()).setName(ChatColor.WHITE + _kit.getDisplayName(world)).setLore(KitManager.getKitDescription(player, _kit, world)).hideAttributes().toItemStack()).collect(Collectors.toCollection(ArrayList::new)));
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
				Bukkit.dispatchCommand(_player, "advancedkitsreloaded:kit delete " + clickedKit.getName());
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
}
