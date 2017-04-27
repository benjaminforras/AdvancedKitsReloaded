package hu.tryharddevs.advancedkits.kits.flags;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Objects;

import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class MaterialFlag extends Flag<Material>
{
	private final Material defaultValue;

	public MaterialFlag(String name, Material defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public MaterialFlag(String name)
	{
		super(name);
		this.defaultValue = Material.EMERALD_BLOCK;
	}

	@Nullable
	@Override
	public Material getDefault()
	{
		return defaultValue;
	}

	@Override
	public Material parseInput(String input) throws InvalidFlagValueException
	{
		Material material = Material.matchMaterial(input);
		if (Objects.isNull(material)) {
			throw new InvalidFlagValueException(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notValidIcon2"));
		}

		return material;
	}

	@Nullable
	@Override
	public Material parseItem(Player player) throws InvalidFlagValueException
	{
		Material material = player.getInventory().getItemInMainHand().getType();
		if (Objects.isNull(material) || material == Material.AIR) {
			throw new InvalidFlagValueException(AdvancedKitsMain.advancedKits.chatPrefix + " " + getMessage("notValidIcon2"));
		}

		return material;
	}

	@Override
	public Material unmarshal(@Nullable Object o)
	{
		return Material.valueOf(String.valueOf(o));
	}

	@Override
	public Object marshal(Material o)
	{
		return o.toString();
	}
}
