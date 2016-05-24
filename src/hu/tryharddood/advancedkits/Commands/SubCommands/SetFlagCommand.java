package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

import static hu.tryharddood.advancedkits.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class SetFlagCommand extends Subcommand {
    @Override
    public String getPermission() {
        return Variables.KITADMIN_PERMISSION;
    }

    @Override
    public String getUsage() {
        return "/kit setflag <kit> <flag> [value]";
    }

    @Override
    public String getDescription() {
        return "Sets a flag for a kit.";
    }

    @Override
    public int getArgs() {
        return -1;
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length <= 2) {
            sender.sendMessage(tl("chat_usage") + ":");
            sender.sendMessage(ChatColor.GREEN + getUsage() + ChatColor.GRAY + " - " + ChatColor.BLUE + getDescription());
            return;
        }

        Kit kit = KitManager.getKit(args[1]);
        if (kit == null) {
            sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
            return;
        }

        String[] strings = Arrays.copyOfRange(args, 2, args.length);

        if (strings.length == 1) {
            String flag = strings[0];
            if (Arrays.asList("visible", "permonly", "permissiononly", "icon", "clearinv", "firstjoin").contains(flag)) {
                if (flag.equalsIgnoreCase("visible")) {
                    boolean visible = kit.isVisible();
                    kit.setSave(true);
                    kit.setVisible(!visible);
                    kit.setSave(false);

                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                    player.sendMessage(ChatColor.WHITE + "    visibility: " + (!visible ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
                } else if (flag.equalsIgnoreCase("firstjoin")) {
                    boolean firstjoin = kit.isFirstjoin();
                    kit.setSave(true);
                    kit.setFirstjoin(!firstjoin);
                    kit.setSave(false);

                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                    player.sendMessage(ChatColor.WHITE + "    firstjoin: " + (!firstjoin ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
                } else if (flag.equalsIgnoreCase("permonly") || flag.equalsIgnoreCase("permissiononly")) {
                    boolean permonly = kit.isPermonly();
                    kit.setSave(true);
                    kit.setPermonly(!permonly);
                    kit.setSave(false);

                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                    player.sendMessage(ChatColor.WHITE + "    permissionOnly: " + (!permonly ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
                } else if (flag.equalsIgnoreCase("icon")) {
                    ItemStack itemStack;
                    if (AdvancedKits.ServerVersion == 19) {
                        itemStack = player.getInventory().getItemInMainHand();
                    } else {
                        itemStack = player.getItemInHand();
                    }

                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_wrong_icon") + ": ");
                        player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + "AIR");
                        return;
                    }

                    kit.setSave(true);
                    kit.setIcon(itemStack.getType());
                    kit.setSave(false);

                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_success_icon") + ":");
                    player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + itemStack.getType().toString());
                } else if (flag.equalsIgnoreCase("clearinv")) {
                    boolean clearinv = kit.isClearinv();
                    kit.setSave(true);
                    kit.setClearinv(!clearinv);
                    kit.setSave(false);

                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                    player.sendMessage(ChatColor.WHITE + "    ClearInv: " + (!clearinv ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
                }
            }
        } else if (strings.length >= 2) {
            String flag = strings[0];
            String value = getArgString(strings, 1);

            if (flag.equalsIgnoreCase("visible")) {
                if (!value.equalsIgnoreCase("true") || !value.equalsIgnoreCase("false")) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! " + tl("kitadmin_flag_boolean"));
                    return;
                }

                kit.setSave(true);
                kit.setVisible(Boolean.valueOf(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                player.sendMessage(ChatColor.WHITE + "    visibility: " + (!kit.isVisible() ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
                return;
            }
            if (flag.equalsIgnoreCase("clearinv")) {
                if (!value.equalsIgnoreCase("true") || !value.equalsIgnoreCase("false")) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! " + tl("kitadmin_flag_boolean"));
                    return;
                }

                kit.setSave(true);
                kit.setClearinv(Boolean.valueOf(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                player.sendMessage(ChatColor.WHITE + "    ClearInv: " + (!kit.isClearinv() ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
            } else if (flag.equalsIgnoreCase("firstjoin")) {
                kit.setSave(true);
                kit.setFirstjoin(!Boolean.valueOf(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                player.sendMessage(ChatColor.WHITE + "    firstjoin: " + (!kit.isFirstjoin() ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));//Itt ugyan azt az ĂŠrtĂŠket hasznĂĄlja, mint vĂĄltoztatĂĄs elĹtt DE a fĂĄjlba pedig elmenti helyesen.
            } else if (flag.equalsIgnoreCase("permonly") || flag.equalsIgnoreCase("permissiononly")) {
                if (!value.equalsIgnoreCase("true") || !value.equalsIgnoreCase("false")) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! " + tl("kitadmin_flag_boolean"));
                    return;
                }

                kit.setSave(true);
                kit.setPermonly(Boolean.valueOf(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GOLD + tl("kitadmin_flag_set"));
                player.sendMessage(ChatColor.WHITE + "    permissionOnly: " + (!kit.isPermonly() ? ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_on")) : ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(tl("chat_off"))));
            } else if (flag.equalsIgnoreCase("permission")) {
                if (!kit.isPermonly()) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_permission_wrong"));
                    return;
                }

                kit.setSave(true);
                kit.setPermission(value);
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_permission_set") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
            } else if (flag.equalsIgnoreCase("addworld")) {
                if (kit.getWorlds().contains(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_addworld_wrong") + " " + value);
                    return;
                }

                kit.setSave(true);
                kit.AddWorld(value);
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_addworld_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getWorlds().toString());
            } else if (flag.equalsIgnoreCase("removeworld") || flag.equalsIgnoreCase("delworld")) {
                if (!kit.getWorlds().contains(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_delworld_wrong") + " " + value);
                    return;
                }

                kit.setSave(true);
                kit.RemoveWorld(value);
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_delworld_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getWorlds().toString());
            } else if (flag.equalsIgnoreCase("addcommand")) {
                if (kit.getCommands().contains(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_addcommand_wrong") + " " + value);
                    return;
                }

                kit.setSave(true);
                kit.AddCommand(value);
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_addcommand_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCommands().toString());
            } else if (flag.equalsIgnoreCase("removecommand") || flag.equalsIgnoreCase("delcommand")) {
                if (!kit.getCommands().contains(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_delcommand_wrong") + " " + value);
                    return;
                }

                kit.setSave(true);
                kit.RemoveCommand(value);
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_delcommand_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + kit.getCommands().toString());
            } else if (flag.equalsIgnoreCase("cost") || flag.equalsIgnoreCase("money")) {
                if (!isNumeric(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
                    return;
                }

                kit.setSave(true);
                kit.setCost(Integer.valueOf(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_cost_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
            } else if (flag.equalsIgnoreCase("uses") || flag.equalsIgnoreCase("setuses")) {
                if (!isNumeric(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
                    return;
                }

                kit.setSave(true);
                kit.setUses(Integer.valueOf(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_uses_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
            } else if (flag.equalsIgnoreCase("wait") || flag.equalsIgnoreCase("delay")) {
                if (!isDouble(value)) {
                    player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
                    return;
                }

                kit.setSave(true);
                kit.setDelay(Double.parseDouble(value));
                kit.setSave(false);

                player.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + ChatColor.GREEN + tl("kitadmin_flag_delay_success") + ": ");
                player.sendMessage("               " + ChatColor.WHITE + "" + ChatColor.BOLD + value);
            } else {
                sender.sendMessage(ChatColor.GREEN + "Avalaible flags:          " + ChatColor.WHITE + "visible, permonly, addworld, delworld, icon, permission, cost, delay, addcommand, delcommand, uses, clearinv");
            }
        }
    }
}