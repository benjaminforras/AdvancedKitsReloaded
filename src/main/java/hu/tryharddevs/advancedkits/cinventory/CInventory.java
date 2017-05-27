package hu.tryharddevs.advancedkits.cinventory;

import hu.tryharddevs.advancedkits.cinventory.listeners.InventoryClickListener;
import hu.tryharddevs.advancedkits.cinventory.listeners.InventoryCloseListener;
import hu.tryharddevs.advancedkits.cinventory.listeners.InventoryDragListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class CInventory {

	public InventoryClickListener inventoryClickListener;
	public InventoryCloseListener inventoryCloseListener;
	public InventoryDragListener  inventoryDragListener;
	public Inventory              currentInventory;

	private boolean modifiable = false;

	private final Player  player;
	private final String  inventoryName;
	private       boolean inventoryInUse;

	protected CInventory(String inventoryName, Player player) {
		this.player = player;
		if (inventoryName == null) {
			inventoryName = getClass().getSimpleName();
		}
		this.inventoryName = inventoryName;
	}

	protected abstract void onInventoryClick(InventoryClickEvent event);

	public void closeInventory() {
		closeInventory(true);
	}

	public void closeInventory(boolean forceClose) {
		CInventoryMain.unregisterInventory(this);
		inventoryInUse = false;

		if (forceClose && getPlayer().getOpenInventory().getTopInventory().equals(currentInventory)) {
			getPlayer().closeInventory();
		}
	}

	protected void setItems(ItemStack[] items) {
		currentInventory.setContents(items);
	}

	protected void openInv() {
		getPlayer().openInventory(currentInventory);
		CInventoryMain.registerInventory(this);
		inventoryInUse = true;
	}

	public boolean isModifiable() {
		return modifiable;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isInventoryInUse() {
		return inventoryInUse;
	}

	public void onInventoryClickEvent(InventoryClickListener inventoryClickListener) {
		this.inventoryClickListener = inventoryClickListener;
	}

	public void onInventoryCloseEvent(InventoryCloseListener inventoryCloseListener) {
		this.inventoryCloseListener = inventoryCloseListener;
	}

	public void onInventoryDragEvent(InventoryDragListener inventoryDragListener) {
		this.inventoryDragListener = inventoryDragListener;
	}

	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}

	protected String getInventoryName() {
		return inventoryName;
	}

	public abstract ItemStack getItem(int slot);
}
