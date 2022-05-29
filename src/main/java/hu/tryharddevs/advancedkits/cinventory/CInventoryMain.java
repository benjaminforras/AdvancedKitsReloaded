package hu.tryharddevs.advancedkits.cinventory;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Objects;

public class CInventoryMain implements Listener {

	private static final ArrayList<CInventory> cInventoryArrayList = new ArrayList<>();

	public static void registerInventory(CInventory cInventory) {
		cInventoryArrayList.add(cInventory);
	}

	public static void unregisterInventory(CInventory cInventory) {
		cInventoryArrayList.remove(cInventory);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		int found = 0;
		for (CInventory cInv : new ArrayList<>(cInventoryArrayList)) {
			if (cInv.getPlayer() == event.getPlayer()) {

				if (Objects.nonNull(cInv.inventoryCloseListener)) {
					cInv.inventoryCloseListener.interact(event);
				}

				if (cInv.isInventoryInUse()) {
					cInv.closeInventory(false);
				}
				if (found++ == 1) break;
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		int found = 0;
		for (CInventory cInv : new ArrayList<>(cInventoryArrayList)) {
			if (cInv.getPlayer() == event.getWhoClicked()) {

				if (!cInv.isModifiable()) {
					event.setCancelled(true);
					event.setResult(Event.Result.DENY);
				}

				if (Objects.nonNull(cInv.inventoryClickListener)) {
					cInv.inventoryClickListener.interact(event);
				}

				cInv.onInventoryClick(event);
				if (found++ == 1) break;
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		int found = 0;
		for (CInventory cInv : new ArrayList<>(cInventoryArrayList)) {
			if (cInv.getPlayer() == event.getWhoClicked()) {


				if (!cInv.isModifiable()) {
					event.setCancelled(true);
					event.setResult(Event.Result.DENY);
				}

				if (Objects.nonNull(cInv.inventoryDragListener)) {
					cInv.inventoryDragListener.interact(event);
				}

				if (found++ == 1) break;
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		int found = 0;
		for (CInventory cInv : new ArrayList<>(cInventoryArrayList)) {
			if (cInv.getPlayer() == event.getPlayer()) {
				if (cInv.isInventoryInUse()) {
					cInv.closeInventory(false);
				}
				if (found++ == 1) break;
			}
		}
	}
}
