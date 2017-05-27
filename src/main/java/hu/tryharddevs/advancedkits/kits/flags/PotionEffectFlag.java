package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.Config;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class PotionEffectFlag extends Flag<PotionEffect> {
	public PotionEffectFlag(String name) {
		super(name);
	}

	@Override public PotionEffect parseInput(String input) throws InvalidFlagValueException {
		if (Objects.isNull(getPotionEffectFromString(input))) {
			throw new InvalidFlagValueException(Config.CHAT_PREFIX + " " + ChatColor.RED + "Invalid format or effect.", Config.CHAT_PREFIX + " " + ChatColor.RED + "Correct format: effect;duration;amplifier.", Config.CHAT_PREFIX + " " + ChatColor.RED + "Here are the available potioneffects: ", ChatColor.GRAY + Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull).map(PotionEffectType::getName).collect(Collectors.joining(",")));
		}

		return getPotionEffectFromString(input);
	}

	@Override public PotionEffect unmarshal(@Nullable Object o) {
		return getPotionEffectFromString(String.valueOf(o));
	}

	@Override public Object marshal(PotionEffect o) {
		return getPotionEffectAsString(o);
	}

	private PotionEffect getPotionEffectFromString(String value) {
		if (value.split(";").length != 3) {
			return null;
		}

		PotionEffect potionEffect;
		String[]     split = value.split(";");

		if (Objects.isNull(PotionEffectType.getByName(split[0]))) {
			return null;
		}

		try {
			potionEffect = new PotionEffect(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
		} catch (Exception e) {
			return null;
		}
		return potionEffect;
	}

	// effect,duration,amplifier
	private String getPotionEffectAsString(PotionEffect potionEffect) {
		return potionEffect.getType().getName() + ";" + potionEffect.getDuration() + ";" + potionEffect.getAmplifier();
	}


}
