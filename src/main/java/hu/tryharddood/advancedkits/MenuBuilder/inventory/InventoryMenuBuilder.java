/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package hu.tryharddood.advancedkits.MenuBuilder.inventory;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.MenuBuilder.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.inventory.ClickType.*;

public class InventoryMenuBuilder extends MenuBuilder<Inventory> {

	public static final ClickType[] ALL_CLICK_TYPES = new ClickType[]{
			LEFT,
			SHIFT_LEFT,
			RIGHT,
			SHIFT_RIGHT,
			WINDOW_BORDER_LEFT,
			WINDOW_BORDER_RIGHT,
			MIDDLE,
			NUMBER_KEY,
			DOUBLE_CLICK,
			DROP,
			CONTROL_DROP};

	private Inventory inventory;

	private List<ItemCallback> callbackItems = new ArrayList<>();

	/**
	 * Construct a new InventoryMenuBuilder without content
	 */
	public InventoryMenuBuilder() {
	}

	/**
	 * Construct a new InventoryMenuBuilder with the specified size
	 *
	 * @param size Size of the inventory
	 */
	public InventoryMenuBuilder(int size) {
		this();
		withSize(size);
	}

	/**
	 * Construct a new InventoryMenuBuilder with the specified size and title
	 *
	 * @param size  Size of the inventory
	 * @param title Title of the inventory
	 */
	public InventoryMenuBuilder(int size, String title) {
		this(size);
		withTitle(title);
	}

	/**
	 * Construct a new InventoryMenuBuilder with the specified {@link InventoryType}
	 *
	 * @param type {@link InventoryType}
	 */
	public InventoryMenuBuilder(InventoryType type) {
		this();
		withType(type);
	}

	/**
	 * Construct a new InventoryMenuBuilder with the specified {@link InventoryType} and title
	 *
	 * @param type  {@link InventoryType}
	 * @param title Title of the inventory
	 */
	public InventoryMenuBuilder(InventoryType type, String title) {
		this(type);
		withTitle(title);
	}

	protected void initInventory(Inventory inventory) {
		if (this.inventory != null) { throw new IllegalStateException("Inventory already initialized"); }
		this.inventory = inventory;
	}

	protected void validateInit() {
		if (this.inventory == null) { throw new IllegalStateException("inventory not yet initialized"); }
	}

	/**
	 * @return The {@link Inventory} being built
	 */
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Sets the initial size
	 *
	 * @param size Size of the inventory
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withSize(int size) {
		initInventory(Bukkit.createInventory(null, size));
		return this;
	}

	/**
	 * Sets the initial type
	 *
	 * @param type {@link InventoryType}
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withType(InventoryType type) {
		initInventory(Bukkit.createInventory(null, type));
		return this;
	}

	/**
	 * Change the title of the inventory and update it to all viewers
	 *
	 * @param title new title of the inventory
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withTitle(@Nonnull String title) {
		return withTitle(title, true);
	}

	/**
	 * Change the title of the inventory and <i>optionally</i> update it to all viewers
	 *
	 * @param title   new title of the inventory
	 * @param refresh if <code>true</code>, the inventory will be re-opened to all viewers
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withTitle(@Nonnull String title, boolean refresh) {
		validateInit();
		InventoryHelper.changeTitle(this.inventory, title);

		if (refresh)
		{
			for (HumanEntity viewer : this.inventory.getViewers())
			{
				viewer.closeInventory();
				viewer.openInventory(this.inventory);
			}
		}
		return this;
	}

	/**
	 * Add an <i>optional</i> {@link InventoryEventHandler} to further customize the click-behaviour
	 *
	 * @param eventHandler {@link InventoryEventHandler} to add
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withEventHandler(@Nonnull InventoryEventHandler eventHandler) {
		try
		{
			AdvancedKits.instance.inventoryListener.registerEventHandler(this, eventHandler);
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Add a {@link InventoryMenuListener} for the specified {@link ClickType}s
	 *
	 * @param listener the {@link InventoryMenuListener} to add
	 * @param actions  the {@link ClickType}s the listener should listen for (you can also use {@link #ALL_CLICK_TYPES} or {@link ClickType#values()}
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder onInteract(@Nonnull InventoryMenuListener listener, @Nonnull ClickType... actions) {
		if (actions == null || (actions != null && actions.length == 0))
		{
			throw new IllegalArgumentException("must specify at least one action");
		}
		try
		{
			AdvancedKits.instance.inventoryListener.registerListener(this, listener, actions);
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Set the item for the specified slot
	 *
	 * @param slot Slot of the item
	 * @param item {@link ItemStack} to set
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withItem(@Nonnegative int slot, @Nonnull ItemStack item) {
		validateInit();
		this.inventory.setItem(slot, item);
		return this;
	}

	/**
	 * Set the item for the specified slot and add a {@link ItemListener} for it
	 *
	 * @param slot     Slot of the item
	 * @param item     {@link ItemStack} to set
	 * @param listener {@link ItemListener} for the item
	 * @param actions  the {@link ClickType}s the listener should listen for (you can also use {@link #ALL_CLICK_TYPES} or {@link ClickType#values()}
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withItem(@Nonnegative final int slot, @Nonnull final ItemStack item, @Nonnull final ItemListener listener, @Nonnull ClickType... actions) {
		withItem(slot, item);
		onInteract(new InventoryMenuListener() {
			@Override
			public void interact(Player player, ClickType action, InventoryClickEvent event) {
				if (event.getSlot() == slot) { listener.onInteract(player, action, item); }
			}
		}, actions);
		return this;
	}

	/**
	 * Add an item using a {@link ItemCallback}
	 * The callback will be called when {@link #show(HumanEntity...)} or {@link #refreshContent()} is called
	 *
	 * @param callback {@link ItemCallback}
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder withItem(@Nonnull ItemCallback callback) {
		callbackItems.add(callback);
		return this;
	}

	/**
	 * Builds the {@link Inventory}
	 *
	 * @return a {@link Inventory}
	 */
	public Inventory build() {
		return this.inventory;
	}

	/**
	 * Shows the inventory to the viewers
	 *
	 * @param viewers Array of {@link HumanEntity}
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder show(HumanEntity... viewers) {
		refreshContent();
		for (HumanEntity viewer : viewers)
		{
			viewer.openInventory(this.build());
		}
		return this;
	}

	/**
	 * Refresh the content of the inventory
	 * Will call all {@link ItemCallback}s registered with {@link #withItem(ItemCallback)}
	 *
	 * @return the InventoryMenuBuilder
	 */
	public InventoryMenuBuilder refreshContent() {
		for (ItemCallback callback : callbackItems)
		{
			int       slot = callback.getSlot();
			ItemStack item = callback.getItem();

			withItem(slot, item);
		}
		return this;
	}

	@Override
	public void dispose() {
		AdvancedKits.instance.inventoryListener.unregisterAllListeners(getInventory());
	}

	public void unregisterListener(InventoryMenuListener listener) {
		try
		{
			AdvancedKits.instance.inventoryListener.registerListener(this, listener, ALL_CLICK_TYPES);
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}

	public void withItems(List<ItemStack> items) {
		withItems(items.toArray(new ItemStack[items.size()]));
	}

	public void withItems(ItemStack[] items) {

		for (int i = 0; i < items.length; i++)
		{
			if (items[i] != null)
				withItem(i, items[i]);
		}
	}
}
