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

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.MenuBuilder.chat.component.MenuComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class LineBuilder {

	protected ChatMenuBuilder container;
	protected List<String> listenerKeys = new ArrayList<>();
	BaseComponent base;

	/**
	 * Construct a new empty LineBuilder
	 */
	public LineBuilder() {
		this("");
	}

	/**
	 * Construct a new LineBuilder with the specified text
	 *
	 * @param text
	 */
	public LineBuilder(String text) {
		base = new TextComponent(text);
	}

	/**
	 * Append text
	 *
	 * @param text {@link String} to append
	 * @return the LineBuilder
	 */
	public LineBuilder append(String... text) {
		for (String s : text)
		{
			base.addExtra(s);
		}
		return this;
	}

	/**
	 * Append {@link BaseComponent}s
	 *
	 * @param components {@link BaseComponent}s to append
	 * @return the LineBuilder
	 */
	public LineBuilder append(BaseComponent... components) {
		for (BaseComponent component : components)
		{
			base.addExtra(component);
		}
		return this;
	}

	/**
	 * Append a {@link MenuComponent} (for example a {@link hu.tryharddood.advancedkits.MenuBuilder.chat.component.MenuComponentCheckbox})
	 *
	 * @param component {@link MenuComponent} to append
	 * @return the LineBuilder
	 */
	public LineBuilder append(MenuComponent component) {
		component.appendTo(this);
		return this;
	}

	/**
	 * Append {@link BaseComponent}s with a {@link ChatListener}
	 *
	 * @param listener   {@link ChatListener}
	 * @param components {@link BaseComponent}s to append
	 * @return
	 */
	public LineBuilder append(ChatListener listener, BaseComponent... components) {
		for (BaseComponent component : components)
		{
			String key = AdvancedKits.instance.chatCommandListener.registerListener(listener);
			component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mbchat " + key));
			listenerKeys.add(key);

			base.addExtra(component);
		}
		return this;
	}

	protected LineBuilder withContainer(ChatMenuBuilder container) {
		this.container = container;
		return this;
	}

	/**
	 * Internal method to access the {@link ChatMenuBuilder}
	 */
	public ChatMenuBuilder getContainer() {
		return this.container;
	}

	/**
	 * Builds the line
	 *
	 * @return a {@link BaseComponent}
	 */
	public BaseComponent build() {
		return this.base;
	}
}
