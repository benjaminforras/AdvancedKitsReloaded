package hu.tryharddevs.advancedkits.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class ItemStackUtil {
	/**
	 * Serialize an Object to a JsonObject.
	 * <p>
	 * The item itself will be saved as a Base64 encoded string to
	 * simplify the serialization and deserialization process. The result is
	 * not human readable.
	 * </p>
	 *
	 * @param object The item to serialize.
	 * @return A JsonObject with the serialized item.
	 */
	private static JsonObject serializeItem(Object object) {
		JsonObject values = new JsonObject();
		if (object == null) return null;

		if (object instanceof ItemStack) {

			ItemStack itemStack = (ItemStack) object;
			/*
			 * Check to see if the item is a skull with a null owner.
	         * This is because some people are getting skulls with null owners, which causes Spigot to throw an error
	         * when it tries to serialize the item. If this ever gets fixed in Spigot, this will be removed.
	         */
			if (itemStack.getType() == Material.PLAYER_HEAD) {
				SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
				if (meta.hasOwner() && meta.getOwningPlayer() == null) {
					itemStack.setItemMeta(Bukkit.getServer().getItemFactory().getItemMeta(Material.PLAYER_HEAD));
				}
			}
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); BukkitObjectOutputStream bos = new BukkitObjectOutputStream(outputStream)) {
			bos.writeObject(object);
			String encoded = Base64Coder.encodeLines(outputStream.toByteArray());

			values.addProperty("item", encoded.replaceAll("[\n\r]", "").trim());
		} catch (IOException ex) {
			AdvancedKitsMain.getPlugin().log(ChatColor.RED + "Error: " + ex.getMessage());
			AdvancedKitsMain.getPlugin().log(ChatColor.RED + "Unable to serialize item '" + object.toString() + "':");
			return null;
		}

		return values;
	}

	/**
	 * Get an ItemStack from a JsonObject.
	 *
	 * @param data The Json to read.
	 * @return The deserialized item stack.
	 */
	private static Object deserializeItem(JsonObject data) {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data.get("item").getAsString())); BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
			return dataInput.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			AdvancedKitsMain.getPlugin().log(ChatColor.RED + "Error: " + ex.getMessage());
			AdvancedKitsMain.getPlugin().log(ChatColor.RED + "Unable to deserialize an item");
			return new ItemStack(Material.AIR);
		}
	}

	public static String itemToString(ItemStack itemStack) {

		return serializeItem(itemStack).toString();
	}


	public static ItemStack itemFromString(String itemStackString) {
		return (ItemStack) deserializeItem(jsonFromString(itemStackString));
	}

	private static JsonObject jsonFromString(String jsonObjectStr) {
		JsonReader reader = new JsonReader(new StringReader(jsonObjectStr));
		reader.setLenient(true);

		JsonElement jsonElement = new JsonParser().parse(reader);
		return jsonElement.getAsJsonObject();
	}

	public static boolean isBoots(ItemStack item) {
		return isBoots(item.getType());
	}

	private static boolean isBoots(Material material) {
		return material.name().endsWith("BOOTS");
	}

	public static boolean isLegs(ItemStack item) {
		return isLegs(item.getType());
	}

	private static boolean isLegs(Material material) {
		return material.name().endsWith("LEGGINGS");
	}

	public static boolean isChest(ItemStack item) {
		return isChest(item.getType());
	}

	private static boolean isChest(Material material) {
		return material.name().endsWith("CHESTPLATE") || material.name().endsWith("ELYTRA");
	}

	public static boolean isHelmet(ItemStack item) {
		return isHelmet(item.getType());
	}

	private static boolean isHelmet(Material material) {
		return material.name().endsWith("HELMET");
	}

	public static boolean isArmor(ItemStack item) {
		return isArmor(item.getType());
	}

	private static boolean isArmor(Material type) {
		return isBoots(type) || isLegs(type) || isChest(type) || isHelmet(type);
	}

	public static boolean isShield(ItemStack item) {
		return isShield(item.getType());
	}

	private static boolean isShield(Material material) {
		return material.name().endsWith("SHIELD");
	}
}
