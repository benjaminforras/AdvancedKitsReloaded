package hu.tryharddevs.advancedkits.cinventory.inventories;

import hu.tryharddevs.advancedkits.cinventory.CInventory;
import hu.tryharddevs.advancedkits.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class CPageInventory extends CInventory {

	private ItemStack backAPage, forwardsAPage, exitInventory;
	private int currentPage;
	private int inventorySize;
	private       boolean                       dynamicInventorySize = true;
	private final HashMap<Integer, ItemStack[]> pagesHashMap         = new HashMap<>();

	public CPageInventory(Player player) {
		this(null, player);
	}

	public CPageInventory(Player player, boolean dymanicInventory) {
		this(null, player, dymanicInventory);
	}

	public CPageInventory(Player player, int inventorySize) {
		this(null, player, inventorySize);
	}

	public CPageInventory(String inventoryName, Player player) {
		super(inventoryName, player);
	}

	private CPageInventory(String inventoryName, Player player, boolean dymanicInventory) {
		super(inventoryName, player);
		this.dynamicInventorySize = dymanicInventory;
	}

	private CPageInventory(String inventoryName, Player player, int inventorySize) {
		super(inventoryName, player);
		this.inventorySize = Math.min(54, (int) (Math.ceil((double) inventorySize / 9)) * 9);
		this.dynamicInventorySize = false;
		this.pagesHashMap.put(0, new ItemStack[0]);
	}

	public void setPages(ArrayList<ItemStack> allItems) {
		setPages(allItems.toArray(new ItemStack[allItems.size()]));
	}

	private void setPages(ItemStack... allItems) {
		if (this.inventorySize == 0)
			this.inventorySize = Math.min(54, (int) (Math.ceil((double) allItems.length / 9)) * 9);

		this.pagesHashMap.clear();

		int         invPage     = 0;
		boolean     usePages    = getExitInventory() != null || allItems.length > this.inventorySize;
		ItemStack[] items       = null;
		int         currentSlot = 0;
		int         baseSize    = this.inventorySize;

		for (int currentItem = 0; currentItem < allItems.length; currentItem++) {
			if (items == null) {
				int newSize = allItems.length - currentItem;
				if (usePages && newSize + 9 > baseSize) {
					newSize = baseSize - 9;
				} else if (newSize > baseSize) {
					newSize = baseSize;
				}
				items = new ItemStack[newSize];
			}

			ItemStack item = allItems[currentItem];
			items[currentSlot++] = item;

			if (currentSlot == items.length) {
				this.pagesHashMap.put(invPage, items);
				invPage++;
				currentSlot = 0;
				items = null;
			}
		}

		if (this.pagesHashMap.keySet().size() < getCurrentPage()) this.currentPage = this.pagesHashMap.keySet().size() - 1;

		if (allItems.length == 0) {
			int size = this.inventorySize;
			if (this.dynamicInventorySize) {
				size = 9;
			}

			items = generateEmptyPage(size);
			if (getExitInventory() != null) {
				items[0] = getExitInventory();
			}
			this.pagesHashMap.put(0, items);
		}
		setPage(getCurrentPage());
	}

	@Override
	protected void onInventoryClick(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (item != null) {
			int newPage = 0;
			if (item.equals(getBackPage())) {
				newPage = -1;
			} else if (item.equals(getForwardsPage())) {
				newPage = 1;
			}

			if (newPage != 0) {
				setPage(getCurrentPage() + newPage);
				event.setCancelled(true);
			}
		}
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.currentInventory.getItem(slot);
	}

	/**
	 * Opens the inventory for use
	 */
	public void openInventory() {
		if (isInventoryInUse()) return;
		ItemStack[] pageItems = getItemsForPage();
		if (this.currentInventory == null) {
			this.currentInventory = Bukkit.createInventory(null, pageItems.length, getInventoryName());
		}
		setItems(pageItems);
		openInv();
	}

	private ItemStack[] getItemsForPage() {
		ItemStack[] pageItems = this.pagesHashMap.get(Math.max(getCurrentPage(), 0));
		int         pageSize  = pageItems.length;
		if (this.pagesHashMap.size() > 1 || this.getExitInventory() != null) {
			pageSize += 9;
		}
		if (!this.dynamicInventorySize) {
			pageSize = this.inventorySize;
		} else {
			pageSize = ((pageSize + 8) / 9) * 9;
		}
		pageItems = Arrays.copyOf(pageItems, pageSize);
		if (getCurrentPage() > 0 || getExitInventory() != null) {
			pageItems[pageItems.length - 9] = getCurrentPage() == 0 ? getExitInventory() : getBackPage();
		}
		if (this.pagesHashMap.size() - 1 > getCurrentPage()) {
			pageItems[pageItems.length - 1] = getForwardsPage();
		}
		return pageItems;
	}

	private void setPage(int newPage) {
		if (this.pagesHashMap.containsKey(newPage)) {
			this.currentPage = newPage;
			if (isInventoryInUse()) {
				ItemStack[] pageItems = getItemsForPage();
				setItems(pageItems);
			}
		}
	}

	private static ItemStack[] generateEmptyPage(int itemsSize) {
		itemsSize = (int) (Math.ceil((double) itemsSize / 9)) * 9;
		return new ItemStack[Math.min(54, itemsSize)];
	}

	private ItemStack getExitInventory() {
		return this.exitInventory;
	}

	public void setExitInventory(ItemStack item) {
		this.exitInventory = item;
	}

	public ItemStack getBackPage() {
		if (this.backAPage == null) {
			this.backAPage = new ItemBuilder(new ItemStack(Material.OAK_SIGN)).setName(getMessage("guiBackpage")).setLore(getMessage("guiBackpageLore")).toItemStack();
		}
		return this.backAPage;
	}

	public void setBackPage(ItemStack newBack) {
		this.backAPage = newBack;
	}

	private int getCurrentPage() {
		return this.currentPage;
	}

	public ItemStack getForwardsPage() {
		if (this.forwardsAPage == null) {
			this.forwardsAPage = new ItemBuilder(new ItemStack(Material.OAK_SIGN)).setName(getMessage("guiNextpage")).setLore(getMessage("guiNextpageLore")).toItemStack();
		}
		return this.forwardsAPage;
	}

	public void setForwardsPage(ItemStack newForwards) {
		this.forwardsAPage = newForwards;
	}
}
