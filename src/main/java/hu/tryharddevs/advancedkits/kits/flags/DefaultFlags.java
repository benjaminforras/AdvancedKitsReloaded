package hu.tryharddevs.advancedkits.kits.flags;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.particle.ParticleEffect;

import java.util.Arrays;
import java.util.List;

public class DefaultFlags {
	public static final BooleanFlag VISIBLE           = new BooleanFlag("visible", true);
	public static final BooleanFlag FIRSTJOIN         = new BooleanFlag("firstjoin", false);
	public static final BooleanFlag RESPAWN           = new BooleanFlag("respawn", false);
	public static final BooleanFlag AUTOEQUIPARMOR    = new BooleanFlag("autoequiparmor", false);
	public static final BooleanFlag FREE              = new BooleanFlag("free", false);
	public static final BooleanFlag USEONBUY          = new BooleanFlag("useonbuy", false);
	public static final BooleanFlag CLEARINVENTORY    = new BooleanFlag("clearinventory", false);
	public static final BooleanFlag SPEWITEMS         = new BooleanFlag("spewitems", false);
	public static final BooleanFlag ITEMSINCONTAINER  = new BooleanFlag("itemsincontainer", false);
	public static final BooleanFlag KEEPINVENTORYOPEN = new BooleanFlag("keepinventoryopen", false);

	public static final StringFlag DISPLAYNAME = new StringFlag("displayname");

	public static final IntegerFlag PERUSECOST = new IntegerFlag("perusecost", 0);
	public static final IntegerFlag COST       = new IntegerFlag("cost", 0);
	public static final IntegerFlag MAXUSES    = new IntegerFlag("maxuses", 0);

	public static final DoubleFlag DELAY = new DoubleFlag("delay", 0.0);

	public static final ItemStackFlag ICON     = new ItemStackFlag("icon", new ItemStack(Material.EMERALD_BLOCK));
	public static final ItemStackFlag FIREWORK = new ItemStackFlag("firework", new ItemStack(Material.FIREWORK_ROCKET));

	public static final ListFlag<String>         CUSTOMDESCRIPTION = new ListFlag<>("customdescription", new StringFlag(null));
	public static final ListFlag<String>         COMMANDS          = new ListFlag<>("commands", new StringFlag(null));
	public static final ListFlag<String>         MESSAGES          = new ListFlag<>("messages", new StringFlag(null));
	public static final ListFlag<String>         DISABLEDWORLDS    = new ListFlag<>("disabledworlds", new StringFlag(null));
	public static final ListFlag<PotionEffect>   POTIONEFFECTS     = new ListFlag<>("potioneffects", new PotionEffectFlag(null));
	public static final ListFlag<ParticleEffect> PARTICLEEFFECTS   = new ListFlag<>("particleeffects", new ParticleEffectFlag(null));
	public static final ListFlag<Sound>          SOUNDEFFECTS      = new ListFlag<>("soundeffects", new SoundEffectFlag(null));

	private static final Flag<?>[] flagsList = new Flag<?>[]{VISIBLE, FIRSTJOIN, RESPAWN, AUTOEQUIPARMOR, FREE, USEONBUY, CLEARINVENTORY, SPEWITEMS, ITEMSINCONTAINER, DISPLAYNAME, PERUSECOST, COST, MAXUSES, DELAY, FIREWORK, ICON, CUSTOMDESCRIPTION, COMMANDS, MESSAGES, DISABLEDWORLDS, POTIONEFFECTS, PARTICLEEFFECTS, SOUNDEFFECTS};

	public static Flag<?>[] getFlags() {
		return flagsList;
	}

	public static List<Flag<?>> getDefaultFlags() {
		return Arrays.asList(flagsList);
	}

	public static Flag<?> fuzzyMatchFlag(String id) {
		for (Flag<?> flag : getDefaultFlags()) {
			if (flag.getName().replace("-", "").equalsIgnoreCase(id.replace("-", ""))) {
				return flag;
			}
		}
		return null;
	}
}
