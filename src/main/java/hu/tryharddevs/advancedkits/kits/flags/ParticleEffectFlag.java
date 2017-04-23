package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.ChatColor;
import org.inventivetalent.particle.ParticleEffect;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ParticleEffectFlag extends Flag<ParticleEffect>
{
	public ParticleEffectFlag(String name)
	{
		super(name);
	}

	public static ParticleEffect getParticleEffectByName(String input)
	{
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

	@Override
	public ParticleEffect parseInput(String input) throws InvalidFlagValueException
	{
		return getParticleEffectFromString(input);
	}

	@Override
	public ParticleEffect unmarshal(@Nullable Object o)
	{
		return getParticleEffectByName(String.valueOf(o));
	}

	@Override
	public Object marshal(ParticleEffect o)
	{
		return o.name();
	}

	public ParticleEffect getParticleEffectFromString(String input) throws InvalidFlagValueException
	{
		if (Objects.isNull(getParticleEffectByName(input))) {
			throw new InvalidFlagValueException(AdvancedKitsMain.advancedKits.chatPrefix + " " + ChatColor.RED + "Invalid particle name.", AdvancedKitsMain.advancedKits.chatPrefix + " " + ChatColor.RED + "Here are the available effects: ", ChatColor.GRAY + Arrays.stream(ParticleEffect.values()).map(ParticleEffect::name).collect(Collectors.joining(",")));
		}
		return getParticleEffectByName(input);
	}
}
