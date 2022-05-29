package hu.tryharddevs.advancedkits.kits;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.Nullable;
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
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class Kit {
	private final String name;
	private final AdvancedKitsMain instance = AdvancedKitsMain.getPlugin();

	private HashMap<String, ConcurrentHashMap<Flag<?>, Object>> flags = new HashMap<>();

	private ArrayList<ItemStack> itemsArrayList  = new ArrayList<>();
	private ArrayList<ItemStack> armorsArrayList = new ArrayList<>();

	private YamlConfiguration kitConfig;

	public Kit(String name) {
		this.name = name;
		this.kitConfig = YamlConfiguration.loadConfiguration(getSaveFile());
	}

	public static ContextResolver<Kit> getContextResolver() {
		return (c) -> {
			Player senderPlayer = c.getSender() instanceof Player ? (Player) c.getSender() : null;
			String world   = "global";
			String kitName = c.popFirstArg();

			if (Objects.nonNull(senderPlayer)) {
				world = senderPlayer.getWorld().getName();
			}

			Kit kit = KitManager.getKit(kitName, world);
			if (Objects.isNull(kit)) {
				throw new InvalidCommandArgument(getMessage("kitNotFound"));
			}
			return kit;
		};
	}

	public final String getName() {
		return this.name;
	}

	public HashMap<String, ConcurrentHashMap<Flag<?>, Object>> getFlags() {
		return this.flags;
	}

	public String getDisplayName(String world) {
		if (!this.flags.containsKey(world)) world = "global";
		if (Objects.isNull(getFlag(DISPLAYNAME, world))) setFlag(DISPLAYNAME, world, this.name);

		return ChatColor.translateAlternateColorCodes('&', getFlag(DISPLAYNAME, world));
	}

	public String getPermission() {
		return "advancedkits.use." + this.name;
	}

	public String getDelayPermission() {
		return "advancedkits.skipdelay." + this.name;
	}

	public void save() {
		// Serialize items in the ArrayLists. This way we don't lose any custom data, including books.
		this.kitConfig.set("Items", this.itemsArrayList.stream().map(ItemStackUtil::itemToString).collect(Collectors.toCollection(ArrayList::new)));
		this.kitConfig.set("Armors", this.armorsArrayList.stream().map(ItemStackUtil::itemToString).collect(Collectors.toCollection(ArrayList::new)));

		if (this.flags.isEmpty()) {
			ConcurrentHashMap<Flag<?>, Object> flagObjects = new ConcurrentHashMap<>();
			for (Flag<?> flag : DefaultFlags.getDefaultFlags()) {
				if (Objects.nonNull(flag.getDefault())) flagObjects.put(flag, flag.getDefault());
				if (flag.getName().equalsIgnoreCase("displayname")) flagObjects.put(flag, this.name);

			}
			this.flags.put("global", flagObjects);
		}

		for (String world : flags.keySet()) {
			this.kitConfig.set("Flags." + world, this.instance.getKitManager().marshal(this.flags.get(world)));
		}

		try {
			this.kitConfig.save(getSaveFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.instance.log("Saved " + this.name);
	}

	public File getSaveFile() {
		File file = new File(this.instance.getDataFolder() + File.separator + "kits", this.name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				this.instance.log(ChatColor.GOLD + "Please send this to the author of this plugin:");
				this.instance.log(" -- StackTrace --");
				e.printStackTrace();
				this.instance.log(" -- End of StackTrace --");
			}
		}
		return file;
	}

	YamlConfiguration getConfig() {
		return this.kitConfig;
	}

	public ArrayList<ItemStack> getItems() {
		return this.itemsArrayList;
	}

	public void setItems(ArrayList<ItemStack> itemsArrayList) {
		this.itemsArrayList = itemsArrayList;
	}

	public ArrayList<ItemStack> getArmors() {
		return this.armorsArrayList;
	}

	public void setArmors(ArrayList<ItemStack> armorsArrayList) {
		this.armorsArrayList = armorsArrayList;
	}


	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Flag<V>, V> V getFlag(T flag, String world) {
		checkNotNull(flag);
		if (!this.flags.containsKey(world)) world = "global";
		if (!this.flags.get(world).containsKey(flag)) world = "global";

		Object obj = this.flags.get(world).get(flag);
		V      val;

		if (obj != null) {
			val = (V) obj;
		} else {
			return flag.getDefault();
		}

		return val;
	}

	public boolean hasFlag(Flag<?> flag, String world) {
		if (this.flags.containsKey(world)) {
			if (this.flags.get(world).containsKey(flag)) {
				return true;
			}
		}
		return false;
	}

	public <T extends Flag<V>, V> void setFlag(T flag, String world, @Nullable V val) {
		checkNotNull(flag);
		if (!this.flags.containsKey(world)) this.flags.put(world, new ConcurrentHashMap<>());

		if (val == null) {
			this.flags.get(world).remove(flag);
		} else {
			this.flags.get(world).put(flag, val);
		}
		save();
	}

	public void setFlags(String world, Map<Flag<?>, Object> flags) {
		checkNotNull(flags);
		this.flags.put(world, new ConcurrentHashMap<>(flags));
	}
}
