package hu.tryharddevs.advancedkits.cinventory.inventories;

import hu.tryharddevs.advancedkits.cinventory.CInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class CSimpleInventory extends CInventory {

	private int inventorySize;
	private final HashMap<Integer, ItemStack> itemStackHashMap   = new HashMap<>();
	private final ArrayList<ItemStack>        itemStackArrayList = new ArrayList<>();

	public CSimpleInventory(String inventoryName, Player player) {
		super(inventoryName, player);
	}

	public CSimpleInventory(Player player, int inventorySize) {
		this(null, player, inventorySize);
	}

	public CSimpleInventory(String inventoryName, Player player, int inventorySize) {
		super(inventoryName, player);
		this.inventorySize = getDynamicSize(inventorySize);
	}

	public void addItems(ArrayList<ItemStack> itemStacks) {
		addItems(itemStacks.toArray(new ItemStack[itemStacks.size()]));
	}

	private void addItems(ItemStack... itemStacks) {
		Arrays.stream(itemStacks).forEach(itemStack -> {
			if (Objects.isNull(itemStack)) this.itemStackArrayList.add(new ItemStack(Material.AIR));
			else this.itemStackArrayList.add(itemStack);
		});
	}

	public void setItem(int slot, ItemStack itemStack) {
		this.itemStackHashMap.put(slot, itemStack);
	}

	public void openInventory() {
		if (isInventoryInUse()) return;

		if (Objects.isNull(this.currentInventory)) {
			this.currentInventory = Bukkit.createInventory(null, inventorySize == 0 ? getDynamicSize(itemStackHashMap.size() + itemStackArrayList.size()) : inventorySize, getInventoryName());
		}

		this.itemStackHashMap.forEach(this.currentInventory::setItem);
		this.itemStackArrayList.forEach(this.currentInventory::addItem);

		openInv();
	}

	@Override
	protected void onInventoryClick(InventoryClickEvent event) {

	}

	@Override
	public ItemStack getItem(int slot) {
		return this.currentInventory.getItem(slot);
	}

	private int getDynamicSize(int items) {
		return Math.min(54, (int) (Math.ceil((double) items / 9)) * 9);
	}
}
