package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;

public class DoubleFlag extends Flag<Double>
{

	private final double defaultValue;

	public DoubleFlag(String name, double defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public DoubleFlag(String name)
	{
		super(name);
		this.defaultValue = 0;
	}

	@Nullable
	@Override
	public Double getDefault()
	{
		return defaultValue;
	}

	@Override
	public Double parseInput(String input) throws InvalidFlagValueException
	{
		return getInputAsDouble(input);
	}

	public Double getInputAsDouble(String input) throws InvalidFlagValueException
	{
		try {
			return Double.parseDouble(input);
		}
		catch (NumberFormatException e) {
			throw new InvalidFlagValueException(AdvancedKitsMain.advancedKits.chatPrefix + " " + ChatColor.RED + "Not a number: " + input);
		}
	}

	@Override
	public Double unmarshal(Object o)
	{
		if (o instanceof Double) {
			return (Double) o;
		}
		else if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		else {
			return null;
		}
	}

	@Override
	public Object marshal(Double o)
	{
		return o;
	}
}
