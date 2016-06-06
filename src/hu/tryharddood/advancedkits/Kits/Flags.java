package advancedkits.Kits;

import org.bukkit.Material;

/**
 * Class:
 *
 * @author TryHardDood
 */
public enum Flags
{
	VISIBLE,
	PERMONLY,
	CLEARINV,
	FIRSTJOIN,
	PERMISSION,
	ICON,
	COST,
	USES,
	DELAY;

	static
	{
		int ids = 0;

		for (Flags flag : values())
		{
			flag.id = (ids++);
		}
	}

	private int id;

	public String getName()
	{
		if (this == VISIBLE)
		{
			return "visible";
		}
		if (this == PERMONLY)
		{
			return "permonly";
		}
		if (this == USES)
		{
			return "uses";
		}
		if (this == FIRSTJOIN)
		{
			return "firstjoin";
		}
		if (this == CLEARINV)
		{
			return "clearinv";
		}
		if (this == PERMISSION)
		{
			return "permission";
		}
		if (this == ICON)
		{
			return "icon";
		}
		if (this == COST)
		{
			return "cost";
		}
		if (this == DELAY)
		{
			return "delay";
		}
		return "Unknown";
	}

	public Class getType()
	{
		if (this == VISIBLE)
		{
			return Boolean.class;
		}
		if (this == PERMONLY)
		{
			return Boolean.class;
		}
		if (this == USES)
		{
			return Integer.class;
		}
		if (this == FIRSTJOIN)
		{
			return Boolean.class;
		}
		if (this == CLEARINV)
		{
			return Boolean.class;
		}
		if (this == PERMISSION)
		{
			return String.class;
		}
		if (this == ICON)
		{
			return Material.class;
		}
		if (this == COST)
		{
			return Integer.class;
		}
		if (this == DELAY)
		{
			return Double.class;
		}
		return this.getClass();
	}

	public String toString()
	{
		return super.toString().toLowerCase();
	}

	public Integer getId()
	{
		return this.id;
	}
}
