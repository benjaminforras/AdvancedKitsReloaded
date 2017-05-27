package hu.tryharddevs.advancedkits.kits;

import com.google.common.collect.Maps;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.KitManager.getDifferenceText;

public class User {
	private static HashMap<UUID, User> userHashMap = new HashMap<>();
	private final UUID uuid;

	private       List<String>                      unlockedList = new ArrayList<>();
	private Map<String, Map<String, Boolean>> firstUseList = new HashMap<>();
	private Map<String, Map<String, Double>>  delaysList   = new HashMap<>();
	private Map<String, Map<String, Integer>> usedList     = new HashMap<>();

	private final AdvancedKitsMain instance = AdvancedKitsMain.getPlugin();

	private YamlConfiguration userConfig;

	private User(UUID uuid) {
		this.uuid = uuid;
		this.userConfig = YamlConfiguration.loadConfiguration(getSaveFile());

		if (this.userConfig.contains("UnlockedKits")) this.unlockedList = this.userConfig.getStringList("UnlockedKits");

		Map<String, Object>  tempMap;
		Map<String, Double>  tempMapDouble;
		Map<String, Integer> tempMapInteger;
		Map<String, Boolean> tempMapBoolean;
		if (this.userConfig.contains("LastUseTime")) {
			for (String kit : this.userConfig.getConfigurationSection("LastUseTime").getKeys(false)) {
				tempMap = this.userConfig.getConfigurationSection("LastUseTime." + kit).getValues(false);
				tempMapDouble = tempMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, me -> Double.valueOf(me.getValue().toString())));
				this.delaysList.put(kit, tempMapDouble);
			}
		}

		if (this.userConfig.contains("TimesUsed")) {
			for (String kit : this.userConfig.getConfigurationSection("TimesUsed").getKeys(false)) {
				tempMap = this.userConfig.getConfigurationSection("TimesUsed." + kit).getValues(false);
				tempMapInteger = tempMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, me -> Integer.valueOf(me.getValue().toString())));
				this.usedList.put(kit, tempMapInteger);
			}
		}

		if (this.userConfig.contains("FirstUse")) {
			for (String kit : this.userConfig.getConfigurationSection("FirstUse").getKeys(false)) {
				tempMap = this.userConfig.getConfigurationSection("FirstUse." + kit).getValues(false);
				tempMapBoolean = tempMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, me -> Boolean.valueOf(me.getValue().toString())));
				this.firstUseList.put(kit, tempMapBoolean);
			}
		}
	}

	public static User getUser(UUID uuid) {
		if (!userHashMap.containsKey(uuid)) {
			User user = new User(uuid);
			userHashMap.put(uuid, user);
			return user;
		}
		return userHashMap.get(uuid);
	}

	public void addToUnlocked(Kit kit) {
		this.unlockedList.add(kit.getName());
	}

	public boolean isUnlocked(Kit kit) {
		return isUnlocked(kit.getName());
	}

	private boolean isUnlocked(String name) {
		return this.unlockedList.contains(name);
	}

	public int getTimesUsed(Kit kit, String world) {
		if (this.usedList.containsKey(kit.getName())) {
			if (!kit.hasFlag(DefaultFlags.MAXUSES, world)) world = "global";
			return this.usedList.get(kit.getName()).getOrDefault(world, 0);
		}
		return 0;
	}

	public boolean isFirstTime(Kit kit, String world) {
		if (this.firstUseList.containsKey(kit.getName())) {
			if (!kit.hasFlag(DefaultFlags.FIRSTJOIN, world)) world = "global";
			return this.firstUseList.get(kit.getName()).getOrDefault(world, true);
		}
		return true;
	}

	public void setFirstTime(Kit kit, String world) {
		if (!this.firstUseList.containsKey(kit.getName())) this.firstUseList.put(kit.getName(), Maps.newHashMap());
		if (!kit.hasFlag(DefaultFlags.FIRSTJOIN, world)) world = "global";

		this.firstUseList.get(kit.getName()).put(world, false);
	}

	public void addUse(Kit kit, String world) {
		if (!this.usedList.containsKey(kit.getName())) this.usedList.put(kit.getName(), Maps.newHashMap());
		if (!kit.hasFlag(DefaultFlags.MAXUSES, world)) world = "global";

		int prevVal = 1;
		if (this.usedList.containsKey(kit.getName()) && this.usedList.get(kit.getName()).containsKey(world)) {
			prevVal += this.usedList.get(kit.getName()).get(world);
		}
		this.usedList.get(kit.getName()).put(world, prevVal);
	}

	public void setDelay(Kit kit, String world, double delay) {
		if (!this.delaysList.containsKey(kit.getName())) this.delaysList.put(kit.getName(), Maps.newHashMap());
		if (!kit.hasFlag(DefaultFlags.DELAY, world)) world = "global";

		this.delaysList.get(kit.getName()).put(world, System.currentTimeMillis() + (delay * 1000));
	}

	public String getDelay(Kit kit, String world) {
		if (!kit.hasFlag(DefaultFlags.DELAY, world)) world = "global";
		if (checkDelay(kit, world) || !this.delaysList.containsKey(kit.getName()) || !this.delaysList.get(kit.getName()).containsKey(world)) {
			return "None";
		}

		Long delay = this.delaysList.get(kit.getName()).get(world).longValue();
		Date date  = new Date(delay);
		return getDifferenceText(new Date(System.currentTimeMillis()), date);
	}

	public boolean checkDelay(Kit kit, String world) {
		if (!this.delaysList.containsKey(kit.getName())) {
			return true;
		}
		if (!kit.hasFlag(DefaultFlags.DELAY, world)) world = "global";
		if (!this.delaysList.get(kit.getName()).containsKey(world)) {
			return true;
		}

		Long delay = this.delaysList.get(kit.getName()).get(world).longValue();
		return System.currentTimeMillis() >= delay;
	}


	public void save() {
		try {
			this.userConfig.set("UnlockedKits", this.unlockedList);
			this.userConfig.set("LastUseTime", this.delaysList);
			this.userConfig.set("TimesUsed", this.usedList);
			this.userConfig.save(getSaveFile());
		} catch (IOException e) {
			this.instance.log(ChatColor.RED + "Please send this to the author of this plugin:");
			this.instance.log(" -- StackTrace --");
			e.printStackTrace();
			this.instance.log(" -- End of StackTrace --");
		}
	}

	private File getSaveFile() {
		if (!this.instance.getDataFolder().exists()) {
			this.instance.getDataFolder().mkdir();
		}
		final File folder = new File(this.instance.getDataFolder(), "userfiles");
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}

		File file = new File(this.instance.getDataFolder() + File.separator + "userfiles", this.uuid.toString() + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				this.instance.getLogger().info("Couldn't create the savefile for " + this.uuid.toString());
				this.instance.getLogger().info(" -- StackTrace --");
				e.printStackTrace();
				this.instance.getLogger().info(" -- End of StackTrace --");
			}
		}
		return file;
	}
}
