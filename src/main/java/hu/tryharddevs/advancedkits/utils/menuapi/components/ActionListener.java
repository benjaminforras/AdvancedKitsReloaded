package hu.tryharddevs.advancedkits.utils.menuapi.components;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Created by ColonelHedgehog on 1/23/15.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public interface ActionListener {
	public void onClick(ClickType clickType, MenuObject menuObject, Player whoClicked);
}
