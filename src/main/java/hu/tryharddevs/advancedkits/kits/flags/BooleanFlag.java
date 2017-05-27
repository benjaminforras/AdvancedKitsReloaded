package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.Config;
import org.bukkit.ChatColor;

import org.jetbrains.annotations.Nullable;

public class BooleanFlag extends Flag<Boolean> {
	private final boolean defaultValue;

	public BooleanFlag(String name, boolean defaultValue) {
		super(name);
		this.defaultValue = defaultValue;
	}

	public BooleanFlag(String name) {
		super(name);
		defaultValue = false;
	}

	@Nullable @Override public Boolean getDefault() {
		return defaultValue;
	}

	@Override public Boolean parseInput(String input) throws InvalidFlagValueException {
		if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("on") || input.equalsIgnoreCase("1")) {
			return true;
		} else if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("0")) {
			return false;
		} else {
			throw new InvalidFlagValueException(Config.CHAT_PREFIX + " " + ChatColor.RED + "Not a yes/no value: " + input);
		}
	}

	@Override public Boolean unmarshal(Object o) {
		if (o instanceof Boolean) {
			return (Boolean) o;
		} else {
			return null;
		}
	}

	@Override public Object marshal(Boolean o) {
		return o;
	}
}
