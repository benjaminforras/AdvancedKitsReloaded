package hu.tryharddevs.advancedkits.utils.menuapi.components.sub;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by ColonelHedgehog on 8/15/15.
 */
public class GUISound {
	private Sound   sound;
	private float   volume;
	private float   pitch;
	private boolean playOnClick;

	public GUISound(Sound sound) {
		this(sound, 1, 1, true);
	}

	public GUISound(Sound sound, float volume, float pitch) {
		this(sound, volume, pitch, true);
	}

	public GUISound(Sound sound, float volume, float pitch, boolean playOnClick) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.playOnClick = playOnClick;
	}

	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public boolean getPlayOnClick() {
		return playOnClick;
	}

	public void setPlayOnClick(boolean playOnClick) {
		this.playOnClick = playOnClick;
	}

	public void playGUISound(Player player) {
		playGUISound(player.getLocation(), player);
	}

	public void playGUISound(Location loc) {
		playGUISound(loc, null);
	}

	public void playGUISound(Location loc, Player player) {
		if (player == null) {
			loc.getWorld().playSound(loc, this.sound, this.pitch, this.volume);
		} else {
			player.playSound(loc, this.sound, this.pitch, this.volume);
		}
	}
}
