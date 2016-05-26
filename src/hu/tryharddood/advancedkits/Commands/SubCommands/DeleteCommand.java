package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.advancedkits.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class DeleteCommand extends Subcommand
{
    @Override
    public String getPermission()
    {
        return Variables.KITADMIN_PERMISSION;
    }

    @Override
    public String getUsage()
    {
        return "/kit delete <kit>";
    }

    @Override
    public String getDescription()
    {
        return "Deletes a kit.";
    }

    @Override
    public int getArgs()
    {
        return 2;
    }

    @Override
    public boolean playerOnly()
    {
        return false;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Kit kit = KitManager.getKit(args[1]);
        if (kit == null)
        {
            sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
            return;
        }

        KitManager.deleteKit(kit);
        sender.sendMessage(AdvancedKits.getInstance().getConfiguration().getChatPrefix() + " " + tl("kit_delete"));

        if (sender instanceof Player) closeGUI((Player) sender, "Details");
    }
}
