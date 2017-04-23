package hu.tryharddevs.advancedkits.kits;

import com.google.common.collect.Maps;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static hu.tryharddevs.advancedkits.kits.KitManager.getDifferenceText;

public class User
{
	private static HashMap<UUID, User> userHashMap = new HashMap<>();
	private final UUID uuid;

	private List<String>                      unlockedList = new ArrayList<>();
	private Map<String, Map<String, Double>>  delaysList   = new HashMap<>();
	private Map<String, Map<String, Integer>> usedList     = new HashMap<>();

	private AdvancedKitsMain instance = AdvancedKitsMain.advancedKits;

	private YamlConfiguration userConfig;

	public User(UUID uuid)
	{
		this.uuid = uuid;
		this.userConfig = YamlConfiguration.loadConfiguration(getSaveFile());

		if (userConfig.contains("UnlockedKits")) this.unlockedList = userConfig.getStringList("UnlockedKits");

		Map<String, Object>  tempMap;
		Map<String, Double>  tempMapDouble;
		Map<String, Integer> tempMapInteger;
		if (userConfig.contains("LastUseTime")) {
			for (String kit : userConfig.getConfigurationSection("LastUseTime").getKeys(false)) {
				tempMap = userConfig.getConfigurationSection("LastUseTime." + kit).getValues(false);
				tempMapDouble = tempMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, me -> Double.valueOf(me.getValue().toString())));
				this.delaysList.put(kit, tempMapDouble);
			}
		}

		if (userConfig.contains("TimesUsed")) {
			for (String kit : userConfig.getConfigurationSection("TimesUsed").getKeys(false)) {
				tempMap = userConfig.getConfigurationSection("TimesUsed." + kit).getValues(false);
				tempMapInteger = tempMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, me -> Integer.valueOf(me.getValue().toString())));
				usedList.put(kit, tempMapInteger);
			}
		}
	}

	public static User getUser(UUID uuid)
	{
		if (!userHashMap.containsKey(uuid)) {
			User user = new User(uuid);
			userHashMap.put(uuid, user);
			return user;
		}
		return userHashMap.get(uuid);
	}

	public void addToUnlocked(Kit kit)
	{
		unlockedList.add(kit.getName());
	}

	public boolean isUnlocked(Kit kit)
	{
		return isUnlocked(kit.getName());
	}

	public boolean isUnlocked(String name)
	{
		return unlockedList.contains(name);
	}

	public int getTimesUsed(Kit kit, String world)
	{
		if (usedList.containsKey(kit.getName()) && usedList.get(kit.getName()).containsKey(world)) {
			return usedList.get(kit.getName()).get(world);
		}
		return 0;
	}

	public void addUse(Kit kit, String world)
	{
		if (!usedList.containsKey(kit.getName())) usedList.put(kit.getName(), Maps.newHashMap());

		int prevVal = 1;
		if (usedList.containsKey(kit.getName()) && usedList.get(kit.getName()).containsKey(world)) {
			prevVal += usedList.get(kit.getName()).get(world);
		}
		usedList.get(kit.getName()).put(world, prevVal);
	}

	public void setDelay(Kit kit, String world, double delay)
	{
		if (!delaysList.containsKey(kit.getName())) delaysList.put(kit.getName(), Maps.newHashMap());

		delaysList.get(kit.getName()).put(world, System.currentTimeMillis() + (delay * 1000));
	}

	public String getDelay(Kit kit, String world)
	{
		if (checkDelay(kit, world) || !delaysList.containsKey(kit.getName()) || !delaysList.get(kit.getName()).containsKey(world)) {
			return "None";
		}

		Long delay = delaysList.get(kit.getName()).get(world).longValue();
		Date date  = new Date(delay);
		return getDifferenceText(new Date(System.currentTimeMillis()), date);
	}

	public boolean checkDelay(Kit kit, String world)
	{
		if (!delaysList.containsKey(kit.getName())) {
			return true;
		}
		if (!delaysList.get(kit.getName()).containsKey(world)) {
			return true;
		}

		Long delay = delaysList.get(kit.getName()).get(world).longValue();
		return System.currentTimeMillis() >= delay;
	}


	public UUID getUuid()
	{
		return uuid;
	}

	public void save()
	{
		try {
			userConfig.set("UnlockedKits", unlockedList);
			userConfig.set("LastUseTime", delaysList);
			userConfig.set("TimesUsed", usedList);
			userConfig.save(getSaveFile());
		}
		catch (IOException e) {
			instance.log(ChatColor.RED + "Please send this to the author of this plugin:");
			instance.log(" -- StackTrace --");
			e.printStackTrace();
			instance.log(" -- End of StackTrace --");
		}
	}

	private File getSaveFile()
	{
		if (!instance.getDataFolder().exists()) {
			instance.getDataFolder().mkdir();
		}
		final File folder = new File(instance.getDataFolder(), "userfiles");
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}

		File file = new File(instance.getDataFolder() + File.separator + "userfiles", uuid.toString() + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				instance.getLogger().info("Couldn't create the savefile for " + uuid.toString());
				instance.getLogger().info(" -- StackTrace --");
				e.printStackTrace();
				instance.getLogger().info(" -- End of StackTrace --");
			}
		}
		return file;
	}
}
