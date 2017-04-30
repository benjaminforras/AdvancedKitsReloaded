package hu.tryharddevs.advancedkits.utils.invapi;

import hu.tryharddevs.advancedkits.utils.invapi.listeners.NamedCloseListener;
import hu.tryharddevs.advancedkits.utils.invapi.listeners.NamedPageChangeListener;
import hu.tryharddevs.advancedkits.utils.invapi.listeners.NamedPageClickListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class NamedInventory extends ClickInventory
{

	public NamedPageClickListener  namedClickListener;
	public NamedCloseListener      namedCloseListener;
	public NamedPageChangeListener namedChangeListener;
	protected Page currentPage;
	protected HashMap<ItemStack, Page>   pageDirectors = new HashMap<ItemStack, Page>();
	protected HashMap<Page, ItemStack[]> pages         = new HashMap<Page, ItemStack[]>();

	public NamedInventory(Player player)
	{
		this(null, player);
	}

	public NamedInventory(String inventoryName, Player player)
	{
		super(inventoryName, player);
	}

	public Page getCurrentPage()
	{
		return currentPage;
	}

	public ItemStack[] getPage(Page page)
	{
		return pages.get(page);
	}

	public Page getPage(String pageName)
	{
		for (Page page : pages.keySet())
			if (page.getPageName().equals(pageName)) return page;
		return null;
	}

	public Page getPageLink(ItemStack item)
	{
		return pageDirectors.get(item);
	}

	public HashMap<Page, ItemStack[]> getPages()
	{
		return pages;
	}

	public String getTitle()
	{
		return currentPage.getPageDisplayTitle();
	}

	public void setTitle(String newTitle)
	{
		if (newTitle != null && getCurrentPage() != null) {
			if (!getCurrentPage().getPageDisplayTitle().equals(newTitle)) {
				setPage(new Page(getCurrentPage().getPageName(), newTitle), getPage(getCurrentPage()));
			}
		}
	}

	public void linkPage(ItemStack item, Page page)
	{
		pageDirectors.put(item, page);
	}

	public void linkPage(ItemStack item, String pageName)
	{
		Page page = getPage(pageName);
		if (page != null) {
			linkPage(item, page);
		}
	}

	protected void onInventoryClick(InventoryClickEvent event)
	{
		ItemStack item = event.getCurrentItem();
		if (checkInMenu(event.getRawSlot())) {
			if (item != null && pageDirectors.containsKey(item)) {
				event.setCancelled(true);
				setPage(pageDirectors.get(item));
				if (namedChangeListener != null) {
					namedChangeListener.interact(this, event);
				}
				return;
			}
			int slot = event.getSlot();
			if (isPlayerInventory()) {
				slot -= 9;
				if (slot < 0) {
					slot += 36;
				}
			}

			if (namedClickListener != null) {
				namedClickListener.interact(this, event);
			}

			/*NamedPageClickEvent itemClickEvent = new NamedPageClickEvent(this, currentPage, slot, event);
			if (!isModifiable()) {
				itemClickEvent.setCancelled(true);
			}
			Bukkit.getPluginManager().callEvent(itemClickEvent);
			if (itemClickEvent.isCancelled()) {
				event.setCancelled(true);
			}*/
		}
		else if (!this.isModifiable() && event.isShiftClick() && item != null && item.getType() != Material.AIR) {
			for (int slot = 0; slot < currentInventory.getSize(); slot++) {
				ItemStack invItem = currentInventory.getItem(slot);
				if (invItem == null || invItem.getType() == Material.AIR || (invItem.isSimilar(item) && invItem.getAmount() < invItem.getMaxStackSize())) {
					event.setCancelled(true);
					break;
				}
			}
		}
	}

	public void openInventory()
	{
		if (isInventoryInUse()) {
			return;
		}
		if (isPlayerInventory()) {
			saveContents();
		}
		if (currentPage == null) {
			if (pages.isEmpty()) {
				pages.put(new Page("Inventory"), new ItemStack[0]);
			}
			currentPage = pages.keySet().iterator().next();
		}
		if (currentInventory == null) {
			ItemStack[] pageItems = getPage(currentPage);
			if (isPlayerInventory()) {
				currentInventory = getPlayer().getInventory();
			}
			else {
				currentInventory = Bukkit.createInventory(null, pageItems.length, getTitle());
			}
			setItems(pageItems);
		}
		openInv();
	}

	public void removePage(Page page)
	{
		pages.remove(page);
	}

	public void setPage(Page newPage)
	{
		if (pages.containsKey(newPage)) {
			Page oldPage = currentPage;
			currentPage = newPage;
			if (isInventoryInUse()) {
				ItemStack[] pageItems = pages.get(currentPage);
				if (!isPlayerInventory() && (pageItems.length != currentInventory.getSize() || !oldPage.getPageDisplayTitle().equals(getTitle()))) {
					currentInventory = Bukkit.createInventory(null, pageItems.length, getTitle());
					currentInventory.setContents(pageItems);
					openInv();
				}
				else {
					setItems(pageItems);
				}
			}
		}
	}

	public void setPage(Page page, ItemStack... items)
	{
		if (items.length % 9 != 0) {
			items = Arrays.copyOf(items, (int) (Math.ceil((double) items.length / 9D) * 9));
		}
		if (items.length > (isPlayerInventory() ? 36 : 54)) {
			throw new RuntimeException("A inventory size of " + items.length + " was passed when the max is " + (isPlayerInventory() ? 36 : 54));
		}
		pages.put(page, items);
		if (currentPage == null) {
			currentPage = page;
		}
		else if (currentPage.equals(page)) {
			setPage(page);
		}
	}

	public void setPage(Page page, List<ItemStack> items)
	{
		setPage(page, items.toArray(new ItemStack[items.size()]));
	}

	public void setPage(String pageName)
	{
		Page page = getPage(pageName);
		if (page != null) {
			setPage(page);
		}
	}

	public NamedInventory setPlayerInventory()
	{
		super.setPlayerInventory();
		return this;
	}

	public void unlinkPage(ItemStack item)
	{
		pageDirectors.remove(item);
	}

	public void onNamedClickEvent(
			@Nonnull
					NamedPageClickListener listener)
	{
		namedClickListener = listener;
	}

	public void onNamedCloseEvent(
			@Nonnull
					NamedCloseListener listener)
	{
		namedCloseListener = listener;
	}

	public void onNamedChangePageEvent(
			@Nonnull
					NamedPageChangeListener listener)
	{
		namedChangeListener = listener;
	}
}
