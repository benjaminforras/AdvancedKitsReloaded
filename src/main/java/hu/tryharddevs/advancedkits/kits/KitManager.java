package hu.tryharddevs.advancedkits.kits;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonSyntaxException;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import hu.tryharddevs.advancedkits.kits.flags.DefaultFlags;
import hu.tryharddevs.advancedkits.kits.flags.Flag;
import hu.tryharddevs.advancedkits.utils.ItemStackUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.tryharddevs.advancedkits.kits.flags.DefaultFlags.*;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public class KitManager {
	private static final Pattern        FILE_PATTERN = Pattern.compile("[^A-Za-z0-9_]+", Pattern.CASE_INSENSITIVE);
	private static       ArrayList<Kit> kitArrayList = new ArrayList<>();
	private static AdvancedKitsMain instance;

	public KitManager(AdvancedKitsMain instance) {
		this.instance = instance;
	}

	public static ArrayList<Kit> getKits() {
		return kitArrayList;
	}

	public static Kit getKit(String name, String world) {
		return kitArrayList.stream().filter(kit -> kit.getName().equalsIgnoreCase(name) || ChatColor.stripColor(kit.getDisplayName(world)).equalsIgnoreCase(ChatColor.stripColor(name))).findFirst().orElse(null);
	}

	public void deleteKit(Kit kit) {

		File config = kit.getSaveFile();
		if (config.delete()) instance.log(ChatColor.GREEN + kit.getName() + " has been deleted.");
		else instance.log(ChatColor.RED + kit.getName() + " could not be deleted.");
		kitArrayList.remove(kit);
	}

	public static ArrayList<String> getKitDescription(Player player, Kit kit, String world) {
		ArrayList<String> descriptions = new ArrayList<>();
		User              user         = User.getUser(player.getUniqueId());

		if (!kit.getFlag(CUSTOMDESCRIPTION, world).isEmpty()) {
			kit.getFlag(CUSTOMDESCRIPTION, world).forEach(message -> descriptions.add(ChatColor.translateAlternateColorCodes('&', (AdvancedKitsMain.getPlugin().isPlaceholderAPIEnabled() ? PlaceholderAPI.setPlaceholders(player, message) : message.replace("%player_name%", player.getName())))));
			descriptions.add(" ");
		}

		if (kit.getFlag(DELAY, world) > 0) {
			if (!user.checkDelay(kit, world) && !player.hasPermission(kit.getDelayPermission())) {
				descriptions.add(" ");
				descriptions.add(getMessage("cantUseDelay", user.getDelay(kit, world)));
				descriptions.add(" ");
			} else if (player.hasPermission(kit.getDelayPermission())) {
				descriptions.add(" ");
				descriptions.add(getMessage("delayImmunity"));
				descriptions.add(" ");
			}

			descriptions.add(getMessage("flagDelay", getDelayInString((int) (double) kit.getFlag(DELAY, world))));
		}

		if (kit.getFlag(COST, world) > 0) {
			descriptions.add(getMessage("flagCost", instance.getEconomy().format(kit.getFlag(COST, world))));
		} else if (kit.getFlag(FREE, world)) {
			descriptions.add(getMessage("flagFree"));
		}

		if (kit.getFlag(MAXUSES, world) != 0) {
			if (user.getTimesUsed(kit, world) >= kit.getFlag(MAXUSES, world)) {
				descriptions.add(getMessage("cantUseNoMore"));
			} else {
				descriptions.add(getMessage("flagAmountOfUses", (kit.getFlag(MAXUSES, world) - user.getTimesUsed(kit, world))));
			}

			descriptions.add(getMessage("flagMaxUses", kit.getFlag(MAXUSES, world)));
		}

		if (kit.getFlag(PERUSECOST, world) != 0) {
			descriptions.add(getMessage("flagPerUseCost", instance.getEconomy().format(kit.getFlag(PERUSECOST, world))));
		}

		if (!kit.getFlag(DISABLEDWORLDS, world).isEmpty()) {
			descriptions.add(getMessage("flagDisabledWorlds"));
			kit.getFlag(DISABLEDWORLDS, world).forEach(s -> descriptions.add(ChatColor.WHITE + "- " + s));
		}

		return descriptions;
	}

	static String getDifferenceText(Date startDate, Date endDate) {
		long different = endDate.getTime() - startDate.getTime();

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli   = minutesInMilli * 60;
		long daysInMilli    = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;

		StringBuilder sb = new StringBuilder();
		if (elapsedDays >= 1) {
			sb.append(elapsedDays).append(" ").append(getMessage("timeUnitDays")).append(" ");
		}

		if (elapsedHours >= 1) {
			sb.append(elapsedHours).append(" ").append(getMessage("timeUnitHours")).append(" ");
		}

		if (elapsedMinutes >= 1) {
			sb.append(elapsedMinutes).append(" ").append(getMessage("timeUnitMinutes")).append(" ");
		}

		if (elapsedSeconds >= 1) {
			sb.append(elapsedSeconds).append(" ").append(getMessage("timeUnitSeconds")).append(" ");
		}
		return sb.toString();
	}

	private static String getDelayInString(int delay) {
		int numberOfDays;
		int numberOfHours;
		int numberOfMinutes;
		int numberOfSeconds;

		numberOfDays = delay / 86400;
		numberOfHours = (delay % 86400) / 3600;
		numberOfMinutes = ((delay % 86400) % 3600) / 60;
		numberOfSeconds = ((delay % 86400) % 3600) % 60;

		StringBuilder sb = new StringBuilder();
		if (numberOfDays >= 1) {
			sb.append(numberOfDays).append(" ").append(getMessage("timeUnitDays")).append(" ");
		}

		if (numberOfHours >= 1) {
			sb.append(numberOfHours).append(" ").append(getMessage("timeUnitHours")).append(" ");
		}

		if (numberOfMinutes >= 1) {
			sb.append(numberOfMinutes).append(" ").append(getMessage("timeUnitMinutes")).append(" ");
		}

		if (numberOfSeconds >= 1) {
			sb.append(numberOfSeconds).append(" ").append(getMessage("timeUnitSeconds")).append(" ");
		}
		return sb.toString();

	}

	public void loadKits() {
		final File folder = new File(instance.getDataFolder(), "kits");

		if (!folder.isDirectory()) {
			instance.log(ChatColor.GOLD + "- kits folder not found. Creating...");
			folder.mkdirs();
			return;
		}
		kitArrayList = new ArrayList<>();

		Kit               kit;
		YamlConfiguration kitConfig;

		String fileName;
		File[] directoryListing = folder.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (!child.isFile()) continue;
				if (!Files.getFileExtension(child.getName()).equalsIgnoreCase("yml")) continue;

				fileName = Files.getNameWithoutExtension(child.getName());
				if (FILE_PATTERN.matcher(fileName).find()) {
					instance.log(ChatColor.RED + "Error when trying to load " + fileName);
					instance.log(ChatColor.RED + "- The name contains special characters.");
					continue;
				}
				kit = new Kit(fileName);
				kitConfig = kit.getConfig();

				if (kitConfig.contains("Items")) {
					List<?> itemsList = kitConfig.getList("Items");

					if (!itemsList.isEmpty()) {
						try {
							kit.setItems(itemsList.stream().map(item -> ItemStackUtil.itemFromString(String.valueOf(item))).collect(Collectors.toCollection(ArrayList::new)));
						} catch (NullPointerException | JsonSyntaxException e) {
							instance.log(ChatColor.RED + "Failed to parse items.");
							instance.log(ChatColor.RED + "Trying to load items using the old methods.");
							kit.setItems(itemsList.stream().map(object -> ItemStack.deserialize((Map<String, Object>) object)).collect(Collectors.toCollection(ArrayList::new)));
						}
					}
				}

				if (kitConfig.contains("Armors")) {
					List<?> armorsList = kitConfig.getList("Armors");

					if (!armorsList.isEmpty()) {
						kit.setArmors(armorsList.stream().map(armor -> ItemStackUtil.itemFromString(String.valueOf(armor))).collect(Collectors.toCollection(ArrayList::new)));
					}
				} else if (kitConfig.contains("Armor")) {
					List<?> armorsList = kitConfig.getList("Armor");

					if (!armorsList.isEmpty()) {
						instance.log(ChatColor.GOLD + "Failed to parse armors.");
						instance.log(ChatColor.GOLD + "Trying to load items using the old methods.");
						kit.setArmors(armorsList.stream().map(object -> ItemStack.deserialize((Map<String, Object>) object)).collect(Collectors.toCollection(ArrayList::new)));
					}

					kitConfig.set("Armor", null);
					kit.save();
				}
				Map<String, Object> temp = null;
				if (kitConfig.contains("Flags")) {

					for (String world : kitConfig.getConfigurationSection("Flags").getKeys(false)) {
						try {
							kit.setFlags(world, unmarshalFlags(kitConfig.getConfigurationSection("Flags." + world).getValues(false)));
						} catch (NullPointerException | JsonSyntaxException e) {
							if (temp == null) {
								temp = kitConfig.getConfigurationSection("Flags").getValues(false);
								kit.setFlags("global", unmarshalFlags(temp));
							}

							kitConfig.set("Flags." + world, null);
						}
					}

					if (temp != null) {
						kit.save();
					}
				}
				kitArrayList.add(kit);
			}
		}

		instance.log(ChatColor.GREEN + "Loaded " + kitArrayList.size() + " kit(s).");
	}

	private Map<Flag<?>, Object> unmarshalFlags(Map<String, Object> rawValues) {
		checkNotNull(rawValues, "rawValues");

		ConcurrentMap<Flag<?>, Object> values = Maps.newConcurrentMap();

		for (Map.Entry<String, Object> entry : rawValues.entrySet()) {
			Flag<?> flag = DefaultFlags.fuzzyMatchFlag(entry.getKey());

			if (flag != null) {
				try {
					values.put(flag, flag.unmarshal(entry.getValue()));
				} catch (Exception e) {
					instance.log(ChatColor.RED + "Error: " + e.getMessage());
					instance.log(ChatColor.RED + "Failed to unmarshal flag value for " + flag);
				}
			}
		}
		return values;
	}

	public Map<String, Object> marshal(Map<Flag<?>, Object> values) {
		checkNotNull(values, "values");

		Map<String, Object> rawValues = Maps.newHashMap();
		for (Map.Entry<Flag<?>, Object> entry : values.entrySet()) {
			try {
				rawValues.put(entry.getKey().getName(), marshal(entry.getKey(), entry.getValue()));
			} catch (Exception e) {
				instance.log(ChatColor.RED + "Error: " + e.getMessage());
				instance.log(ChatColor.RED + "Failed to marshal flag value for " + entry.getKey() + "; value is " + entry.getValue());
			}
		}

		return rawValues;
	}

	@SuppressWarnings("unchecked") private <T> Object marshal(Flag<T> flag, Object value) {
		return flag.marshal((T) value);
	}
}
