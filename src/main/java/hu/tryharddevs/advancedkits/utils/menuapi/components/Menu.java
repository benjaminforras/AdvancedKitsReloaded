package hu.tryharddevs.advancedkits.utils.menuapi.components;

import hu.tryharddevs.advancedkits.utils.menuapi.core.MenuAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ColonelHedgehog on 12/12/14.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public class Menu
{
	private HashMap<Object, Object>      metadata;
	private Inventory                    inv;
	private HashMap<Integer, MenuObject> objects;

	public Menu(Inventory inv)
	{
		MenuAPI.i().getMenuRegistry().register(this);
		objects = new HashMap<>();
		this.inv = inv;
		metadata = new HashMap<>();
	}

	public Inventory getInventory()
	{
		return inv;
	}

	public void setInventory(Inventory inv)
	{
		objects.clear();
		this.inv = inv;
	}

	public MenuObject getItemAt(Coordinates coordinates)
	{
		return this.equals(coordinates.getMenu()) ? objects.get(coordinates.asSlotNumber()) : null;
	}

	public void setMenuObjectAt(Coordinates coordinates, MenuObject menuObject)
	{
		if (menuObject.getCoordinates() != null && objects.containsKey(menuObject.getCoordinates().asSlotNumber())) {
			objects.remove(menuObject.getCoordinates().asSlotNumber());
		}

		objects.put(coordinates.asSlotNumber(), menuObject);
		menuObject.setCoordinates(coordinates);

		int slot = coordinates.asSlotNumber();

		if (slot >= inv.getSize() || slot < 0) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Unreachable coordinates \"(" + coordinates.getX() + ", " + coordinates.getY() + ")\"! These coordinates measure to " + slot + " which cannot be mapped on an inventory with a size of " + inv.getSize() + "." + ChatColor.RESET);
			throw new IllegalArgumentException();
		}

		inv.setItem(slot, menuObject.toItemStack());
	}

	public void removeMenuObjectAt(Coordinates coordinates)
	{
		coordinates.getMenu().getItemAt(coordinates).setCoordinates(null);
		inv.setItem(coordinates.asSlotNumber(), null);
	}

	public void addMenuObject(MenuObject... menuObject)
	{
		for (MenuObject me : menuObject) {
			if (inv.firstEmpty() != -1) {
				setMenuObjectAt(new Coordinates(this, inv.firstEmpty()), me);
			}
		}
	}

	@Deprecated
	public void close()
	{
		this.objects.clear();
		inv.clear();
		MenuAPI.i().getMenuRegistry().deregister(this);
		for (HumanEntity viewer : inv.getViewers()) {
			viewer.closeInventory();
		}
	}

	public void clear()
	{
		this.objects.clear();
		inv.clear();
	}

	public void close(Player p)
	{
		p.closeInventory();
	}

	public void openForPlayer(Player p)
	{
		p.openInventory(inv);
	}

	public MenuObject getItemByItemStack(ItemStack currentItem)
	{
		for (Map.Entry<Integer, MenuObject> entry : objects.entrySet()) {
			if (entry.getValue().toItemStack().equals(currentItem)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public HashMap<Integer, MenuObject> getObjects()
	{
		return objects;
	}

	public HashMap<Object, Object> getMetadata()
	{
		return metadata;
	}


	public void setMetadata(HashMap<Object, Object> metadata)
	{
		this.metadata = metadata;
	}
}
