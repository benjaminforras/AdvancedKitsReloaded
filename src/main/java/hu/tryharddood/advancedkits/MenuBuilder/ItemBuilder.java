package hu.tryharddood.advancedkits.MenuBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

/**
 * @author PaulBGD from https://gist.github.com/PaulBGD/9831d28b1c7bdba0cddd
 *         <p>
 *         This however was modified for my own needs
 */
public class ItemBuilder {

	private final short data;
	@SuppressWarnings("Convert2Diamond")
	private final HashMap<Enchantment, Integer> enchants = new HashMap<>();
	@SuppressWarnings("Convert2Diamond")
	private final List<String>                  lore     = new ArrayList<>();
	private int      amount;
	private Color    color;
	private Material mat;
	private String title = null;

	public ItemBuilder(ItemStack item) {
		this(item.getType(), item.getDurability());
		this.amount = item.getAmount();
		this.enchants.putAll(item.getEnchantments());

		if (item.hasItemMeta())
		{
			ItemMeta meta = item.getItemMeta();
			if (meta.hasDisplayName())
			{
				this.title = meta.getDisplayName();
			}
			if (meta.hasLore())
			{
				this.lore.addAll(meta.getLore());
			}
			if (meta instanceof LeatherArmorMeta)
			{
				this.setColor(((LeatherArmorMeta) meta).getColor());
			}
		}
	}

	public ItemBuilder(Material mat) {
		this(mat, 1);
	}

	private ItemBuilder(Material mat, int amount) {
		this(mat, amount, (short) 0);
	}

	private ItemBuilder(Material mat, int amount, short data) {
		this.mat = mat;
		this.amount = amount;
		this.data = data;
	}

	public ItemBuilder(Material mat, short data) {
		this(mat, 1, data);
	}

	@SuppressWarnings("Convert2Diamond")
	private static ArrayList<String> split(String string, int maxLength) {
		String[] split = string.split(" ");
		string = "";
		ArrayList<String> newString = new ArrayList<>();
		for (String aSplit : split)
		{
			string += (string.length() == 0 ? "" : " ") + aSplit;
			if (ChatColor.stripColor(string).length() > maxLength)
			{
				newString.add((newString.size() > 0 ? ChatColor.getLastColors(newString.get(newString.size() - 1)) : "") + string);
				string = "";
			}
		}
		if (string.length() > 0)
		{
			newString.add((newString.size() > 0 ? ChatColor.getLastColors(newString.get(newString.size() - 1)) : "") + string);
		}
		return newString;
	}

	public static boolean isBoots(Material type) {
		return type == Material.DIAMOND_BOOTS || type == Material.IRON_BOOTS || type == Material.GOLD_BOOTS || type == Material.CHAINMAIL_BOOTS || type == Material.LEATHER_BOOTS;
	}

	public static boolean isChestplate(Material type) {
		return type == Material.DIAMOND_CHESTPLATE || type == Material.IRON_CHESTPLATE || type == Material.GOLD_CHESTPLATE || type == Material.CHAINMAIL_CHESTPLATE || type == Material.LEATHER_CHESTPLATE;
	}

	public static boolean isHelmet(Material type) {
		return type == Material.DIAMOND_HELMET || type == Material.SKULL_ITEM || type == Material.PUMPKIN || type == Material.IRON_HELMET || type == Material.GOLD_HELMET || type == Material.CHAINMAIL_HELMET || type == Material.LEATHER_HELMET;
	}

	public static boolean isLeggings(Material type) {
		return type == Material.DIAMOND_LEGGINGS || type == Material.IRON_LEGGINGS || type == Material.GOLD_LEGGINGS || type == Material.CHAINMAIL_LEGGINGS || type == Material.LEATHER_LEGGINGS;
	}

	private ItemBuilder addEnchantment(Enchantment enchant, int level) {
		if (enchants.containsKey(enchant))
		{
			enchants.remove(enchant);
		}
		enchants.put(enchant, level);
		return this;
	}

	public ItemBuilder addLore(String... lores) {
		for (String lore : lores)
		{
			this.lore.add(ChatColor.GRAY + lore);
		}
		return this;
	}

	private ItemBuilder addLore(String lore, int maxLength) {
		this.lore.addAll(split(lore, maxLength));
		return this;
	}

	public ItemBuilder addLores(List<String> lores) {
		this.lore.addAll(lores);
		return this;
	}

	private ItemBuilder addLores(List<String> lores, int maxLength) {
		for (String lore : lores)
		{
			addLore(lore, maxLength);
		}
		return this;
	}

	public ItemBuilder addLores(String[] description, int maxLength) {
		return addLores(Arrays.asList(description), maxLength);
	}

	public ItemStack build() {
		Material mat = this.mat;
		if (mat == null)
		{
			mat = Material.AIR;
			Bukkit.getLogger().warning("Null material!");
		}
		else if (mat == Material.AIR)
		{
			Bukkit.getLogger().warning("Air material!");
		}
		ItemStack item = new ItemStack(mat, this.amount, this.data);
		ItemMeta  meta = item.getItemMeta();
		if (meta != null)
		{
			if (this.title != null)
			{
				meta.setDisplayName(this.title);
			}
			if (!this.lore.isEmpty())
			{
				meta.setLore(this.lore);
			}
			if (meta instanceof LeatherArmorMeta)
			{
				((LeatherArmorMeta) meta).setColor(this.color);
			}
			item.setItemMeta(meta);
		}
		item.addUnsafeEnchantments(this.enchants);
		// if (this.potion != null) {
		// this.potion.apply(item);
		// }
		return item;
	}

	@Override
	public ItemBuilder clone() throws CloneNotSupportedException {
		ItemBuilder newBuilder = new ItemBuilder(this.mat);

		newBuilder.setTitle(this.title);
		for (String lore : this.lore)
		{
			newBuilder.addLore(lore);
		}
		for (Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet())
		{
			newBuilder.addEnchantment(entry.getKey(), entry.getValue());
		}
		newBuilder.setColor(this.color);
		// newBuilder.potion = this.potion;

		return newBuilder;
	}

	public HashMap<Enchantment, Integer> getAllEnchantments() {
		return this.enchants;
	}

	public Color getColor() {
		return this.color;
	}

	private ItemBuilder setColor(Color color) {
		if (!this.mat.name().contains("LEATHER_"))
		{
			throw new IllegalArgumentException("Can only dye leather armor!");
		}
		this.color = color;
		return this;
	}

	public int getEnchantmentLevel(Enchantment enchant) {
		return this.enchants.get(enchant);
	}

	private List<String> getLore() {
		return this.lore;
	}

	private String getTitle() {
		return this.title;
	}

	public ItemBuilder setTitle(String title) {
		this.title = (title == null ? null : (title.length() > 2 && ChatColor.getLastColors(title.substring(0, 2)).length() == 0 ? ChatColor.WHITE : "")) + title;
		return this;
	}

	private Material getType() {
		return this.mat;
	}

	public ItemBuilder setType(Material mat) {
		this.mat = mat;
		return this;
	}

	private boolean hasEnchantment(Enchantment enchant) {
		return this.enchants.containsKey(enchant);
	}

	public boolean isItem(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (item.getType() != this.getType())
		{
			return false;
		}
		if (!meta.hasDisplayName() && this.getTitle() != null)
		{
			return false;
		}
		if (!meta.getDisplayName().equals(this.getTitle()))
		{
			return false;
		}
		if (!meta.hasLore() && !this.getLore().isEmpty())
		{
			return false;
		}
		if (meta.hasLore())
		{
			for (String lore : meta.getLore())
			{
				if (!this.getLore().contains(lore))
				{
					return false;
				}
			}
		}
		for (Enchantment enchant : item.getEnchantments().keySet())
		{
			if (!this.hasEnchantment(enchant))
			{
				return false;
			}
		}
		return true;
	}

	public ItemBuilder setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	public ItemBuilder setRawTitle(String title) {
		this.title = title;
		return this;
	}

	public ItemBuilder setTitle(String title, int maxLength) {
		if (title != null && ChatColor.stripColor(title).length() > maxLength)
		{
			ArrayList<String> lores = split(title, maxLength);
			for (int i = 1; i < lores.size(); i++)
			{
				this.lore.add(lores.get(i));
			}
			title = lores.get(0);
		}
		setTitle(title);
		return this;
	}
}
