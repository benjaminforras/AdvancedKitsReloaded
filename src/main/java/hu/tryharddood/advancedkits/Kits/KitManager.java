package hu.tryharddood.advancedkits.Kits;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


public class KitManager {
	private final HashMap<String, Kit> Kits = new HashMap<>();
	private JavaPlugin plugin;

	public KitManager(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean canBuy(Player player, Kit kit) {
		if (!AdvancedKits.getConfiguration().isEconomy())
		{
			return false;
		}

		if (kit.getDefaultUnlock())
		{
			return false;
		}

		if (getUnlocked(kit, player))
		{
			return false;
		}

		double money = AdvancedKits.econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
		int    cost  = kit.getCost();

		if ((money - cost) >= 0)
		{
			if (player.hasPermission(kit.getPermission()))
			{
				return true;
			}
			return true;
		}
		return false;
	}

	public boolean canUse(Player player, Kit kit) {
		if (player.hasPermission(kit.getPermission()))
		{
			if (!AdvancedKits.getConfiguration().isEconomy() || AdvancedKits.getConfiguration().isEconomy() && (kit.getDefaultUnlock() || getUnlocked(kit, player)))
			{
				if (!kit.getWorlds().contains(player.getWorld().getName()))
				{
					if (kit.getUses() == 0 || (kit.getUses() > 0 && (kit.getUses() - getUses(kit, player)) < 0) && player.isOp() || kit.getUses() > 0 && ((kit.getUses() - getUses(kit, player)) > 0))
					{
						if (CheckCooldown(player, kit) || !CheckCooldown(player, kit) && player.hasPermission(Variables.KITDELAY_BYPASS))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void deleteKit(Kit kit) {
		File config = kit.getSaveFile();

		if (config.delete())
		{
			AdvancedKits.log(ChatColor.GREEN + kit.getName() + " has been deleted.");
		}
		else
		{
			AdvancedKits.log(ChatColor.RED + kit.getName() + " could not be deleted.");
		}
		load();
	}


	public Kit getKit(String kitname) {
		for (Map.Entry<String, Kit> kit : Kits.entrySet())
		{
			if (kit.getKey().equalsIgnoreCase(kitname))
			{
				return kit.getValue();
			}
		}
		return null;
	}

	public Kit getKitByDisplayName(String displayName) {
		for (Map.Entry<String, Kit> kit : Kits.entrySet())
		{
			if (displayName.startsWith("&f"))
			{
				displayName = displayName.substring(2);
			}
			if (kit.getValue().getDisplayName().equals(displayName))
				return kit.getValue();
		}
		return null;
	}

	public HashMap<String, Kit> getKits() {
		return Kits;
	}

	public List<String> getLores(Player player, Kit kit) {
		List<String> list = new ArrayList<>();

		if (kit.getUses() > 0 && (kit.getUses() - getUses(kit, player) > 0))
		{
			list.add(ChatColor.RED + "");
			list.add(ChatColor.RED + "" + ChatColor.BOLD + tl("kit_time_use", (kit.getUses() - getUses(kit, player))));
		}

		if (AdvancedKits.getConfiguration().isEconomy())
		{
			if (kit.getDefaultUnlock() || getUnlocked(kit, player))
			{
				list.add(ChatColor.GREEN + "");
				list.add(ChatColor.GREEN + "" + ChatColor.BOLD + tl("unlocked"));
			}
			else
			{
				list.add(ChatColor.GREEN + "");
				list.add(ChatColor.RED + "" + ChatColor.BOLD + tl("locked"));
			}

			if (kit.getCost() == 0)
			{
				list.add(ChatColor.GREEN + "");
				list.add(ChatColor.GREEN + "" + ChatColor.BOLD + tl("cost") + ": " + ChatColor.WHITE + "" + ChatColor.BOLD + tl("free", true));
			}
			else
			{
				if ((AdvancedKits.econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) - kit.getCost()) >= 0)
				{
					list.add(ChatColor.GREEN + "");
					list.add(ChatColor.GREEN + "" + ChatColor.BOLD + tl("cost") + ": " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCost());
				}
				else
				{
					list.add(ChatColor.GREEN + "");
					list.add(ChatColor.RED + "" + ChatColor.BOLD + tl("cost") + ": " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCost());
				}
			}
		}

		if (!player.hasPermission(kit.getPermission()))
		{
			list.add(ChatColor.GREEN + "");
			list.add(ChatColor.RED + "" + ChatColor.BOLD + tl("error_no_permission"));
		}

		if (kit.getDelay() > 0)
		{
			list.add(ChatColor.GREEN + "");
			list.add(ChatColor.GREEN + "" + ChatColor.BOLD + tl("delay") + ": " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getDelay() + " " + tl("seconds"));
		}

		if (kit.getWorlds().size() > 0)
		{
			list.add(ChatColor.GREEN + "");
			list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Banned in these world(s):");
			list.addAll(kit.getWorlds().stream().map(s -> ChatColor.WHITE + "" + ChatColor.BOLD + " - " + s).collect(Collectors.toList()));
		}

		if (kit.getCommands().size() > 0)
		{
			if (player.hasPermission(Variables.KITADMIN_PERMISSION))
			{
				list.add(ChatColor.GREEN + "");
				list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Added command(s):");
				list.addAll(kit.getCommands().stream().map(s -> ChatColor.WHITE + "" + ChatColor.BOLD + " - " + s).collect(Collectors.toList()));
			}
		}

		list.add(ChatColor.GREEN + "");

		return list;
	}

	@SuppressWarnings("unchecked")
	private void getProperties(String name, YamlConfiguration configuration) {
		if (Kits.containsKey(name))
		{
			return;
		}
		Kit kit = new Kit(name);

		if (configuration.getList("Items") != null)
		{
			for (Object object : configuration.getList("Items"))
			{
				try
				{
					kit.AddItem(ItemStack.deserialize((Map<String, Object>) object));
				} catch (ClassCastException | NullPointerException e)
				{
					AdvancedKits.log(ChatColor.GOLD + "=====================================================");
					AdvancedKits.log(ChatColor.RED + "Error loading: " + name);
					AdvancedKits.log(ChatColor.RED + "- Invalid item:");
					AdvancedKits.log(ChatColor.RED + "" + object.toString());
					AdvancedKits.log(ChatColor.GOLD + "=====================================================");
					AdvancedKits.log(ChatColor.GREEN + "Skipping " + name);
					return;
				}
			}
		}
		else
		{
			AdvancedKits.log(ChatColor.RED + "Error when trying to load " + name);
			AdvancedKits.log(ChatColor.RED + "- No items found.");
			return;
		}

		if (configuration.getList("Armor") != null)
		{
			for (Object object : configuration.getList("Armor"))
			{
				try
				{
					kit.AddArmor(ItemStack.deserialize((Map<String, Object>) object));
				} catch (ClassCastException e)
				{
					AdvancedKits.log(ChatColor.GOLD + "=====================================================");
					AdvancedKits.log(ChatColor.RED + "Error loading: " + name);
					AdvancedKits.log(ChatColor.RED + "- Invalid item:");
					AdvancedKits.log(ChatColor.RED + "" + object.toString());
					AdvancedKits.log(ChatColor.GOLD + "=====================================================");
					AdvancedKits.log(ChatColor.GREEN + "Skipping item");
				}
			}
		}

		if (configuration.getConfigurationSection("Flags") != null)
		{
			for (String s : configuration.getConfigurationSection("Flags").getKeys(false))
			{
				for (Flags flag : Flags.values())
				{
					if (s.equalsIgnoreCase(flag.toString()))
					{
						kit.setFlag(flag, configuration.get("Flags." + s));
					}
				}
			}
		}

		if (configuration.getStringList("Flags.World") != null)
		{
			configuration.getStringList("Flags.World").forEach(kit::AddWorld);
		}

		if (configuration.getStringList("Flags.Commands") != null)
		{
			configuration.getStringList("Flags.Commands").forEach(kit::AddCommand);
		}

		Kits.put(name, kit);
	}

	public void load() {
		YamlConfiguration configuration;
		final File        folder = new File(plugin.getDataFolder() + File.separator + "kits");

		if (!folder.isDirectory())
		{
			AdvancedKits.log(ChatColor.GREEN + "- kits folder not found. Creating...");
			folder.mkdirs();
			return;
		}

		File[] listOfFiles = folder.listFiles();

		if (listOfFiles.length == 0)
		{
			AdvancedKits.log(ChatColor.RED + "- Can't find any kit.");
			return;
		}

		String name;
		int    pos;

		Kits.clear();

		for (File file : listOfFiles)
		{
			if (file.isFile())
			{
				try
				{
					name = file.getName();
					pos = name.lastIndexOf(".");
					if (pos > 0)
					{
						name = name.substring(0, pos);
					}

					if (Pattern.compile("[^A-Za-z0-9_]+", Pattern.CASE_INSENSITIVE).matcher(name).find())
					{
						AdvancedKits.log(ChatColor.RED + "Error when trying to load " + name);
						AdvancedKits.log(ChatColor.RED + "- The name contains special charaters.");
						continue;
					}

					configuration = YamlConfiguration.loadConfiguration(file);
					configuration.load(file);

					getProperties(name, configuration);
				} catch (IOException | InvalidConfigurationException e)
				{
					AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
					AdvancedKits.log(" -- StackTrace --");
					e.printStackTrace();
					System.out.println(" -- End of StackTrace --");
				}
			}
		}
		AdvancedKits.log(ChatColor.GREEN + "- " + Kits.size() + " kit loaded");
	}

	public boolean CheckCooldown(Player player, Kit kit) {
		Long delay = Double.valueOf(getProperty(player, kit, Properties.LASTUSE, 0.0).toString()).longValue();
		return System.currentTimeMillis() >= delay;
	}

	public String getDelay(Player player, Kit kit) {
		Long delay = Double.valueOf(getProperty(player, kit, Properties.LASTUSE, 0.0).toString()).longValue();
		Date date  = new Date(delay);
		return printDifference(new Date(System.currentTimeMillis()), date);
	}

	public boolean getUnlocked(Kit kit, Player player) {
		return (Boolean) getProperty(player, kit, Properties.UNLOCKED, false);
	}

	public int getUses(Kit kit, Player player) {
		return (Integer) getProperty(player, kit, Properties.USES, 0);
	}

	public void setDelay(Player player, double delay, Kit kit) {
		setProperty(player, kit, Properties.LASTUSE, (System.currentTimeMillis() + (delay * 1000)));
	}

	public void setUnlocked(Kit kit, Player player) {
		setProperty(player, kit, Properties.UNLOCKED, true);
	}

	public boolean getFirstJoin(Player player, Kit kit) {
		return (Boolean) getProperty(player, kit, Properties.FIRSTJOIN, false);
	}

	public void setFirstJoin(Player player, Kit kit) {
		setProperty(player, kit, Properties.FIRSTJOIN, true);
	}

	public void setUses(Kit kit, Player player, int uses) {
		setProperty(player, kit, Properties.USES, uses);
	}

	private void setProperty(Player player, Kit kit, Properties property, Object value) {
		try
		{
			YamlConfiguration yamlConfiguration = kit.getYaml();
			yamlConfiguration.set(player.getUniqueId() + "." + property.toString(), value);
			yamlConfiguration.save(kit.getSaveFile());
		} catch (IOException e)
		{
			AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
			AdvancedKits.log(" -- StackTrace --");
			e.printStackTrace();
			System.out.println(" -- End of StackTrace --");
		}
	}

	private Object getProperty(Player player, Kit kit, Properties property, Object defvalue) {
		YamlConfiguration yamlConfiguration = kit.getYaml();
		return yamlConfiguration.get(player.getUniqueId() + "." + property.toString(), defvalue);
	}

	public String printDifference(Date startDate, Date endDate) {

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
		if (elapsedDays >= 1)
		{
			sb.append(elapsedDays + " " + tl("days") + " ");
		}

		if (elapsedHours >= 1)
		{
			sb.append(elapsedHours + " " + tl("hours") + " ");
		}

		if (elapsedMinutes >= 1)
		{
			sb.append(elapsedMinutes + " " + tl("minutes") + " ");
		}

		if (elapsedSeconds >= 1)
		{
			sb.append(elapsedSeconds + " " + tl("seconds") + " ");
		}
		return sb.toString();
	}
}
