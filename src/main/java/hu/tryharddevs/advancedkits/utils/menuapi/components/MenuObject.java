package hu.tryharddevs.advancedkits.utils.menuapi.components;

import hu.tryharddevs.advancedkits.utils.menuapi.components.sub.GUISound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ColonelHedgehog on 12/11/14.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public class MenuObject
{
	private ItemStack               item;
	private Coordinates             coordinates;
	private ActionListener          actionListener;
	private HashMap<Object, Object> metadata;
	private GUISound                sound;

	public MenuObject(ItemStack holder)
	{
		metadata = new HashMap<>();
		if (holder == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "The ItemStack used as a menu object was null." + ChatColor.RESET);
			throw new IllegalArgumentException();
		}


		this.item = holder;
		this.coordinates = null;
		this.actionListener = null;
	}

	public MenuObject(Material icon, byte data, String name, List<String> tooltip)
	{
		metadata = new HashMap<>();
		item = new ItemStack(icon, 1, data);
		ItemMeta meta = item.getItemMeta();
		meta.setLore(tooltip);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		this.coordinates = null;
		this.actionListener = null;
	}

	public void setIcon(ItemStack holder)
	{
		this.item = holder;

		update();
	}

	public void setIcon(Material icon, byte data, String name, List<String> tooltip)
	{
		item = new ItemStack(icon, 1, data);
		ItemMeta meta = item.getItemMeta();
		meta.setLore(tooltip);
		meta.setDisplayName(name);
		item.setItemMeta(meta);

		update();
	}

	public ItemStack toItemStack()
	{
		return this.item;
	}

	public Coordinates getCoordinates()
	{
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates)
	{
		this.coordinates = coordinates;
	}

	public ActionListener getActionListener()
	{
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener)
	{
		this.actionListener = actionListener;
	}

	public void update()
	{
		coordinates.getMenu().getInventory().setItem(coordinates.asSlotNumber(), toItemStack());
	}

	public HashMap<Object, Object> getMetadata()
	{
		return metadata;
	}

	public void setMetadata(HashMap<Object, Object> metadata)
	{
		this.metadata = metadata;
	}

	public Menu getMenu()
	{
		return getCoordinates().getMenu();
	}

	public GUISound getGUISound()
	{
		return sound;
	}

	public void setGUISound(GUISound sound)
	{
		this.sound = sound;
	}
}
