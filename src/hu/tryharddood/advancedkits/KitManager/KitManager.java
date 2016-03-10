package hu.tryharddood.advancedkits.KitManager;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Variables;
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

import static hu.tryharddood.advancedkits.Phrases.phrase;


public class KitManager
{
    private static final ArrayList<Kit> Kits = new ArrayList<>();

    public static Kit getKit(String kitname)
    {
        for (Kit kit : Kits)
        {
            if (kit.getKitname().equalsIgnoreCase(kitname))
            {
                return kit;
            }
        }
        return null;
    }

    public static void load()
    {
        YamlConfiguration configuration;
        final File folder = new File(AdvancedKits.getInstance().getDataFolder() + File.separator + "kits");

        if (!folder.isDirectory())
        {
            AdvancedKits.log(ChatColor.GOLD + "kits folder not found. Creating...");
            folder.mkdirs();
            return;
        }

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles.length == 0)
        {
            AdvancedKits.log(ChatColor.RED + "Can't find any kit.");
            return;
        }

        String name;
        int pos;

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
                } catch (IOException | InvalidConfigurationException e)
                {
                    e.printStackTrace();
                }
            }
        }
        AdvancedKits.log(ChatColor.GREEN + "" + Kits.size() + " kit loaded");
    }

    private static void getProperties(String name, YamlConfiguration configuration)
    {
        Kit kit = new Kit(name);
        if (Kits.contains(kit))
        {
            return;
        }
        kit.setSave(true);
        if (configuration.getList("Items") != null)
        {
            for (Object object : configuration.getList("Items"))
            {
                try
                {
                    kit.AddItem(ItemStack.deserialize((Map<String, Object>) object));
                } catch (ClassCastException e)
                {
                    AdvancedKits.log(ChatColor.RED + "Error loading: " + object.toString());
                }
            }
        }
        else
        {
            AdvancedKits.log(ChatColor.RED + "Error when trying to load " + name);
            AdvancedKits.log(ChatColor.RED + "- No items found.");
            return;
        }

        if (configuration.getString("Flags.PermissionOnly") != null)
        {
            kit.setPermissionOnly(configuration.getBoolean("Flags.PermissionOnly"));
        }
        else
        {
            kit.setPermissionOnly(false);
        }

        if (configuration.getString("Flags.Permission") != null)
        {
            kit.setPermission(configuration.getString("Flags.Permission"));
        }
        else
        {
            kit.setPermission("advancedkits.kit." + name);
        }

        if (configuration.getString("Flags.Visible") != null)
        {
            kit.setVisible(configuration.getBoolean("Flags.Visible"));
        }
        else
        {
            kit.setVisible(true);
        }

        if (configuration.getString("Flags.Uses") != null)
        {
            kit.setUses(configuration.getInt("Flags.Uses"));
        }
        else
        {
            kit.setUses(0);
        }

        if (configuration.getString("Flags.Icon") != null)
        {
            kit.setIcon(Material.matchMaterial(configuration.getString("Flags.Icon")));
        }
        else
        {
            kit.setIcon(Material.EMERALD_BLOCK);
        }

        if (configuration.getString("Flags.Delay") != null)
        {
            kit.setDelay(configuration.getDouble("Flags.Delay"));
        }
        else
        {
            kit.setDelay(0);
        }

        if (configuration.getString("Flags.Cost") != null)
        {
            kit.setCost(configuration.getInt("Flags.Cost"));
        }
        else
        {
            kit.setCost(0);
        }

        if (configuration.getStringList("Flags.World") != null)
        {
            for (String world : configuration.getStringList("Flags.World"))
            {
                kit.AddWorld(world);
            }
        }

        if (configuration.getStringList("Flags.Commands") != null)
        {
            for (String command : configuration.getStringList("Flags.Commands"))
            {
                kit.AddCommand(command);
            }
        }

        if (configuration.getString("Flags.Displayname") != null)
        {
            kit.setDisplayname(configuration.getString("Flags.Displayname"));
        }
        else
        {
            kit.setDisplayname(name);
        }
        kit.setSave(false);
        Kits.add(kit);
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

    public static void setUses(Kit kit, Player player, int uses)
    {
        YamlConfiguration config = kit.getYaml();

        config.set("Uses." + player.getName(), uses);
    }

    public static boolean getUnlocked(Kit kit, String player)
    {
        YamlConfiguration config = kit.getYaml();

        return config != null && config.contains("Unlocked." + player) && config.getBoolean("Unlocked." + player);
    }

    public static void setUnlocked(Kit kit, String player)
    {
        YamlConfiguration config = kit.getYaml();

        config.set("Unlocked." + player, true);
        try
        {
            config.save(kit.getSaveFile());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String getDelay(Player player, Kit kit)
    {
        YamlConfiguration config = kit.getYaml();

        if (config == null)
        {
            return null;
        }

        Long delay = config.getLong("LastUse." + player.getName());
        Date date = new Date(delay);
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return DATE_FORMAT.format(date);
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
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void deleteKit(Kit kit)
    {
        File config = kit.getSaveFile();

        if (config.delete())
        {
            AdvancedKits.log(ChatColor.GREEN + kit.getDisplayname() + " has been deleted.");
        }
        else
        {
            AdvancedKits.log(ChatColor.RED + kit.getDisplayname() + " could not be deleted.");
        }
        KitManager.load();
    }

    public static List<Kit> getKits()
    {
        return Kits;
    }

    public static boolean canUse(Player player, Kit kit)
    {
        if (CheckCooldown(player, kit))
        {
            if (!kit.getWorlds().contains(player.getWorld().getName()))
            {
                if (kit.getUses() > 0)
                {
                    if (kit.getUses() - getUses(kit, player) > 0)
                    {
                        if (AdvancedKits.getInstance().getConfiguration().isEconomy())
                        {
                            if (getUnlocked(kit, player.getName()))
                            {
                                return true;
                            }
                        }
                        else
                        {
                            return true;
                        }
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        else
        {
            if (player.hasPermission(Variables.KITDELAY_BYPASS))
            {
                if (!kit.getWorlds().contains(player.getWorld().getName()))
                {
                    if (AdvancedKits.getInstance().getConfiguration().isEconomy())
                    {
                        if (getUnlocked(kit, player.getName()))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean canBuy(Player player, Kit kit)
    {
        if (!AdvancedKits.getInstance().getConfiguration().isEconomy()) return false;
        if (getUnlocked(kit, player.getName())) return false;

        double money = AdvancedKits.econ.getBalance(player.getName());
        int cost = kit.getCost();

        return (money - cost) >= 0 && (!kit.isPermissionOnly() || player.hasPermission(kit.getPermission()));
    }

    public static List<String> getLores(Player player, Kit kit)
    {
        List<String> list = new ArrayList<>();

        if (kit.getUses() > 0 && (kit.getUses() - KitManager.getUses(kit, player) > 0))
        {
            list.add(ChatColor.RED + "");
            list.add(ChatColor.RED + "" + ChatColor.BOLD + phrase("kit_time_use", (kit.getUses() - KitManager.getUses(kit, player))));
        }

        if (AdvancedKits.getInstance().getConfiguration().isEconomy())
        {
            if (KitManager.getUnlocked(kit, player.getName()))
            {
                list.add(ChatColor.GREEN + "");
                list.add(ChatColor.GREEN + "" + ChatColor.BOLD + phrase("unlocked"));
            }
            else
            {
                list.add(ChatColor.GREEN + "");
                list.add(ChatColor.RED + "" + ChatColor.BOLD + phrase("locked"));
            }

            if (kit.getCost() == 0)
            {
                list.add(ChatColor.GREEN + "");
                list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Cost: " + ChatColor.WHITE + "" + ChatColor.BOLD + "FREE");
            }
            else
            {
                if ((AdvancedKits.econ.getBalance(player.getName()) - kit.getCost()) >= 0)
                {
                    list.add(ChatColor.GREEN + "");
                    list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Cost: " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCost());
                }
                else
                {
                    list.add(ChatColor.GREEN + "");
                    list.add(ChatColor.RED + "" + ChatColor.BOLD + "Cost: " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCost());
                }
            }
        }

        if (kit.isPermissionOnly() && !player.hasPermission(kit.getPermission()))
        {
            list.add(ChatColor.GREEN + "");
            list.add(ChatColor.RED + "" + ChatColor.BOLD + "You don't have the permission for this kit");
        }

        if (kit.getDelay() > 0)
        {
            list.add(ChatColor.GREEN + "");
            list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Delay: " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getDelay() + " hour(s)");
        }

        if (kit.getWorlds().size() > 0)
        {
            list.add(ChatColor.GREEN + "");
            list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Banned in these world(s):");
            for (String s : kit.getWorlds())
            {
                list.add(ChatColor.WHITE + "" + ChatColor.BOLD + " - " + s);
            }
        }

        if (kit.getCommands().size() > 0)
        {
            if (player.hasPermission(Variables.KITADMIN_PERMISSION))
            {
                list.add(ChatColor.GREEN + "");
                list.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Added command(s):");
                for (String s : kit.getCommands())
                {
                    list.add(ChatColor.WHITE + "" + ChatColor.BOLD + " - " + s);
                }
            }
        }

        list.add(ChatColor.GREEN + "");

        return list;
    }
}