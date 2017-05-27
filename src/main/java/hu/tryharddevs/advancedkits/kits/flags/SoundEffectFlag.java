package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.Config;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class SoundEffectFlag extends Flag<Sound> {
	public SoundEffectFlag(String name) {
		super(name);
	}

	@Override public Sound parseInput(String input) throws InvalidFlagValueException {
		return getSoundEffectFromString(input);
	}

	@Override public Sound unmarshal(@Nullable Object o) {
		return getSoundEffectByName(String.valueOf(o));
	}

	@Override public Object marshal(Sound o) {
		return o.name();
	}

	private Sound getSoundEffectFromString(String input) throws InvalidFlagValueException {
		if (Objects.isNull(getSoundEffectByName(input))) {
			throw new InvalidFlagValueException(Config.CHAT_PREFIX + " " + ChatColor.RED + "Invalid soundeffect name.", Config.CHAT_PREFIX + " " + ChatColor.RED + "Here are the available effects: ", ChatColor.GRAY + "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
		}

		return getSoundEffectByName(input);
	}

	private Sound getSoundEffectByName(String input) {
		Sound lowMatch = null;

		for (Sound sound : Sound.values()) {
			if (sound.name().equalsIgnoreCase(input.trim())) {
				return sound;
			}

			if (sound.name().toLowerCase().startsWith(input.toLowerCase().trim())) {
				lowMatch = sound;
			}
		}

		return lowMatch;
	}
}
