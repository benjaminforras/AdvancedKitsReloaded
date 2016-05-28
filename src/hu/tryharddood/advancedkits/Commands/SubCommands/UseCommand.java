package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Listeners.InventoryListener;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class UseCommand extends Subcommand
{
    public static void GiveItems(Player player, Kit kit)
    {
        if (kit.getUses() > 0)
        {
            AdvancedKits.getKitManager().setUses(kit, player, (AdvancedKits.getKitManager().getUses(kit, player) + 1));
        }

        PlayerInventory inv = player.getInventory();
        if (kit.isClearinv())
        {
            if (AdvancedKits.ServerVersion == 19)
            {
                player.getInventory().setArmorContents(null);
                player.getInventory().setExtraContents(null);
                player.getInventory().setItemInMainHand(null);
                player.getInventory().setItemInOffHand(null);
                player.getInventory().clear();
            }
            else
            {
                player.getInventory().setArmorContents(null);
                player.getInventory().clear();
            }
        }

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

        for (ItemStack item : kit.getArmor())
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

            if (InventoryListener.isHelmet(item.getType()))
            {
                player.getInventory().setHelmet(item);
            }
            else if (InventoryListener.isChestplate(item.getType()))
            {
                player.getInventory().setChestplate(item);
            }
            else if (InventoryListener.isLeggings(item.getType()))
            {
                player.getInventory().setLeggings(item);
            }
            else if (InventoryListener.isBoots(item.getType())) player.getInventory().setBoots(item);
        }

        player.updateInventory();

        AdvancedKits.getKitManager().setDelay(player, kit.getDelay(), kit);
        closeGUI(player, "Details");
        sendMessage(player, tl("kituse_success"), ChatColor.GREEN);

        for (String command : kit.getCommands())
        {
            Bukkit.dispatchCommand(AdvancedKits.getInstance().getServer().getConsoleSender(), command.replaceAll("%player%", player.getName()));
        }
    }

    @Override
    public String getPermission()
    {
        return Variables.KIT_USE_PERMISSION;
    }

    @Override
    public String getUsage()
    {
        return "/kit use <kit>";
    }

    @Override
    public String getDescription()
    {
        return "Uses a kit";
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
        Kit    kit    = AdvancedKits.getKitManager().getKit(args[1]);
        if (kit == null)
        {
            sendMessage(player, tl("error_kit_not_found"), ChatColor.RED);
            return;
        }
        if (kit.getUses() > 0 && (kit.getUses() - AdvancedKits.getKitManager().getUses(kit, player)) <= 0 && !player.hasPermission(Variables.KITADMIN_PERMISSION))
        {
            sendMessage(player, tl("cant_use_anymore"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (AdvancedKits.getInstance().getConfiguration().isEconomy() && !AdvancedKits.getKitManager().getUnlocked(kit, player.getName()))
        {
            sendMessage(player, tl("kituse_error_notunlocked"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (kit.isPermonly() && !player.hasPermission(kit.getPermission()))
        {
            sendMessage(player, tl("error_no_permission"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (kit.getDelay() > 0)
        {
            if (!player.hasPermission(Variables.KITDELAY_BYPASS))
            {
                if (!AdvancedKits.getKitManager().CheckCooldown(player, kit))
                {
                    closeGUI(player, "Details");
                    sendMessage(player, tl("kituse_wait", AdvancedKits.getKitManager().getDelay(player, kit)), ChatColor.RED);
                    return;
                }
            }
        }

        if (kit.getWorlds().contains(player.getWorld().getName()))
        {
            sendMessage(player, tl("kitadmin_flag_world"), ChatColor.RED);
            return;
        }

        GiveItems(player, kit);
    }
}
