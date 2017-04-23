package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;

public class IntegerFlag extends Flag<Integer>
{
	private final int defaultValue;

	public IntegerFlag(String name, int defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public IntegerFlag(String name)
	{
		super(name);
		this.defaultValue = 0;
	}

	@Nullable
	@Override
	public Integer getDefault()
	{
		return defaultValue;
	}

	@Override
	public Integer parseInput(String input) throws InvalidFlagValueException
	{
		return getInputAsInt(input);
	}

	public Integer getInputAsInt(String input) throws InvalidFlagValueException
	{
		try {
			return Integer.parseInt(input);
		}
		catch (NumberFormatException e) {
			throw new InvalidFlagValueException(AdvancedKitsMain.advancedKits.chatPrefix + " " + ChatColor.RED + "Not a number: " + input);
		}
	}

	@Override
	public Integer unmarshal(Object o)
	{
		if (o instanceof Integer) {
			return (Integer) o;
		}
		else if (o instanceof Number) {
			return ((Number) o).intValue();
		}
		else {
			return null;
		}
	}

	@Override
	public Object marshal(Integer o)
	{
		return o;
	}
}
