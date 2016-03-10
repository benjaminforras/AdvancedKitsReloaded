package hu.tryharddood.advancedkits.Listeners;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.CommandManager;
import hu.tryharddood.advancedkits.KitManager.Kit;
import hu.tryharddood.advancedkits.KitManager.KitManager;
import hu.tryharddood.advancedkits.Utils.OpInfoThread;
import hu.tryharddood.advancedkits.Utils.TitleAPI.TitleAPI;
import hu.tryharddood.advancedkits.Utils.UpdateManager;
import hu.tryharddood.advancedkits.Variables;
import me.libraryaddict.inventory.ItemBuilder;
import me.libraryaddict.inventory.PageInventory;
import me.libraryaddict.inventory.events.PagesClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hu.tryharddood.advancedkits.Phrases.phrase;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class InventoryListener implements Listener
{

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        Player player = ev.getPlayer();

        if (player.isOp() && UpdateManager.isUpdated())
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(AdvancedKits.getInstance(), new OpInfoThread(player), 20L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPageClickEvent(PagesClickEvent event)
    {
        if (event.getItemStack() == null || event.getItemStack().getType() == Material.AIR || !event.getItemStack().hasItemMeta() || !event.getItemStack().getItemMeta().hasDisplayName())
        {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemStack();
        String itemName = event.getItemStack().getItemMeta().getDisplayName();
        String invName = event.getInventory().getTitle();

        if (invName.contains("Kits") && itemStack.getType() != Material.PAPER)
        {
            Kit kit = KitManager.getKit(ChatColor.stripColor(itemName));
            if (kit == null)
            {
                return;
            }
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
        }

        if (invName.contains("Details"))
        {
            Kit kit = KitManager.getKit(invName.substring(10));
            if (kit == null)
            {
                return;
            }

            if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "USE"))
            {
                CommandManager.handleUseCommand(player, kit);
            }

            if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "BUY"))
            {
                CommandManager.handleBuyCommand(player, kit);
            }

            if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "Back to the list"))
            {
                CommandManager.handleKitCommand(player);
            }

            if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "Edit"))
            {
                CommandManager.handleEditCommand(player, kit);
            }

            if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "Delete"))
            {
                CommandManager.handleDeleteCommand(player, kit);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClickEvent(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player))
        {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        Inventory inventory = event.getInventory();
        String invName = event.getInventory().getTitle();

        if (invName.contains("Create"))
        {
            Kit kit = KitManager.getKit(invName.substring(9));
            if (kit != null)
            {
                CommandManager.handleKitExists(player);
                return;
            }

            List<ItemStack> itemStacks = new ArrayList<>();
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            {
                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (itemStack != null && itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "Create kit"))
                {

                    for (int i = 0; i < 36; i++)
                    {
                        if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)
                        {
                            continue;
                        }
                        else
                        {
                            itemStacks.add(inventory.getItem(i));
                        }
                    }
                    new Kit(invName.substring(9)).createKit(itemStacks);
                    event.setCancelled(true);
                    player.closeInventory();

                    TitleAPI.sendTitle(player, 2, 20, 2, "", ChatColor.RED + phrase("kit_create"));
                }

                if (itemName.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "Cancel"))
                {
                    event.setCancelled(true);
                    player.closeInventory();
                }
            }

            if (Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53).contains(event.getRawSlot()))
            {
                event.setCancelled(true);
            }
        }

        if (invName.contains("Edit"))
        {
            if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            {
                Kit kit = new Kit(invName.substring(7));

                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (itemName.equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "Edit kit"))
                {
                    kit.setSave(true);
                    kit.getItemStacks().clear();
                    for (int i = 0; i < 36; i++)
                    {
                        if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)
                        {
                            continue;
                        }
                        kit.AddItem(inventory.getItem(i));
                    }
                    kit.setSave(false);
                    KitManager.load();

                    event.setCancelled(true);
                    player.closeInventory();

                    TitleAPI.sendTitle(player, 2, 20, 2, "", ChatColor.GREEN + phrase("kit_edit"));
                }

                if (itemName.equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "Cancel"))
                {
                    event.setCancelled(true);
                    player.closeInventory();
                }
            }

            if (Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53).contains(event.getRawSlot()))
            {
                event.setCancelled(true);
            }
        }
    }
}
