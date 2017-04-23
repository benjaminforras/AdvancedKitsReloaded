package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ItemStackFlag extends Flag<ItemStack>
{
	public ItemStackFlag(String name)
	{
		super(name);
	}

	@Override
	public ItemStack parseInput(String input) throws InvalidFlagValueException
	{
		return null;
	}

	@Nullable
	@Override
	public ItemStack parseItem(Player player)
	{
		return player.getInventory().getItemInMainHand();
	}

	@Override
	public ItemStack unmarshal(@Nullable Object o)
	{
		return ItemStackUtil.itemFromString(String.valueOf(o));
	}

	@Override
	public Object marshal(ItemStack o)
	{
		return ItemStackUtil.itemToString(o);
	}
}
