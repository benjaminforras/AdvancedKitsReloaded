package hu.tryharddood.advancedkits.Kits;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static hu.tryharddood.advancedkits.I18n.tl;


public class KitManager
{
    private static final ArrayList<Kit> Kits = new ArrayList<>();

    public static boolean canBuy(Player player, Kit kit)
    {
        if (!AdvancedKits.getInstance().getConfiguration().isEconomy()) return false;
        if (getUnlocked(kit, player.getName())) return false;

        double money = AdvancedKits.econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
        int    cost  = kit.getCost();

        return (money - cost) >= 0 && (!kit.isPermonly() || player.hasPermission(kit.getPermission()));
    }

    public static boolean canUse(Player player, Kit kit)
    {
        if (!kit.isPermonly() || kit.isPermonly() && player.hasPermission(Variables.KIT_USE_KIT_PERMISSION.replaceAll("[kitname]", kit.getName())))
        {
            if (!AdvancedKits.getInstance().getConfiguration().isEconomy() || AdvancedKits.getInstance().getConfiguration().isEconomy() && getUnlocked(kit, player.getName()))
            {
                if (!kit.getWorlds().contains(player.getWorld().getName()))
                {
                    if (kit.getUses() == 0 || (kit.getUses() > 0 && (kit.getUses() - getUses(kit, player)) <= 0) && player.isOp() || kit.getUses() > 0 && ((kit.getUses() - getUses(kit, player)) > 0))
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

    public static boolean CheckCooldown(Player player, Kit kit)
    {
        YamlConfiguration config = kit.getYaml();

        if (config == null)
        {
            return false;
        }

        Long delay = config.getLong("LastUse." + player.getName());
        return System.currentTimeMillis() >= delay;
    }

    public static void deleteKit(Kit kit)
    {
        File config = kit.getSaveFile();

        if (config.delete())
        {
            AdvancedKits.log(ChatColor.GREEN + kit.getName() + " has been deleted.");
        }
        else
        {
            AdvancedKits.log(ChatColor.RED + kit.getName() + " could not be deleted.");
        }
        KitManager.load();
    }

    public static String getDelay(Player player, Kit kit)
    {
        YamlConfiguration config = kit.getYaml();

        if (config == null)
        {
            return null;
        }

        Long             delay       = config.getLong("LastUse." + player.getName());
        Date             date        = new Date(delay);
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return DATE_FORMAT.format(date);
    }

    public static Kit getKit(String kitname)
    {
        for (Kit kit : Kits)
        {
            if (kit.getName().equalsIgnoreCase(kitname))
            {
                return kit;
            }
        }
        return null;
    }

    public static List<Kit> getKits()
    {
        return Kits;
    }

    public static List<String> getLores(Player player, Kit kit)
    {
        List<String> list = new ArrayList<>();

        if (kit.getUses() > 0 && (kit.getUses() - KitManager.getUses(kit, player) > 0))
        {
            list.add(ChatColor.RED + "");
            list.add(ChatColor.RED + "" + ChatColor.BOLD + tl("kit_time_use", (kit.getUses() - KitManager.getUses(kit, player))));
        }

        if (AdvancedKits.getInstance().getConfiguration().isEconomy())
        {
            if (KitManager.getUnlocked(kit, player.getName()))
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

        if (kit.isPermonly() && !player.hasPermission(kit.getPermission()))
        {
            list.add(ChatColor.GREEN + "");
            list.add(ChatColor.RED + "" + ChatColor.BOLD + tl("error_no_permission"));
        }

        if (kit.getDelay() > 0)
        {
            list.add(ChatColor.GREEN + "");
            list.add(ChatColor.GREEN + "" + ChatColor.BOLD + tl("delay") + ": " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getDelay() + " hour(s)");
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
    private static void getProperties(String name, YamlConfiguration configuration)
    {
        Kit kit = new Kit(name);
        if (Kits.contains(kit))
        {
            return;
        }

        if (configuration.getList("Items") != null)
        {
            for (Object object : configuration.getList("Items"))
            {
                try
                {
                    kit.AddItem(ItemStack.deserialize((Map<String, Object>) object));
                }
                catch (ClassCastException | NullPointerException e)
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
                }
                catch (ClassCastException e)
                {
                    AdvancedKits.log(ChatColor.GOLD + "=====================================================");
                    AdvancedKits.log(ChatColor.RED + "Error loading: " + name);
                    AdvancedKits.log(ChatColor.RED + "- Invalid item:");
                    AdvancedKits.log(ChatColor.RED + "" + object.toString());
                    AdvancedKits.log(ChatColor.GOLD + "=====================================================");
                    AdvancedKits.log(ChatColor.GREEN + "Skipping item");
                    continue;
                }
            }
        }

        kit.setPermonly(configuration.getBoolean("Flags.PermissionOnly", false));

        kit.setPermission(configuration.getString("Flags.Permission", Variables.KIT_USE_KIT_PERMISSION.replaceAll("[kitname]", name)));

        kit.setVisible(configuration.getBoolean("Flags.Visible", true));

        kit.setClearinv(configuration.getBoolean("Flags.ClearInv", false));

        kit.setClearinv(configuration.getBoolean("Flags.FirstJoin", false));

        kit.setUses(configuration.getInt("Flags.Uses", 0));

        kit.setIcon(Material.matchMaterial(configuration.getString("Flags.Icon", Material.EMERALD_BLOCK.toString())));

        kit.setDelay(configuration.getDouble("Flags.Delay", 0));

        kit.setCost(configuration.getInt("Flags.Cost", 0));

        if (configuration.getStringList("Flags.World") != null)
        {
            configuration.getStringList("Flags.World").forEach(kit::AddWorld);
        }

        if (configuration.getStringList("Flags.Commands") != null)
        {
            configuration.getStringList("Flags.Commands").forEach(kit::AddCommand);
        }

        Kits.add(kit);
    }

    public static boolean getUnlocked(Kit kit, String player)
    {
        YamlConfiguration config = kit.getYaml();

        return config != null && config.contains("Unlocked." + player) && config.getBoolean("Unlocked." + player);
    }

    public static int getUses(Kit kit, Player player)
    {
        YamlConfiguration config = kit.getYaml();
        if (!config.contains("Uses." + player.getName()))
        {
            return 0;
        }
        return config.getInt("Uses." + player.getName());
    }

    public static void load()
    {
        YamlConfiguration configuration;
        final File        folder = new File(AdvancedKits.getInstance().getDataFolder() + File.separator + "kits");

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

                    configuration = YamlConfiguration.loadConfiguration(file);
                    configuration.load(file);

                    getProperties(name, configuration);
                }
                catch (IOException | InvalidConfigurationException e)
                {
                    e.printStackTrace();
                }
            }
        }
        AdvancedKits.log(ChatColor.GREEN + "- " + Kits.size() + " kit loaded");
    }

    public static void setDelay(Player player, double delay, Kit kit)
    {
        YamlConfiguration config = kit.getYaml();

        if (config == null)
        {
            return;
        }

        config.set("LastUse." + player.getName(), (System.currentTimeMillis() + (delay * 3600000)));
        try
        {
            config.save(kit.getSaveFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void setUnlocked(Kit kit, String player)
    {
        YamlConfiguration config = kit.getYaml();

        config.set("Unlocked." + player, true);
        try
        {
            config.save(kit.getSaveFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean getFirstJoin(Player player, Kit kit)
    {
        YamlConfiguration config = kit.getYaml();
        return config.getBoolean("FirstJoin." + player.getUniqueId(), false);
    }

    public static void setFirstJoin(Player player, Kit kit)
    {
        YamlConfiguration config = kit.getYaml();

        config.set("FirstJoin." + player.getUniqueId(), true);
        try
        {
            config.save(kit.getSaveFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void setUses(Kit kit, Player player, int uses)
    {
        YamlConfiguration config = kit.getYaml();

        config.set("Uses." + player.getName(), uses);
    }
}
