package hu.tryharddevs.advancedkits.utils.invapi;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PageLayout
{

	private static String             empty   = "X";
	private static String             full    = "O";
	private        int                invSize = 0;
	private        ArrayList<Integer> size    = new ArrayList<Integer>();

	public PageLayout(String... strings)
	{
		invSize = strings.length * 9;
		for (int slot = 0; slot < strings.length * 9; slot++) {
			String string = strings[(int) Math.floor((double) slot / 9D)];
			if (string.length() != 9) {
				throw new RuntimeException("String is not a length of 9. String is a length of " + string.length() + ". " + string);
			}
			String letter = string.substring(slot % 9, (slot % 9) + 1);
			if (letter.equalsIgnoreCase(empty)) {
				continue;
			}
			else if (letter.equalsIgnoreCase(full)) {
				size.add(slot);
			}
			else {
				throw new RuntimeException("Unrecognised value " + letter);
			}
		}
	}

	public static void setStringFormat(String noItem, String aItem)
	{
		empty = noItem;
		full = aItem;
	}

	public ItemStack[] generate(ArrayList<ItemStack> items)
	{
		return generate(items.toArray(new ItemStack[items.size()]));
	}

	public ItemStack[] generate(ItemStack... items)
	{
		ItemStack[] itemArray = new ItemStack[invSize];
		for (int i = 0; i < size.size(); i++) {
			if (i < items.length) {
				ItemStack itemToInsert = items[i];
				if (itemToInsert != null) itemArray[size.get(i)] = itemToInsert.clone();
			}
		}
		return itemArray;
	}

}
