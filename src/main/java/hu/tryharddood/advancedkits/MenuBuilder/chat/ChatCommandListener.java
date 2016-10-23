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

package hu.tryharddood.advancedkits.MenuBuilder.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChatCommandListener implements CommandExecutor {

	private JavaPlugin plugin;

	private Map<String, List<ChatListener>> listenerMap = new HashMap<>();

	public ChatCommandListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if ("mbchat".equals(command.getName()))
		{
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				if (args.length > 0)
				{
					String key = args[0];
					if (listenerMap.containsKey(key))
					{
						List<ChatListener> listeners = listenerMap.get(key);

						for (ChatListener listener : listeners)
						{
							listener.onClick(player);
						}
					}
					else
					{
						plugin.getLogger().warning(sender.getName() + " tried to run click-command for an unknown listener");
					}
				}
			}
		}
		return false;
	}

	public void registerListener(ChatListener listener, String key) {
		List<ChatListener> list = this.listenerMap.get(key);
		if (list == null) { list = new ArrayList<>(); }
		list.add(listener);
		listenerMap.put(key, list);
	}

	public void registerListener(ChatListener listener, UUID key) {
		registerListener(listener, key.toString().replace("-", ""));
	}

	public String registerListener(ChatListener listener) {
		for (Map.Entry<String, List<ChatListener>> entry : listenerMap.entrySet())
		{
			for (ChatListener listener1 : entry.getValue())
			{
				if (listener1.equals(listener))
				{
					return entry.getKey();
				}
			}
		}

		UUID   uuid = UUID.randomUUID();
		String key  = uuid.toString().replace("-", "");
		registerListener(listener, key);
		return key;
	}

	public void unregisterListener(String key) {
		this.listenerMap.remove(key);
	}
}
