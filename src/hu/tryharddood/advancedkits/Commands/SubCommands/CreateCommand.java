package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Variables;
import me.libraryaddict.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static hu.tryharddood.advancedkits.Phrases.phrase;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class CreateCommand extends Subcommand
{
    @Override
    public String getPermission()
    {
        return Variables.KITADMIN_PERMISSION;
    }

    @Override
    public String getUsage()
    {
        return "/kit create <kit>";
    }

    @Override
    public String getDescription()
    {
        return "Creates a kit with the given name.";
    }

    @Override
    public int getArgs()
    {
        return 2;
    }

    @Override
    public boolean playerOnly()
    {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player) sender;
        Kit kit = KitManager.getKit(args[1]);
        if (kit != null)
        {
            player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + phrase("error_kit_create_exists"));
            return;
        }

        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "Create - " + args[1]);

        inventory.setItem(inventorySize - 4, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Create kit").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to create the kit").build());
        inventory.setItem(inventorySize - 6, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setTitle(ChatColor.RED + "" + ChatColor.BOLD + "Cancel").addLore(ChatColor.WHITE + "" + ChatColor.BOLD, ChatColor.WHITE + "" + ChatColor.BOLD + "Click here to cancel the kit creation").build());

        for (int i = 27; i < 31; i++)
        {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.GREEN.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
        }

        for (int i = 36; i < 45; i++)
        {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.BLACK.getData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "§8").build());
        }
        player.openInventory(inventory);
    }
}
