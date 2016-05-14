package hu.tryharddood.advancedkits.ClearInventory;

import org.bukkit.entity.Player;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class ClearInventory_1_9 implements ClearInventory
{
    @Override
    public void clearArmor(Player player)
    {
        player.getInventory().setArmorContents(null);
    }

    @Override
    public void clearInventory(Player player)
    {
        player.getInventory().setExtraContents(null);
        player.getInventory().setItemInMainHand(null);
        player.getInventory().setItemInOffHand(null);
        player.getInventory().clear();
    }
}
