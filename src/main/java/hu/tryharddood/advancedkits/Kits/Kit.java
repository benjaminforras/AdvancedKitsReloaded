package hu.tryharddood.advancedkits.Kits;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Kit {
	private final Object[] flags = new Object[Flags.values().length];

	private final YamlConfiguration saveFile;
	private       String            kitname;

	private ArrayList<ItemStack> itemStacks = new ArrayList<>();
	private ArrayList<ItemStack> armor      = new ArrayList<>();

	//private Boolean visible;
	//private Boolean permonly;
	//private Boolean clearinv;
	//private Boolean firstjoin;
	private Boolean save = false;

	//private String permission;

	//private Material icon;

	//private Integer cost;
	//private Integer uses;

	//private Double delay;

	private ArrayList<String> worlds   = new ArrayList<>();
	private ArrayList<String> commands = new ArrayList<>();

	public Kit(String kitname) {
		this.kitname = kitname;
		saveFile = YamlConfiguration.loadConfiguration(getSaveFile());
	}

	public String getName() {
		return this.kitname;
	}

	public ArrayList<ItemStack> getItemStacks() {
		return this.itemStacks;
	}

	public Boolean AddItem(ItemStack a) {
		itemStacks.add(a);

		List<Map<String, Object>> list = itemStacks.stream().map(ItemStack::serialize).collect(Collectors.toList());
		setProperty("Items", list);
		return true;
	}

	public void createKit(List<ItemStack> itemStacks, List<ItemStack> armors) {
		setSave(true);
		setVisible(true);
		setPermonly(false);
		setClearinv(false);
		setFirstjoin(false);
		setCost(0);
		setDelay(0.0);
		setIcon(Material.EMERALD_BLOCK);
		itemStacks.forEach(this::AddItem);
		armors.forEach(this::AddArmor);
		setSave(false);
		AdvancedKits.getKitManager().load();
	}

	public ArrayList<ItemStack> getArmor() {
		return this.armor;
	}

	public void setArmor(ArrayList<ItemStack> armor) {
		this.armor = armor;

		List<Map<String, Object>> list = this.armor.stream().map(ItemStack::serialize).collect(Collectors.toList());
		setProperty("Armor", list);
	}

	public Boolean AddArmor(ItemStack a) {
		this.armor.add(a);

		List<Map<String, Object>> list = this.armor.stream().map(ItemStack::serialize).collect(Collectors.toList());
		setProperty("Armor", list);
		return true;
	}

	public File getSaveFile() {
		File file = new File(AdvancedKits.getInstance().getDataFolder() + File.separator + "kits" + File.separator + kitname + ".yml");
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
				AdvancedKits.log(" -- StackTrace --");
				e.printStackTrace();
				System.out.println(" -- End of StackTrace --");
			}
		}
		return file;
	}

	public YamlConfiguration getYaml() {
		return this.saveFile;
	}

	private void setProperty(String key, Object value) {
		if (!isSave())
		{
			return;
		}

		try
		{
			saveFile.load(getSaveFile());
			saveFile.set(key, value);
			saveFile.save(getSaveFile());
		} catch (Exception e)
		{
			AdvancedKits.log("Couldn't set property '" + key + "' for '" + this.kitname + "'");
			AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
			AdvancedKits.log(" -- StackTrace --");
			e.printStackTrace();
			System.out.println(" -- End of StackTrace --");
		}
	}

	public ArrayList<String> getWorlds() {
		return this.worlds;
	}

	public void AddWorld(String w1) {
		if (!this.worlds.contains(w1))
		{
			this.worlds.add(w1);
		}

		setProperty("Flags.Worlds", this.worlds);
	}

	public void RemoveWorld(String w1) {
		if (this.worlds.contains(w1))
		{
			this.worlds.remove(w1);
		}

		setProperty("Flags.Worlds", this.worlds);
	}

	public ArrayList<String> getCommands() {
		return this.commands;
	}

	public void AddCommand(String w1) {
		if (!this.commands.contains(w1))
		{
			this.commands.add(w1);
		}

		setProperty("Flags.Commands", this.commands);
	}

	public void RemoveCommand(String w1) {
		if (this.commands.contains(w1))
		{
			this.commands.remove(w1);
		}

		setProperty("Flags.Commands", commands);
	}

	private Boolean isSave() {
		return save;
	}

	public void setSave(Boolean save) {
		this.save = save;
	}

	public Boolean isVisible() {
		//return visible;

		return (Boolean) getFlag(Flags.VISIBLE, true);
	}

	public void setVisible(Boolean visible) {
		//this.visible = visible;
		//setProperty("Flags.Visible", visible);

		setFlag(Flags.VISIBLE, visible);
	}

	public Boolean isPermonly() {
		//return permonly;

		return (Boolean) getFlag(Flags.PERMONLY, false);
	}

	public void setPermonly(Boolean permonly) {
		//this.permonly = permonly;
		//setProperty("Flags.PermissionOnly", permonly);

		setFlag(Flags.PERMONLY, permonly);
	}

	public Boolean isClearinv() {
		//return clearinv;

		return (Boolean) getFlag(Flags.CLEARINV, false);
	}

	public void setClearinv(Boolean clearinv) {
		//this.clearinv = clearinv;
		//setProperty("Flags.ClearInv", clearinv);

		setFlag(Flags.CLEARINV, clearinv);
	}

	public Boolean getDefaultUnlock() {
		return (Boolean) getFlag(Flags.UNLOCKED, false);
	}

	public void setDefaultUnlock(Boolean defaultUnlock) {
		setFlag(Flags.UNLOCKED, defaultUnlock);
	}

	public String getPermission() {
		//return permission;

		return (String) getFlag(Flags.PERMISSION, Variables.KIT_USE_KIT_PERMISSION.replaceAll("[kitname]", getName()));
	}

	public void setPermission(String permission) {
		//this.permission = permission;
		//setProperty("Flags.Permission", permission);

		setFlag(Flags.PERMISSION, permission);
	}

	public String getDisplayName() {

		return (String) getFlag(Flags.DISPLAYNAME, getName());
	}

	public void setDisplayName(String arg1) {
		setFlag(Flags.DISPLAYNAME, arg1);
	}

	public Material getIcon() {
		//return icon;

		return Material.matchMaterial(getFlag(Flags.ICON, Material.EMERALD_BLOCK.toString()).toString());
	}

	public void setIcon(Material icon) {
		//this.icon = icon;
		//setProperty("Flags.Icon", icon.toString());

		setFlag(Flags.ICON, icon.toString());
	}

	public Integer getCost() {
		//return cost;

		return (Integer) getFlag(Flags.COST, 0);
	}

	public void setCost(Integer cost) {
		//this.cost = cost;
		//setProperty("Flags.Cost", cost);

		setFlag(Flags.COST, cost);
	}

	public Integer getUses() {
		//return uses;

		return (Integer) getFlag(Flags.USES, 0);
	}

	public void setUses(Integer uses) {
		//this.uses = uses;
		//setProperty("Flags.Uses", uses);

		setFlag(Flags.USES, uses);
	}

	public Double getDelay() {
		//return delay;

		return (Double) getFlag(Flags.DELAY, 0.0);
	}

	public void setDelay(Double delay) {
		//this.delay = delay;
		//setProperty("Flags.Delay", delay);

		setFlag(Flags.DELAY, delay);
	}

	public Boolean isFirstjoin() {
		return (Boolean) getFlag(Flags.FIRSTJOIN, false);
	}

	public void setFirstjoin(Boolean firstjoin) {
		//this.firstjoin = firstjoin;
		//setProperty("Flags.FirstJoin", firstjoin);

		setFlag(Flags.FIRSTJOIN, firstjoin);
	}

	public void setFlag(Flags flag, Object value) {
		this.flags[flag.getId()] = value;
		setProperty("Flags." + flag.toString(), value);
	}

	public Object getFlag(Flags flag, Object defvalue) {
		if (this.flags[flag.getId()] == null)
		{
			return defvalue;
		}
		return this.flags[flag.getId()];
	}

	public Object getFlag(Flags flag) {
		return getFlag(flag, null);
	}
}
