package hu.tryharddood.advancedkits.Commands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Configuration.Configuration;
import hu.tryharddood.advancedkits.KitManager.Kit;
import hu.tryharddood.advancedkits.KitManager.KitManager;
import hu.tryharddood.advancedkits.Utils.TitleAPI.TitleAPI;
import hu.tryharddood.advancedkits.Variables;
import me.libraryaddict.inventory.ItemBuilder;
import me.libraryaddict.inventory.PageInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hu.tryharddood.advancedkits.Phrases.phrase;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class CommandManager implements CommandExecutor
{
    public static Configuration configuration = AdvancedKits.getInstance().getConfiguration();

    private static boolean handleCreateCommand(Player player, String string)
    {
        if (!player.hasPermission(Variables.KITADMIN_PERMISSION))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            return true;
        }

        Kit kit = KitManager.getKit(string);
        if (kit != null)
        {
            return handleKitExists(player);
        }

        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "Create - " + string);

        inventory.setItem(inventorySize - 4, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Create kit").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to create the kit").build());
        inventory.setItem(inventorySize - 6, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setTitle(ChatColor.RED + "" + ChatColor.BOLD + "Cancel").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to cancel the kit creation").build());

        for (int i = 36; i < 45; i++)
        {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
        }
        player.openInventory(inventory);
        return true;
    }

    public static boolean handleKitExists(CommandSender commandSender)
    {
        commandSender.sendMessage(configuration.getChatPrefix() + " " + phrase("error_kit_create_exists"));
        return true;
    }

    public static boolean handleViewCommand(Player player, Kit kit)
    {
        List<ItemStack> itemStackList = kit.getItemStacks();

        int inventorySize = 54;

        PageInventory inv = new PageInventory(player);

        ItemStack[] items = itemStackList.toArray(new ItemStack[inventorySize]);

        if (player.hasPermission(Variables.KITADMIN_PERMISSION))
        {
            items[inventorySize - 9] = new ItemBuilder(Material.BOOK_AND_QUILL).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Edit").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to edit this kit").build();
            items[inventorySize - 1] = new ItemBuilder(Material.BARRIER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Delete").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to delete this kit").build();
        }

        {
            if (KitManager.canUse(player, kit))
            {
                items[inventorySize - 4] = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "USE").build();
            }

            items[inventorySize - 5] = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.WHITE.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Back to the list").addLores(KitManager.getLores(player, kit)).build();


            if (KitManager.canBuy(player, kit))
            {
                items[inventorySize - 6] = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "BUY").build();
            }
        }

        for (int i = 36; i < 45; i++)
        {
            items[i] = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build();
        }

        inv.setPages(items);
        inv.setTitle("Details - " + kit.getKitname());
        inv.openInventory();
        return true;
    }

    public static boolean handleBuyCommand(Player player, Kit kit)
    {
        if (kit.isPermissionOnly() && !player.hasPermission(kit.getPermission()))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            closeGUI(player, "Details");

            return true;
        }

        if (KitManager.getUnlocked(kit, player.getName()))
        {
            sendMessage(player, phrase("error_kitbuy_bought_already"), ChatColor.RED);
            closeGUI(player, "Details");

            return true;
        }

        if (!configuration.isEconomy())
        {
            sendMessage(player, "Economy support disabled..", ChatColor.RED);
            closeGUI(player, "Details");
            return true;
        }

        double balance = AdvancedKits.econ.getBalance(player.getName());
        if ((balance - kit.getCost()) >= 0)
        {
            AdvancedKits.econ.withdrawPlayer(player.getName(), kit.getCost());
            KitManager.setUnlocked(kit, player.getName());

            sendMessage(player, phrase("kitbuy_success_message", kit.getDisplayname()), ChatColor.GREEN);
            closeGUI(player, "Details");

            return true;
        }
        else
        {
            sendMessage(player, phrase("error_kitbuy_not_enough_money"), ChatColor.RED);
            closeGUI(player, "Details");
            return true;
        }
    }

    public static boolean handleUseCommand(Player player, Kit kit)
    {
        if (kit.getUses() > 0 && (kit.getUses() - KitManager.getUses(kit, player)) <= 0)
        {
            sendMessage(player, phrase("cant_use_anymore"), ChatColor.RED);
            closeGUI(player, "Details");

            return true;
        }

        if (configuration.isEconomy() && !KitManager.getUnlocked(kit, player.getName()))
        {
            sendMessage(player, phrase("kituse_error_notunlocked"), ChatColor.RED);
            closeGUI(player, "Details");

            return true;
        }

        if (kit.isPermissionOnly() && !player.hasPermission(kit.getPermission()))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            closeGUI(player, "Details");

            return true;
        }

        if (kit.getDelay() > 0)
        {
            if (!player.hasPermission(Variables.KITDELAY_BYPASS))
            {
                if (!KitManager.CheckCooldown(player, kit))
                {
                    closeGUI(player, "Details");
                    sendMessage(player, phrase("kituse_wait", KitManager.getDelay(player, kit)), ChatColor.RED);
                    return true;
                }
            }
        }

        if (kit.getWorlds().contains(player.getWorld().getName()))
        {
            return handleWorldException(player);
        }
        return handleGivePlayerKit(player, kit);
    }

    private static boolean handleGivePlayerKit(Player player, Kit kit)
    {
        if (kit.getUses() > 0)
        {
            KitManager.setUses(kit, player, (KitManager.getUses(kit, player) + 1));
        }

        Inventory inv = player.getInventory();
        ItemMeta itemMeta;
        for (ItemStack item : kit.getItemStacks())
        {
            if (item.hasItemMeta())
            {
                itemMeta = item.getItemMeta();
                if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().contains("%player%"))
                {
                    itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%player%", player.getName()));
                }

                if (itemMeta.getLore() != null)
                {
                    List<String> lore = itemMeta.getLore();
                    for (int i = 0; i < lore.size(); i++)
                    {
                        lore.set(i, lore.get(i).replaceAll("%player%", player.getName()));
                    }
                    itemMeta.setLore(lore);
                }

                item.setItemMeta(itemMeta);
            }
            inv.addItem(item);
        }
        player.updateInventory();

        KitManager.setDelay(player, kit.getDelay(), kit);
        closeGUI(player, "Details");
        sendMessage(player, phrase("kituse_success"), ChatColor.GREEN);

        for (String command : kit.getCommands())
        {
            Bukkit.dispatchCommand(AdvancedKits.getInstance().getServer().getConsoleSender(), command.replaceAll("%player%", player.getName()));
        }
        return true;
    }

    public static boolean handleKitCommand(CommandSender commandSender)
    {
        Player player = (Player) commandSender;
        if (!player.hasPermission(Variables.KIT_PERMISSION))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            return true;
        }

        PageInventory inv = new PageInventory(player);

        List<Kit> kits = KitManager.getKits();
        ItemStack[] items = new ItemStack[kits.size()];

        Kit kit;
        List<String> lore = new ArrayList<>();
        for (int i = 0; i < kits.size(); i++)
        {
            if (!kits.get(i).isVisible())
            {
                continue;
            }
            lore.clear();

            kit = kits.get(i);
            if (!player.hasPermission(Variables.KITADMIN_PERMISSION) && KitManager.getUses(kit, player) > 0)
            {
                continue;
            }

            if (!KitManager.CheckCooldown(player, kit))
            {
                lore.add("§8");
                lore.add(ChatColor.RED + "" + ChatColor.BOLD + phrase("kituse_wait"));
                lore.add(ChatColor.WHITE + "" + ChatColor.BOLD + "- " + KitManager.getDelay(player, kit));
                lore.add("§8");
            }

            items[i] = new ItemBuilder(kit.getIcon()).setTitle(ChatColor.translateAlternateColorCodes('&', kit.getDisplayname())).addLores(lore).addLores(KitManager.getLores(player, kit)).build();
        }
        inv.setPages(items);
        inv.setTitle("Kits");
        inv.openInventory();

        return true;
    }

    private static boolean handleKitNotFound(CommandSender commandSender)
    {
        sendMessage(commandSender, phrase("error_kit_not_found"), ChatColor.RED);
        return true;
    }

    public static void closeGUI(Player player, String name)
    {
        if (player.getOpenInventory().getTitle().contains(name))
        {
            player.closeInventory();
        }
    }

    private static boolean handleFlagCommand(Player player, Kit kit, String[] strings)
    {
        if (!player.hasPermission(Variables.RELOAD_PERMISSION))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            return true;
        }

        if (strings.length == 1)
        {
            String flag = strings[0];
            if (Arrays.asList("visible", "permonly", "permissiononly", "icon").contains(strings[0]))
            {
                if (flag.equalsIgnoreCase("visible"))
                {
                    boolean visible = kit.isVisible();
                    kit.setSave(true);
                    kit.setVisible(!visible);
                    kit.setSave(false);

                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GOLD + phrase("kitadmin_flag_set"));
                    player.sendMessage(ChatColor.WHITE + "    visibility: " + (!visible ? ChatColor.GREEN + "" + ChatColor.BOLD + "ON" : ChatColor.RED + "" + ChatColor.BOLD + "OFF"));
                    return true;
                }
                else if (flag.equalsIgnoreCase("permonly") || flag.equalsIgnoreCase("permissiononly"))
                {
                    boolean permonly = kit.isPermissionOnly();
                    kit.setSave(true);
                    kit.setPermissionOnly(!permonly);
                    kit.setSave(false);

                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GOLD + phrase("kitadmin_flag_set"));
                    player.sendMessage(ChatColor.WHITE + "    permissionOnly: " + (!permonly ? ChatColor.GREEN + "" + ChatColor.BOLD + "ON" : ChatColor.RED + "" + ChatColor.BOLD + "OFF"));
                    return true;
                }
                else if (flag.equalsIgnoreCase("icon"))
                {
                    ItemStack itemStack = player.getItemInHand();
                    if (itemStack == null || itemStack.getType() == Material.AIR)
                    {
                        player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_wrong_icon") + ": ");
                        player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + "AIR");
                        return true;
                    }

                    kit.setSave(true);
                    kit.setIcon(itemStack.getType());
                    kit.setSave(false);

                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_success_icon") + ":");
                    player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + itemStack.getType().toString());
                    return true;
                }
            }
        }
        else if (strings.length >= 2)
        {
            String flag = strings[0];
            String value = getArgs(strings, 1);

            if (flag.equalsIgnoreCase("visible"))
            {
                if (!value.equalsIgnoreCase("true") || !value.equalsIgnoreCase("false"))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_boolean"));
                    return true;
                }

                boolean visible = kit.isVisible();
                kit.setSave(true);
                kit.setVisible(Boolean.valueOf(value));
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GOLD + phrase("kitadmin_flag_set"));
                player.sendMessage(ChatColor.WHITE + "    visibility: " + (!visible ? ChatColor.GREEN + "" + ChatColor.BOLD + "ON" : ChatColor.RED + "" + ChatColor.BOLD + "OFF"));
                return true;
            }
            else if (flag.equalsIgnoreCase("permonly") || flag.equalsIgnoreCase("permissiononly"))
            {
                if (!value.equalsIgnoreCase("true") || !value.equalsIgnoreCase("false"))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_boolean"));
                    return true;
                }

                boolean permonly = kit.isPermissionOnly();
                kit.setSave(true);
                kit.setPermissionOnly(Boolean.valueOf(value));
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GOLD + phrase("kitadmin_flag_set"));
                player.sendMessage(ChatColor.WHITE + "    permissionOnly: " + (!permonly ? ChatColor.GREEN + "" + ChatColor.BOLD + "ON" : ChatColor.RED + "" + ChatColor.BOLD + "OFF"));
                return true;
            }
            else if (flag.equalsIgnoreCase("displayname"))
            {
                kit.setSave(true);
                kit.setDisplayname(value);
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_displayname_set") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + ChatColor.translateAlternateColorCodes('&', value));
                return true;
            }
            else if (flag.equalsIgnoreCase("permission"))
            {
                if (!kit.isPermissionOnly())
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_permission_wrong"));
                    return true;
                }

                kit.setSave(true);
                kit.setPermission(value);
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_permission_set") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                return true;
            }
            else if (flag.equalsIgnoreCase("addworld"))
            {
                if (kit.getWorlds().contains(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_addworld_wrong") + " " + value);
                    return true;
                }

                kit.setSave(true);
                kit.AddWorld(value);
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_addworld_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getWorlds().toString());
                return true;
            }
            else if (flag.equalsIgnoreCase("removeworld") || flag.equalsIgnoreCase("delworld"))
            {
                if (!kit.getWorlds().contains(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_delworld_wrong") + " " + value);
                    return true;
                }

                kit.setSave(true);
                kit.RemoveWorld(value);
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_delworld_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getWorlds().toString());
                return true;
            }
            else if (flag.equalsIgnoreCase("addcommand"))
            {
                if (kit.getCommands().contains(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_addcommand_wrong") + " " + value);
                    return true;
                }

                kit.setSave(true);
                kit.AddCommand(value);
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_addcommand_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCommands().toString());
                return true;
            }
            else if (flag.equalsIgnoreCase("removecommand") || flag.equalsIgnoreCase("delcommand"))
            {
                if (!kit.getCommands().contains(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_delcommand_wrong") + " " + value);
                    return true;
                }

                kit.setSave(true);
                kit.RemoveCommand(value);
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_delcommand_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCommands().toString());
                return true;
            }
            else if (flag.equalsIgnoreCase("cost") || flag.equalsIgnoreCase("money"))
            {
                if (!isNumeric(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_integer"));
                    return true;
                }

                kit.setSave(true);
                kit.setCost(Integer.valueOf(value));
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_cost_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                return true;
            }
            else if (flag.equalsIgnoreCase("setuse") || flag.equalsIgnoreCase("setuses"))
            {
                if (!isNumeric(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_integer"));
                    return true;
                }

                kit.setSave(true);
                kit.setUses(Integer.valueOf(value));
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_uses_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                return true;
            }
            else if (flag.equalsIgnoreCase("wait") || flag.equalsIgnoreCase("delay"))
            {
                if (!isDouble(value))
                {
                    player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_integer"));
                    return true;
                }

                kit.setSave(true);
                kit.setDelay(Double.parseDouble(value));
                kit.setSave(false);

                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kitadmin_flag_delay_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                return true;
            }
            else
            {
                return handleFlagHelpMessage(player);
            }
        }
        else
        {
            return handleFlagHelpMessage(player);
        }
        return true;
    }

    public static boolean handleDeleteCommand(CommandSender commandSender, Kit kit)
    {
        if (!commandSender.hasPermission(Variables.RELOAD_PERMISSION))
        {
            sendMessage(commandSender, phrase("error_no_permission"), ChatColor.RED);
            return true;
        }

        KitManager.deleteKit(kit);
        commandSender.sendMessage(configuration.getChatPrefix() + " " + phrase("kit_delete"));

        if (commandSender instanceof Player) closeGUI((Player) commandSender, "Details");
        return true;
    }

    public static boolean handleEditCommand(Player player, Kit kit)
    {
        if (!player.hasPermission(Variables.KITADMIN_PERMISSION))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            return true;
        }

        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "Edit - " + kit.getKitname());

        for (ItemStack i : kit.getItemStacks())
        {
            inventory.addItem(i);
        }

        inventory.setItem(inventorySize - 4, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Edit kit").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to create the kit").build());
        inventory.setItem(inventorySize - 6, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setTitle(ChatColor.RED + "" + ChatColor.BOLD + "Cancel").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to cancel the kit creation").build());

        for (int i = 36; i < 45; i++)
        {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
        }
        player.openInventory(inventory);
        return true;
    }

    private static boolean handleHelpMessage(CommandSender commandSender)
    {
        commandSender.sendMessage(ChatColor.BLUE + "======================================");
        commandSender.sendMessage(ChatColor.GREEN + "");
        commandSender.sendMessage(ChatColor.WHITE + "/kit                                      " + ChatColor.GRAY + "Opens up the kit list.");
        commandSender.sendMessage(ChatColor.WHITE + "/kit version                            " + ChatColor.GRAY + "Shows the plugin infos.");
        commandSender.sendMessage(ChatColor.WHITE + "/kit help                                " + ChatColor.GRAY + "Shows all commands.");
        commandSender.sendMessage(ChatColor.WHITE + "/kit buy <kit>                           " + ChatColor.GRAY + "Buys the kit.");
        commandSender.sendMessage(ChatColor.WHITE + "/kit use <kit>                           " + ChatColor.GRAY + "Uses the kit.");
        commandSender.sendMessage(ChatColor.WHITE + "/kit view <kit>                          " + ChatColor.GRAY + "Shows the kit.");

        if (commandSender.hasPermission(Variables.KITADMIN_PERMISSION))
        {
            commandSender.sendMessage(ChatColor.WHITE + "/kit setflag <kit> <flag> [value]     " + ChatColor.GRAY + "Sets a flag for a kit.");
            commandSender.sendMessage(ChatColor.WHITE + "/kit create <kit>                       " + ChatColor.GRAY + "Create a kit.");
            commandSender.sendMessage(ChatColor.WHITE + "/kit edit <kit>                           " + ChatColor.GRAY + "Edit a kit.");
            commandSender.sendMessage(ChatColor.WHITE + "/kit delete <kit>                           " + ChatColor.GRAY + "Delete a kit.");
            commandSender.sendMessage(ChatColor.WHITE + "/kit edititem <name|addlore|dellore|amount|durability>  [value]     " + ChatColor.GRAY + "Sets an items variable.");
        }

        commandSender.sendMessage(ChatColor.GREEN + "");
        commandSender.sendMessage(ChatColor.BLUE + "======================================");
        return true;
    }

    private static boolean handleFlagHelpMessage(CommandSender commandSender)
    {
        commandSender.sendMessage(ChatColor.GREEN + "Base command:             " + ChatColor.WHITE + "/kit flag <kit> <flag> [value]");
        commandSender.sendMessage(ChatColor.GREEN + "Avalaible flags:          " + ChatColor.WHITE + "visible, permonly, displayname, addworld, delworld, icon, permission, cost, delay, addcommand, delcommand");
        return true;
    }

    private static String getArgs(String[] args, int start)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++)
        {
            sb.append(args[i]).append(" ");
        }

        return sb.toString().trim();
    }

    private static boolean handleWorldException(CommandSender commandSender)
    {
        sendMessage(commandSender, phrase("kitadmin_flag_world"), ChatColor.RED);
        return true;
    }

    public static boolean isNumeric(String s)
    {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean isDouble(String s)
    {
        try
        {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    private static void sendMessage(CommandSender commandSender, String message, ChatColor color)
    {
        commandSender.sendMessage(configuration.getChatPrefix() + " " + message);

        if (commandSender instanceof Player)
        {
            TitleAPI.sendTitle((Player) commandSender, 2, 20, 2, "", color + message);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String s, String[] strings)
    {
        String command = cmd.getName();
        if (strings.length == 0)
        {
            if (!(commandSender instanceof Player)) return this.handleOnlyPlayer(commandSender);

            if (command.equalsIgnoreCase("kit"))
            {
                return handleKitCommand(commandSender);
            }
        }
        else if (strings.length == 1)
        {
            if (strings[0].equalsIgnoreCase("version"))
            {
                return this.handleVersionMessage(commandSender);
            }
            else if (strings[0].equalsIgnoreCase("help"))
            {
                return handleHelpMessage(commandSender);
            }
            else if (strings[0].equalsIgnoreCase("reload"))
            {
                return this.handleReload(commandSender);
            }
        }
        else if (strings.length == 2)
        {
            if (!(commandSender instanceof Player)) return this.handleOnlyPlayer(commandSender);
            Player player = (Player) commandSender;

            if (!player.hasPermission(Variables.KIT_PERMISSION))
            {
                sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
                return true;
            }

            if (command.equalsIgnoreCase("kit"))
            {
                if (strings[0].equalsIgnoreCase("use"))
                {
                    Kit kit = KitManager.getKit(strings[1]);
                    if (kit == null)
                    {
                        return handleKitNotFound(commandSender);
                    }

                    return handleUseCommand(player, kit);
                }
                else if (strings[0].equalsIgnoreCase("buy"))
                {
                    Kit kit = KitManager.getKit(strings[1]);
                    if (kit == null)
                    {
                        return handleKitNotFound(commandSender);
                    }

                    return handleBuyCommand(player, kit);
                }
                else if (strings[0].equalsIgnoreCase("view"))
                {
                    Kit kit = KitManager.getKit(strings[1]);
                    if (kit == null)
                    {
                        return handleKitNotFound(commandSender);
                    }

                    return handleViewCommand(player, kit);
                }
                else if (strings[0].equalsIgnoreCase("create"))
                {
                    return handleCreateCommand(player, strings[1]);
                }
                else if (strings[0].equalsIgnoreCase("edit"))
                {
                    Kit kit = KitManager.getKit(strings[1]);
                    if (kit == null)
                    {
                        return handleKitNotFound(commandSender);
                    }

                    return handleEditCommand(player, kit);
                }
                else if (strings[0].equalsIgnoreCase("delete"))
                {
                    Kit kit = KitManager.getKit(strings[1]);
                    if (kit == null)
                    {
                        return handleKitNotFound(commandSender);
                    }

                    return handleDeleteCommand(player, kit);
                }
            }
        }
        else if (strings.length > 2)
        {
            if (!(commandSender instanceof Player)) return this.handleOnlyPlayer(commandSender);
            Player player = (Player) commandSender;

            if (strings[0].equalsIgnoreCase("setflag") && player.hasPermission(Variables.KITADMIN_PERMISSION))
            {
                Kit kit = KitManager.getKit(strings[1]);
                if (kit == null)
                {
                    return handleKitNotFound(commandSender);
                }

                return handleFlagCommand(player, kit, Arrays.copyOfRange(strings, 2, strings.length));
            }
            else if (strings[0].equalsIgnoreCase("edititem") && player.hasPermission(Variables.KITADMIN_PERMISSION))
            {
                return handleEditItem(player, Arrays.copyOfRange(strings, 1, strings.length));
            }
        }
        return handleHelpMessage(commandSender);
    }

    private boolean handleEditItem(Player player, String[] strings)
    {
        ItemStack itemStack = player.getItemInHand();
        if (itemStack == null || itemStack.getType() == Material.AIR)
        {
            sendMessage(player, phrase("not_air"), ChatColor.RED);
            return true;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (strings[0].equalsIgnoreCase("name"))
        {
            String name = strings[1];
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        else if (strings[0].equalsIgnoreCase("addlore"))
        {
            String value = getArgs(strings, 1);
            List<String> lore;

            lore = itemMeta.getLore();
            lore.add(ChatColor.translateAlternateColorCodes('&', value));
            itemMeta.setLore(lore);
        }
        else if (strings[0].equalsIgnoreCase("dellore"))
        {
            String value = getArgs(strings, 1);

            if (!isNumeric(value))
            {
                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_integer"));
                return true;
            }
            List<String> lore = itemMeta.getLore();
            if (lore.size() <= Integer.valueOf(value))
            {
                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + "Cannot find the selected lore.");
                return true;
            }

            lore.remove((int) Integer.valueOf(value));
            itemMeta.setLore(lore);
        }
        else if (strings[0].equalsIgnoreCase("durability"))
        {
            String value = getArgs(strings, 1);

            if (!isNumeric(value))
            {
                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_integer"));
                return true;
            }
            itemStack.setDurability(Short.valueOf(value));
        }
        else if (strings[0].equalsIgnoreCase("amount"))
        {
            String value = getArgs(strings, 1);

            if (!isNumeric(value))
            {
                player.sendMessage(configuration.getChatPrefix() + " " + ChatColor.RED + phrase("kitadmin_flag_integer"));
                return true;
            }
            itemStack.setAmount(Integer.valueOf(value));
        }
        itemStack.setItemMeta(itemMeta);
        player.updateInventory();
        return true;
    }

    private boolean handleReload(CommandSender commandSender)
    {
        if (!commandSender.hasPermission(Variables.RELOAD_PERMISSION))
        {
            sendMessage(commandSender, phrase("error_no_permission"), ChatColor.RED);
            return true;
        }

        AdvancedKits.getInstance().setConfiguration(new Configuration(AdvancedKits.getInstance()));

        AdvancedKits.getInstance().getConfiguration().loadConfiguration();

        KitManager.load();
        commandSender.sendMessage(configuration.getChatPrefix() + " " + ChatColor.GREEN + phrase("kit_reload"));

        return true;
    }

    private boolean handleVersionMessage(CommandSender commandSender)
    {
        commandSender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Plugin:         " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getName());
        commandSender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Version:       " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getVersion());
        commandSender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Author(s):    " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getAuthors());
        commandSender.sendMessage(ChatColor.WHITE + "" + ChatColor.GRAY + "Website:       " + ChatColor.BLUE + "" + ChatColor.BOLD + AdvancedKits.getInstance().getDescription().getWebsite());
        return true;
    }

    private boolean handleOnlyPlayer(CommandSender commandSender)
    {
        commandSender.sendMessage(configuration.getChatPrefix() + " " + phrase("error_only_player"));
        return true;
    }
}
