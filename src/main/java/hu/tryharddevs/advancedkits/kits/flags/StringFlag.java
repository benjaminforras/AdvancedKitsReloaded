package hu.tryharddevs.advancedkits.kits.flags;

import org.jetbrains.annotations.Nullable;

public class StringFlag extends Flag<String> {
	private final String defaultValue;

	public StringFlag(String name) {
		super(name);
		this.defaultValue = null;
	}

	public StringFlag(String name, String defaultValue) {
		super(name);
		this.defaultValue = defaultValue;
	}

	@Nullable @Override public String getDefault() {
		return defaultValue;
	}

	@Override public String parseInput(String input) throws InvalidFlagValueException {
		return input.replaceAll("(?!\\\\)\\\\n", "\n").replaceAll("\\\\\\\\n", "\\n");
	}

	@Override public String unmarshal(Object o) {
		if (o instanceof String) {
			return (String) o;
		} else {
			return null;
		}
	}

	@Override public Object marshal(String o) {
		return o;
	}
}
