package hu.tryharddood.advancedkits.MenuBuilder;

import hu.tryharddood.advancedkits.MenuBuilder.inventory.InventoryMenuBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*****************************************************
 *              Created by TryHardDood on 2016. 10. 31..
 ****************************************************/

/*****************************************************
 *              Created by TryHardDood on 2016. 10. 31..
 ****************************************************/

public final class PageInventory extends InventoryMenuBuilder {

	private final HashMap<Integer, ItemStack[]> pages = new HashMap<>();

	private ItemStack backAPage;
	private ItemStack forwardsAPage;
	private ItemStack paddingItem;

	private int currentPage;
	private boolean dynamicInventorySize = true;
	private int     inventorySize        = 54;

	public PageInventory(String inventoryName, ArrayList<ItemStack> itemStacks) {
		super(getInventorySize(itemStacks.size()), inventoryName);
		setPages(itemStacks);
	}

	public PageInventory(String inventoryName, ItemStack... itemStacks) {
		super(getInventorySize(itemStacks.length), inventoryName);
		setPages(itemStacks);
	}

	public PageInventory(boolean dymanicInventory) {
		this(null, dymanicInventory);
	}

	public PageInventory(int inventorySize) {
		this(null, inventorySize);
	}

	public PageInventory(String inventoryName) {
		super(54, inventoryName);
	}

	public PageInventory(String inventoryName, boolean dymanicInventory) {
		super(54, inventoryName);
		dynamicInventorySize = dymanicInventory;
	}

	public PageInventory(String inventoryName, int inventorySize) {
		super(inventorySize, inventoryName);
		this.inventorySize = Math.min(54, (int) (Math.ceil((double) inventorySize / 9)) * 9);
		this.dynamicInventorySize = false;
		pages.put(0, new ItemStack[0]);
	}

	private static int getInventorySize(int size) {
		return size > 54 ? 54 : (int) Math.min(54, Math.ceil((double) size / 9) * 9);
	}

	public ItemStack getBackPage() {
		if (backAPage == null)
		{
			backAPage = new ItemBuilder(Material.PAPER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Previous page").build();
		}
		return backAPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public ItemStack getForwardsPage() {
		if (forwardsAPage == null)
		{
			forwardsAPage = new ItemBuilder(Material.PAPER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Next page").build();
		}
		return forwardsAPage;
	}

	public ArrayList<ItemStack> getItems() {
		ArrayList<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < pages.size(); i++)
		{
			ItemStack[] itemArray = pages.get(i);
			items.addAll(Arrays.asList(itemArray).subList(0, itemArray.length - (pages.size() > 1 ? 9 : 0)));
		}
		return items;
	}

	private ItemStack[] getItemsForPage() {
		ItemStack[] pageItems = pages.get(Math.max(getCurrentPage(), 0));
		int         pageSize  = pageItems.length;
		if (pages.size() > 1)
		{
			pageSize += 9;
		}
		if (!this.dynamicInventorySize)
		{
			pageSize = inventorySize;
		}
		else
		{
			pageSize = 54;
		}
		pageItems = Arrays.copyOf(pageItems, pageSize);
		if (getCurrentPage() > 0)
		{
			pageItems[pageItems.length - 9] = getBackPage();
		}

		if (pages.size() - 1 > getCurrentPage())
		{
			pageItems[pageItems.length - 1] = getForwardsPage();
		}

		if (getCurrentPage() > 0 || pages.size() - 1 > getCurrentPage())
		{
			if (pageItems[pageItems.length - 9] == null)
			{
				pageItems[pageItems.length - 9] = getPaddingItem();
			}

			if (pageItems[pageItems.length - 1] == null)
			{
				pageItems[pageItems.length - 1] = getPaddingItem();
			}

			pageItems[pageItems.length - 8] = getPaddingItem();
			pageItems[pageItems.length - 7] = getPaddingItem();
			pageItems[pageItems.length - 6] = getPaddingItem();
			pageItems[pageItems.length - 5] = getPaddingItem();
			pageItems[pageItems.length - 4] = getPaddingItem();
			pageItems[pageItems.length - 3] = getPaddingItem();
			pageItems[pageItems.length - 2] = getPaddingItem();
		}

		return pageItems;
	}

	public ItemStack[] getPage(int pageNumber) {
		if (pages.containsKey(pageNumber))
		{
			return pages.get(pageNumber);
		}
		return null;
	}

	public HashMap<Integer, ItemStack[]> getPages() {
		return pages;
	}

	public void setPages(ItemStack... allItems) {
		pages.clear();
		int         invPage     = 0;
		boolean     usePages    = allItems.length > inventorySize;
		ItemStack[] items       = null;
		int         currentSlot = 0;
		int         baseSize    = inventorySize;
		for (int currentItem = 0; currentItem < allItems.length; currentItem++)
		{
			if (items == null)
			{
				int newSize = allItems.length - currentItem;
				if (usePages && newSize + 9 > baseSize)
				{
					newSize = baseSize - 9;
				}
				else if (newSize > baseSize)
				{
					newSize = baseSize;
				}
				items = new ItemStack[newSize];
			}
			ItemStack item = allItems[currentItem];
			items[currentSlot++] = item;
			if (currentSlot == items.length)
			{
				pages.put(invPage, items);
				invPage++;
				currentSlot = 0;
				items = null;
			}
		}
		if (pages.keySet().size() < getCurrentPage())
		{
			currentPage = pages.keySet().size() - 1;
		}
		if (allItems.length == 0)
		{
			int size = inventorySize;
			if (dynamicInventorySize)
			{
				size = 9;
			}

			int itemsSize = (int) (Math.ceil((double) size / 9)) * 9;
			items = new ItemStack[Math.min(54, itemsSize)];
			pages.put(0, items);
		}
		setPage(getCurrentPage());
	}

	public void setPages(ArrayList<ItemStack> allItems) {
		setPages(allItems.toArray(new ItemStack[allItems.size()]));
	}

	public void setPage(int newPage) {
		if (pages.containsKey(newPage))
		{
			currentPage = newPage;
			getInventory().clear();

			ItemStack[] pageItems = getItemsForPage();
			withItems(pageItems);
		}
	}

	public ItemStack getPaddingItem() {
		if (paddingItem == null)
			paddingItem = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 0).setTitle("§8").build();
		return paddingItem;
	}
}

