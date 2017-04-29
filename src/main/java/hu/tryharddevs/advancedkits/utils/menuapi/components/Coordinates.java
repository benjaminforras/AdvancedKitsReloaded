package hu.tryharddevs.advancedkits.utils.menuapi.components;

import hu.tryharddevs.advancedkits.utils.menuapi.system.MenuUtils;

/**
 * Created by ColonelHedgehog on 1/22/15.
 * You have freedom to modify given sources. Please credit me as original author.
 * Keep in mind that this is not for sale.
 */
public class Coordinates {
	private Menu menu;
	private int  x;
	private int  y;

	public Coordinates(Menu menu, int x, int y) {
		this.menu = menu;
		this.x = x;
		this.y = y;
	}

	public Coordinates(Menu menu, int slot) {
		this.menu = menu;
		this.x = MenuUtils.calculateCoordinates(slot)[0];
		this.y = MenuUtils.calculateCoordinates(slot)[1];
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Menu getMenu() {
		return menu;
	}

	public int asSlotNumber() {
		return MenuUtils.toSlotNumber(this);
	}
}
