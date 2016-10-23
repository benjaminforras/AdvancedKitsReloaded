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

package hu.tryharddood.advancedkits.MenuBuilder.chat.component;

import hu.tryharddood.advancedkits.MenuBuilder.ValueListener;
import hu.tryharddood.advancedkits.MenuBuilder.chat.ChatListener;
import hu.tryharddood.advancedkits.MenuBuilder.chat.LineBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * Checkbox MenuComponent
 * States are either <code>true</code> or <code>false</code> (checked/unchecked) returned by {@link #isChecked()}
 * By default is rendered either as <i>[✔]</i> or <i>[✖]</i>
 */
public class MenuComponentCheckbox extends MenuComponent {

	public static final String DEFAULT_FORMAT = " [%s] ";
	public static final String YES            = "✔";
	public static final String NO             = "✖";
	public static final String EMPTY          = " ";

	private boolean checked;
	private String format          = DEFAULT_FORMAT;
	private String stringChecked   = YES;
	private String stringUnchecked = NO;

	private ValueListener<Boolean> valueListener;

	/**
	 * Construct a new Checkbox
	 */
	public MenuComponentCheckbox() {
		this(false);
	}

	/**
	 * Construct a new Checkbox
	 *
	 * @param checked if <code>true</code> the checkbox will be checked
	 */
	public MenuComponentCheckbox(boolean checked) {
		this.checked = checked;
		updateText();
	}

	/**
	 * Changes the default format (<i> [%s] </i>)
	 *
	 * @param format the new format, must contain a variable (e.g. <i>%s</i>
	 * @return the Checkbox
	 */
	public MenuComponentCheckbox withFormat(@Nonnull String format) {
		this.format = format;
		updateText();
		return this;
	}

	/**
	 * Changes the default <b>checked</b> string (<i>✔</i>)
	 *
	 * @param stringChecked the new string
	 * @return the Checkbox
	 */
	public MenuComponentCheckbox withCheckedString(@Nonnull String stringChecked) {
		this.stringChecked = stringChecked;
		updateText();
		return this;
	}

	/**
	 * Changes the default <b>unchecked</b> string (<i>✖</i>)
	 *
	 * @param stringUnchecked the new string
	 * @return the Checkbox
	 */
	public MenuComponentCheckbox withUncheckedString(@Nonnull String stringUnchecked) {
		this.stringUnchecked = stringUnchecked;
		updateText();
		return this;
	}

	/**
	 * @return <code>true</code> if the checkbox is checked, <code>false</code> otherwise
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * Change the <i>checked</i>-state of the checkbox
	 *
	 * @param checked <code>true</code> if the checkbox should be set checked, <code>false</code> otherwise
	 * @return the Checkbox
	 */
	public MenuComponentCheckbox setChecked(boolean checked) {
		this.checked = checked;
		updateText();
		return this;
	}

	/**
	 * Add a {@link ValueListener} to be called when the value updates
	 *
	 * @param listener {@link ValueListener} to add
	 * @return the Checkbox
	 */
	public MenuComponentCheckbox onChange(final ValueListener<Boolean> listener) {
		this.valueListener = listener;
		return this;
	}

	@Override
	public String render() {
		String formatted = this.format;

		formatted = String.format(formatted, isChecked() ? stringChecked : stringUnchecked);

		return formatted;
	}

	public TextComponent build() {
		return this.component;
	}

	@Override
	public MenuComponent appendTo(final LineBuilder builder) {
		builder.append(new ChatListener() {
			@Override
			public void onClick(Player player) {
				boolean wasChecked = isChecked();
				setChecked(!wasChecked);
				if (valueListener != null) { valueListener.onChange(player, wasChecked, isChecked()); }
				if (builder.getContainer() != null) { builder.getContainer().refreshContent(); }
			}
		}, build());
		return this;
	}
}
