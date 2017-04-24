package hu.tryharddevs.advancedkits.kits;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.DISPLAYNAME;

public class Kit
{
	private final String name;
	private AdvancedKitsMain instance = AdvancedKitsMain.advancedKits;

	private HashMap<String, ConcurrentHashMap<Flag<?>, Object>> flags = new HashMap<>();

	private ArrayList<ItemStack> itemsArrayList  = new ArrayList<>();
	private ArrayList<ItemStack> armorsArrayList = new ArrayList<>();

	private YamlConfiguration kitConfig;

	public Kit(String name)
	{
		this.name = name;
		this.kitConfig = YamlConfiguration.loadConfiguration(getSaveFile());
	}

	public final String getName()
	{
		return name;
	}

	public HashMap<String, ConcurrentHashMap<Flag<?>, Object>> getFlags()
	{
		return flags;
	}

	public String getDisplayName(String world)
	{
		if (!flags.containsKey(world)) world = "global";
		if (Objects.isNull(getFlag(DISPLAYNAME, world))) setFlag(DISPLAYNAME, world, name);

		return ChatColor.translateAlternateColorCodes('&', getFlag(DISPLAYNAME, world));
	}

	public String getPermission()
	{
		return "advancedkits.use." + name;
	}

	public String getDelayPermission()
	{
		return "advancedkits.skipdelay." + name;
	}

	public void save()
	{
		// Serialize items in the ArrayLists. This way we don't lose any custom data, including books.
		kitConfig.set("Items", itemsArrayList.stream().map(ItemStackUtil::itemToString).collect(Collectors.toCollection(ArrayList::new)));
		kitConfig.set("Armors", armorsArrayList.stream().map(ItemStackUtil::itemToString).collect(Collectors.toCollection(ArrayList::new)));

		if (flags.isEmpty()) {
			ConcurrentHashMap<Flag<?>, Object> flagObjects = new ConcurrentHashMap<>();
			for (Flag<?> flag : DefaultFlags.getDefaultFlags()) {
				if (Objects.nonNull(flag.getDefault())) flagObjects.put(flag, flag.getDefault());
				if (flag.getName().equalsIgnoreCase("displayname")) flagObjects.put(flag, name);

			}
			flags.put("global", flagObjects);
		}

		for (String world : flags.keySet()) {
			kitConfig.set("Flags." + world, KitManager.marshal(flags.get(world)));
		}

		try {
			kitConfig.save(getSaveFile());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getSaveFile()
	{
		File file = new File(instance.getDataFolder() + File.separator + "kits", name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				instance.log(ChatColor.GOLD + "Please send this to the author of this plugin:");
				instance.log(" -- StackTrace --");
				e.printStackTrace();
				instance.log(" -- End of StackTrace --");
			}
		}
		return file;
	}

	YamlConfiguration getConfig()
	{
		return kitConfig;
	}

	public ArrayList<ItemStack> getItems()
	{
		return itemsArrayList;
	}

	public void setItems(ArrayList<ItemStack> itemsArrayList)
	{
		this.itemsArrayList = itemsArrayList;
	}

	public ArrayList<ItemStack> getArmors()
	{
		return armorsArrayList;
	}

	public void setArmors(ArrayList<ItemStack> armorsArrayList)
	{
		this.armorsArrayList = armorsArrayList;
	}


	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Flag<V>, V> V getFlag(T flag, String world)
	{
		checkNotNull(flag);
		if (!flags.containsKey(world)) world = "global";
		if (!flags.get(world).containsKey(flag)) world = "global";

		Object obj = flags.get(world).get(flag);
		V      val;

		if (obj != null) {
			val = (V) obj;
		}
		else {
			return flag.getDefault();
		}

		return val;
	}

	public boolean hasFlag(Flag flag, String world)
	{
		if (flags.containsKey(world)) {
			if (flags.get(world).containsKey(flag)) {
				return true;
			}
		}
		return false;
	}

	public <T extends Flag<V>, V> void setFlag(T flag, String world, @Nullable V val)
	{
		checkNotNull(flag);
		if (!flags.containsKey(world)) world = "global";
		if (!flags.get(world).containsKey(flag)) world = "global";

		if (val == null) {
			flags.get(world).remove(flag);
		}
		else {
			flags.get(world).put(flag, val);
		}
	}

	public void setFlags(String world, Map<Flag<?>, Object> flags)
	{
		checkNotNull(flags);
		this.flags.put(world, new ConcurrentHashMap<>(flags));
	}
}
