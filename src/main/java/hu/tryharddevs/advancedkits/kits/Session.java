package hu.tryharddevs.advancedkits.kits;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class Session {

	private static HashMap<UUID, Session> sessions = new HashMap<>();

	private UUID uuid;

	private ArrayList<ItemStack> kitItems  = new ArrayList<>();
	private ArrayList<ItemStack> kitArmors = new ArrayList<>();

	private Session(UUID uuid) {
		this.uuid = uuid;
	}

	public static Session getSession(UUID uuid) {
		if (!sessions.containsKey(uuid)) {
			Session sett = new Session(uuid);
			sessions.put(uuid, sett);
			return sett;
		}
		Session sett = sessions.get(uuid);
		sett.setUUID(uuid);
		return sett;
	}

	private void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public ArrayList<ItemStack> getKitItems() {
		return kitItems;
	}

	public void addItems(ItemStack... itemStacks) {
		Collections.addAll(kitItems, itemStacks);
	}

	public void addArmor(ItemStack... itemStacks) {
		Collections.addAll(kitArmors, itemStacks);
	}

	public ArrayList<ItemStack> getKitArmors() {
		return kitArmors;
	}

}
