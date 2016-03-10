package hu.tryharddood.advancedkits.KitManager;

import hu.tryharddood.advancedkits.AdvancedKits;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class Kit
{
    private final YamlConfiguration saveFile;

    private String kitname;
    private String displayname;
    private String permission;

    private ArrayList<String> world = new ArrayList<>();
    private ArrayList<String> commands = new ArrayList<>();
    private ArrayList<ItemStack> itemStacks = new ArrayList<>();
    private Material icon;

    private boolean permissionOnly;
    private boolean visible;
    private boolean save = false;

    private int uses;
    private int cost;
    private double delay;

    public Kit(String kitname)
    {
        this.kitname = kitname;
        this.saveFile = YamlConfiguration.loadConfiguration(getSaveFile());
    }

    public String getDisplayname()
    {
        return ChatColor.translateAlternateColorCodes('&', this.displayname);
    }

    public void setDisplayname(String displayname)
    {
        this.displayname = displayname;
        setProperty("Flags." + "Displayname", displayname);
    }

    public void createKit(List<ItemStack> itemStacks)
    {
        setSave(true);

        setDisplayname(this.kitname);
        setVisible(true);
        setIcon(Material.EMERALD_BLOCK);

        for (ItemStack item : itemStacks)
            AddItem(item);

        setSave(false);
        KitManager.load();
    }

    public String getKitname()
    {
        return this.kitname;
    }

    public String getPermission()
    {
        return this.permission;
    }

    public void setPermission(String permission)
    {
        this.permission = permission;
        setProperty("Flags." + "Permission", permission);
    }

    public List<String> getWorlds()
    {
        return this.world;
    }

    public void AddWorld(String world)
    {
        this.world.add(world);

        setProperty("Flags." + "World", this.world);
    }

    public void RemoveWorld(String world)
    {
        if (this.world.contains(world)) this.world.remove(world);

        setProperty("Flags." + "World", this.world);
    }

    public List<String> getCommands()
    {
        return this.commands;
    }

    public void AddCommand(String command)
    {
        this.commands.add(command);

        setProperty("Flags." + "Commands", this.commands);
    }

    public void RemoveCommand(String command)
    {
        if (this.commands.contains(command)) this.commands.remove(command);

        setProperty("Flags." + "Commands", this.commands);
    }

    public Material getIcon()
    {
        return this.icon;
    }

    public void setIcon(Material icon)
    {
        this.icon = icon;
        setProperty("Flags." + "Icon", icon.toString());
    }

    public boolean AddItem(ItemStack a)
    {
        this.itemStacks.add(a);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ItemStack i : this.itemStacks)
        {
            list.add(i.serialize());
        }
        setProperty("Items", list);
        return true;
    }

    public ArrayList<ItemStack> getItemStacks()
    {
        return this.itemStacks;
    }

    public boolean isPermissionOnly()
    {
        return this.permissionOnly;
    }

    public void setPermissionOnly(boolean permissionOnly)
    {
        this.permissionOnly = permissionOnly;
        setProperty("Flags." + "PermissionOnly", permissionOnly);
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
        setProperty("Flags." + "Visible", visible);
    }

    public int getCost()
    {
        return this.cost;
    }

    public void setCost(int cost)
    {
        this.cost = cost;
        setProperty("Flags." + "Cost", cost);
    }

    public double getDelay()
    {
        return this.delay;
    }

    public void setDelay(double delay)
    {
        this.delay = delay;
        setProperty("Flags." + "Delay", delay);
    }

    private void setProperty(String key, Object value)
    {
        if (isSave())
        {
            try
            {
                this.saveFile.load(getSaveFile());
                this.saveFile.set(key, value);
                this.saveFile.save(getSaveFile());
            } catch (Exception e)
            {
                e.printStackTrace();
                AdvancedKits.log("Couldn't set property '" + key + "' for zone '" + getKitname() + "'");
            }
        }
    }

    public boolean isSave()
    {
        return this.save;
    }

    public void setSave(boolean save)
    {
        this.save = save;
    }

    public void setWorld(ArrayList<String> world)
    {
        this.world = world;
    }

    public YamlConfiguration getYaml()
    {
        return this.saveFile;
    }

    public File getSaveFile()
    {
        File file = new File(AdvancedKits.getInstance().getDataFolder() + File.separator + "kits" + File.separator + this.kitname + ".yml");
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return file;
    }

    public int getUses()
    {
        return this.uses;
    }

    public void setUses(int uses)
    {
        this.uses = uses;
        setProperty("Flags." + "Uses", uses);
    }
}
