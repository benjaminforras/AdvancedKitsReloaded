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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryListener implements Listener {

	private final Map<Inventory, Map<ClickType, List<InventoryMenuListener>>> listenerMap     = new HashMap<>();
	private final Map<Inventory, List<InventoryEventHandler>>                 eventHandlerMap = new HashMap<>();
	JavaPlugin plugin;

	public InventoryListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void registerListener(InventoryMenuBuilder builder, InventoryMenuListener listener, ClickType[] actions) {
		Map<ClickType, List<InventoryMenuListener>> map = listenerMap.get(builder.getInventory());
		if (map == null) { map = new HashMap<>(); }
		for (ClickType action : actions)
		{
			List<InventoryMenuListener> list = map.get(action);
			if (list == null) { list = new ArrayList<>(); }
			if (list.contains(listener)) { throw new IllegalArgumentException("listener already registered"); }
			list.add(listener);

			map.put(action, list);
		}

		listenerMap.put(builder.getInventory(), map);
	}

	public void unregisterListener(InventoryMenuBuilder builder, InventoryMenuListener listener, ClickType[] actions) {
		Map<ClickType, List<InventoryMenuListener>> map = listenerMap.get(builder.getInventory());
		if (map == null) { return; }
		for (ClickType action : actions)
		{
			List<InventoryMenuListener> list = map.get(action);
			if (list == null) {continue; }
			list.remove(listener);
		}
	}

	public void unregisterAllListeners(Inventory inventory) {
		listenerMap.remove(inventory);
	}

	public void registerEventHandler(InventoryMenuBuilder builder, InventoryEventHandler eventHandler) {
		List<InventoryEventHandler> list = eventHandlerMap.get(builder.getInventory());
		if (list == null) { list = new ArrayList<>(); }
		if (!list.contains(eventHandler)) { list.add(eventHandler); }

		eventHandlerMap.put(builder.getInventory(), list);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Player    player    = (Player) event.getWhoClicked();
		Inventory inventory = event.getClickedInventory();
		ClickType type      = event.getClick();

		if (listenerMap.containsKey(inventory))
		{
			event.setCancelled(true);
			event.setResult(Event.Result.DENY);

			Map<ClickType, List<InventoryMenuListener>> actionMap = listenerMap.get(inventory);
			if (actionMap.containsKey(type))
			{
				List<InventoryMenuListener> listeners = actionMap.get(type);

				for (InventoryMenuListener listener : listeners)
				{
					try
					{
						listener.interact(player, type, event.getSlot());
					} catch (Throwable throwable)
					{
						throwable.printStackTrace();
					}
				}
			}
		}
		if (eventHandlerMap.containsKey(inventory))
		{
			List<InventoryEventHandler> list = eventHandlerMap.get(inventory);

			for (InventoryEventHandler handler : list)
			{
				try
				{
					handler.handle(event);
				} catch (Throwable throwable)
				{
					throwable.printStackTrace();
				}
			}
		}
	}

}
