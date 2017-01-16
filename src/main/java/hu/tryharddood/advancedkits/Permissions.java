package hu.tryharddood.advancedkits;

import hu.tryharddood.advancedkits.Kits.Kit;
import org.bukkit.entity.Player;

import static hu.tryharddood.advancedkits.Permissions.Permission.*;

public class Permissions {

	private Permissions() {}

	public enum Permission {
		RELOAD_PERMISSION("advancedkits.reload"),
		KITADMIN_PERMISSION("advancedkits.kitadmin"),

		KIT_VIEW_PERMISSION("advancedkits.kit.view"),
		KIT_VIEW_PERMISSION_ALL("advancedkits.kit.view.*"),
		KIT_VIEW_PERMISSION_KIT("advancedkits.kit.view.[kitname]"),

		KIT_BUY_PERMISSION("advancedkits.kit.buy"),
		KIT_USE_PERMISSION("advancedkits.kit.use"),
		KIT_USE_KIT_PERMISSION("advancedkits.kit.use.[kitname]"),
		KIT_USE_KIT_PERMISSION_ALL("advancedkits.kit.use.*"),

		KIT_GIVE_PERMISSION("advancedkits.kit.give"),
		KIT_BYPASS_DELAY("advancedkits.kit.delay.bypass");

		private String _permission;

		Permission(String permission) {
			_permission = permission;
		}

		public String toString() {
			return _permission;
		}
	}

	/**
	 * Checks if a player is an admin
	 *
	 * @param player The Player
	 * @return true/false
	 */
	public static boolean isAdmin(Player player) {
		return (player.hasPermission(KITADMIN_PERMISSION.toString()));
	}


	/**
	 * Checks if the player has the permission for buying kits
	 *
	 * @param player player
	 * @return true/false
	 */
	public static boolean canBuy(Player player) {
		return (player.hasPermission(KIT_BUY_PERMISSION.toString()));
	}

	/**
	 * Checks if a player has permission to view kits
	 *
	 * @param player player
	 * @return true/false
	 */
	public static boolean canView(Player player) {
		return (player.hasPermission(KIT_VIEW_PERMISSION.toString()));
	}

	/**
	 * Checks if the player is allowed to reload the plugin
	 *
	 * @param player player
	 * @return true/false
	 */
	public static boolean canReload(Player player) {
		return (player.hasPermission(RELOAD_PERMISSION.toString()));
	}

	/**
	 * Checks if the player is allowed to give kits
	 *
	 * @param player player
	 * @return true/false
	 */
	public static boolean canGive(Player player) {
		return (player.hasPermission(KIT_GIVE_PERMISSION.toString()));
	}

	/**
	 * Checks if a player has bypass permission
	 *
	 * @param player The Player
	 * @return true/false
	 */
	public static boolean skipDelay(Player player) {
		return (player.hasPermission(KIT_BYPASS_DELAY.toString()));
	}

	/**
	 * Checks if a player has permissions to the use command and the kit
	 *
	 * @param player The player
	 * @param kit    The kit
	 * @return true/false
	 */
	public static boolean canUseKit(Player player, Kit kit) {
		return (player.hasPermission(KIT_USE_PERMISSION.toString()) && (player.hasPermission(KIT_USE_KIT_PERMISSION_ALL.toString()) || player.hasPermission(kit.getPermission())));
	}

	/**
	 * Checks if a player can view the kit.
	 *
	 * @param player The player
	 * @param kit    The kit
	 * @return true/false
	 */
	public static boolean canViewKit(Player player, Kit kit) {
		return (player.hasPermission(KIT_VIEW_PERMISSION_ALL.toString()) || player.hasPermission(KIT_VIEW_PERMISSION_KIT.toString().replace("[kitname]", kit.getName())));
	}
}
