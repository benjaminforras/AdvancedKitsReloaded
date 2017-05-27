package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.Config;
import org.bukkit.ChatColor;
import org.inventivetalent.particle.ParticleEffect;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ParticleEffectFlag extends Flag<ParticleEffect> {
	public ParticleEffectFlag(String name) {
		super(name);
	}

	private static ParticleEffect getParticleEffectByName(String input) {
		ParticleEffect lowMatch = null;

		for (ParticleEffect particleEffect : ParticleEffect.values()) {
			if (particleEffect.name().equalsIgnoreCase(input.trim())) {
				return particleEffect;
			}

			if (particleEffect.name().toLowerCase().startsWith(input.toLowerCase().trim())) {
				lowMatch = particleEffect;
			}
		}

		return lowMatch;
	}

	@Override public ParticleEffect parseInput(String input) throws InvalidFlagValueException {
		return getParticleEffectFromString(input);
	}

	@Override public ParticleEffect unmarshal(@Nullable Object o) {
		return getParticleEffectByName(String.valueOf(o));
	}

	@Override public Object marshal(ParticleEffect o) {
		return o.name();
	}

	private ParticleEffect getParticleEffectFromString(String input) throws InvalidFlagValueException {
		if (Objects.isNull(getParticleEffectByName(input))) {
			throw new InvalidFlagValueException(Config.CHAT_PREFIX + " " + ChatColor.RED + "Invalid particle name.", Config.CHAT_PREFIX + " " + ChatColor.RED + "Here are the available effects: ", ChatColor.GRAY + Arrays.stream(ParticleEffect.values()).map(ParticleEffect::name).collect(Collectors.joining(",")));
		}
		return getParticleEffectByName(input);
	}
}
