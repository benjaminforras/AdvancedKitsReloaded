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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

	public static class CenterUtil {
		public static final String line = ChatColor.STRIKETHROUGH + "----------------------------------------------------";
		public static final String newline = ChatColor.GREEN + "";
		private static final int CENTER_PX = 154;

		public static void sendCenteredMessage(Player player, String message) {
			if (message == null || message.equals("")) player.sendMessage("");
			message = ChatColor.translateAlternateColorCodes('&', message);

			int     messagePxSize = 0;
			boolean previousCode  = false;
			boolean isBold        = false;

			for (char c : message.toCharArray())
			{
				if (c == '§')
				{
					previousCode = true;
					continue;
				}
				else if (previousCode == true)
				{
					previousCode = false;
					if (c == 'l' || c == 'L')
					{
						isBold = true;
						continue;
					}
					else isBold = false;
				}
				else
				{
					DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
					messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
					messagePxSize++;
				}
			}

			int           halvedMessageSize = messagePxSize / 2;
			int           toCompensate      = CENTER_PX - halvedMessageSize;
			int           spaceLength       = DefaultFontInfo.SPACE.getLength() + 1;
			int           compensated       = 0;
			StringBuilder sb                = new StringBuilder();
			while (compensated < toCompensate)
			{
				sb.append(" ");
				compensated += spaceLength;
			}
			player.sendMessage(sb.toString() + message);
		}

		public static String sendCenteredMessage2(String message) {
			if (message == null || message.equals("")) return "";
			message = ChatColor.translateAlternateColorCodes('&', message);

			int     messagePxSize = 0;
			boolean previousCode  = false;
			boolean isBold        = false;

			for (char c : message.toCharArray())
			{
				if (c == '§')
				{
					previousCode = true;
					continue;
				}
				else if (previousCode == true)
				{
					previousCode = false;
					if (c == 'l' || c == 'L')
					{
						isBold = true;
						continue;
					}
					else isBold = false;
				}
				else
				{
					DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
					messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
					messagePxSize++;
				}
			}

			int           halvedMessageSize = messagePxSize / 2;
			int           toCompensate      = (CENTER_PX/2) - halvedMessageSize;
			int           spaceLength       = DefaultFontInfo.SPACE.getLength() + 1;
			int           compensated       = 0;
			StringBuilder sb                = new StringBuilder();
			while (compensated < toCompensate)
			{
				sb.append(" ");
				compensated += spaceLength;
			}
			return sb.toString() + message;
		}

		public static String sendCenteredMessage3(String message) {
			if (message == null || message.equals("")) return "";
			message = ChatColor.translateAlternateColorCodes('&', message);

			int     messagePxSize = 0;
			boolean previousCode  = false;
			boolean isBold        = false;

			for (char c : message.toCharArray())
			{
				if (c == '§')
				{
					previousCode = true;
					continue;
				}
				else if (previousCode == true)
				{
					previousCode = false;
					if (c == 'l' || c == 'L')
					{
						isBold = true;
						continue;
					}
					else isBold = false;
				}
				else
				{
					DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
					messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
					messagePxSize++;
				}
			}

			int           halvedMessageSize = messagePxSize / 2;
			int           toCompensate      = (CENTER_PX/2) - halvedMessageSize;
			int           spaceLength       = DefaultFontInfo.SPACE.getLength() + 1;
			int           compensated       = 0;
			StringBuilder sb                = new StringBuilder();
			while (compensated < toCompensate)
			{
				sb.append(" ");
				compensated += spaceLength;
			}
			return sb.toString() + sb.toString() + message;
		}

		enum DefaultFontInfo {

			A('A', 5),
			a('a', 5),
			B('B', 5),
			b('b', 5),
			C('C', 5),
			c('c', 5),
			D('D', 5),
			d('d', 5),
			E('E', 5),
			e('e', 5),
			F('F', 5),
			f('f', 4),
			G('G', 5),
			g('g', 5),
			H('H', 5),
			h('h', 5),
			I('I', 3),
			i('i', 1),
			J('J', 5),
			j('j', 5),
			K('K', 5),
			k('k', 4),
			L('L', 5),
			l('l', 1),
			M('M', 5),
			m('m', 5),
			N('N', 5),
			n('n', 5),
			O('O', 5),
			o('o', 5),
			P('P', 5),
			p('p', 5),
			Q('Q', 5),
			q('q', 5),
			R('R', 5),
			r('r', 5),
			S('S', 5),
			s('s', 5),
			T('T', 5),
			t('t', 4),
			U('U', 5),
			u('u', 5),
			V('V', 5),
			v('v', 5),
			W('W', 5),
			w('w', 5),
			X('X', 5),
			x('x', 5),
			Y('Y', 5),
			y('y', 5),
			Z('Z', 5),
			z('z', 5),
			NUM_1('1', 5),
			NUM_2('2', 5),
			NUM_3('3', 5),
			NUM_4('4', 5),
			NUM_5('5', 5),
			NUM_6('6', 5),
			NUM_7('7', 5),
			NUM_8('8', 5),
			NUM_9('9', 5),
			NUM_0('0', 5),
			EXCLAMATION_POINT('!', 1),
			AT_SYMBOL('@', 6),
			NUM_SIGN('#', 5),
			DOLLAR_SIGN('$', 5),
			PERCENT('%', 5),
			UP_ARROW('^', 5),
			AMPERSAND('&', 5),
			ASTERISK('*', 5),
			LEFT_PARENTHESIS('(', 4),
			RIGHT_PERENTHESIS(')', 4),
			MINUS('-', 5),
			UNDERSCORE('_', 5),
			PLUS_SIGN('+', 5),
			EQUALS_SIGN('=', 5),
			LEFT_CURL_BRACE('{', 4),
			RIGHT_CURL_BRACE('}', 4),
			LEFT_BRACKET('[', 3),
			RIGHT_BRACKET(']', 3),
			COLON(':', 1),
			SEMI_COLON(';', 1),
			DOUBLE_QUOTE('"', 3),
			SINGLE_QUOTE('\'', 1),
			LEFT_ARROW('<', 4),
			RIGHT_ARROW('>', 4),
			QUESTION_MARK('?', 5),
			SLASH('/', 5),
			BACK_SLASH('\\', 5),
			LINE('|', 1),
			TILDE('~', 5),
			TICK('`', 2),
			PERIOD('.', 1),
			COMMA(',', 1),
			SPACE(' ', 3),
			DEFAULT('a', 4);

			private char character;
			private int  length;

			DefaultFontInfo(char character, int length) {
				this.character = character;
				this.length = length;
			}

			public char getCharacter() {
				return this.character;
			}

			public int getLength() {
				return this.length;
			}

			public int getBoldLength() {
				if (this == DefaultFontInfo.SPACE) return this.getLength();
				return this.length + 1;
			}

			public static DefaultFontInfo getDefaultFontInfo(char c) {
				for (DefaultFontInfo dFI : DefaultFontInfo.values())
				{
					if (dFI.getCharacter() == c) return dFI;
				}
				return DefaultFontInfo.DEFAULT;
			}
		}
	}
}
