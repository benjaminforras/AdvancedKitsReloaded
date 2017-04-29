package hu.tryharddevs.advancedkits.utils.menuapi.system;

import hu.tryharddevs.advancedkits.utils.menuapi.components.Coordinates;

/**
 * Created by ColonelHedgehog on 12/11/14.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public class MenuUtils {
	public static int[] calculateCoordinates(int slot) {
		//system.out.println("Calculating by slot: " + slot);
		int slotx = (slot % 9) + 1;
		int sloty = (slot / 9) + 1;

		//system.out.println("RETURN: " + (slotx++) + ", " + (sloty++));
		return new int[]{slotx, sloty};
	}

	public static int toSlotNumber(Coordinates coordinates) {
		return ((coordinates.getY() - 1) * 9) + (coordinates.getX() - 1);
	}
}
