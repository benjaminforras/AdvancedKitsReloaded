package hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection;

import hu.tryharddood.advancedkits.Utils.ReflectionHelper.minecraft.DataWatcher;
import hu.tryharddood.advancedkits.Utils.ReflectionHelper.minecraft.Minecraft;
import org.bukkit.Location;

import java.util.UUID;

import static hu.tryharddood.advancedkits.Utils.ReflectionHelper.minecraft.DataWatcher.V1_9.ValueType.ENTITY_NAME;
import static hu.tryharddood.advancedkits.Utils.ReflectionHelper.minecraft.DataWatcher.V1_9.ValueType.ENTITY_NAME_VISIBLE;

public abstract class ClassBuilder {

	public static Object buildWitherSpawnPacket(int id, UUID uuid/*UUID*/, Location loc, Object dataWatcher) throws Exception {
		Object packet = hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.newInstance();
		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1))
		{
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("a")).set(packet, id);
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).set(packet, 64);// TODO: Find correct entity type id
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).set(packet, (int) loc.getX());
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).set(packet, hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.floor(loc.getY() * 32D));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).set(packet, (int) loc.getZ());

			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("i")).set(packet, (byte) hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.d(loc.getYaw() * 256F / 360F));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).set(packet, (byte) hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.d(loc.getPitch() * 256F / 360F));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).set(packet, (byte) hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.d(loc.getPitch() * 256F / 360F));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("l")).set(packet, dataWatcher);
		}
		else
		{
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("a")).set(packet, id);
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).set(packet, uuid);
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).set(packet, 64);
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("d")).set(packet, loc.getX());
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("e")).set(packet, loc.getY());
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("f")).set(packet, loc.getZ());

			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("j")).set(packet, (byte) hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.d(loc.getYaw() * 256F / 360F));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("k")).set(packet, (byte) hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.d(loc.getPitch() * 256F / 360F));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("l")).set(packet, (byte) hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.MathUtil.d(loc.getPitch() * 256F / 360F));
			hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("m")).set(packet, dataWatcher);
		}

		return packet;
	}

	public static Object buildNameMetadataPacket(int id, Object dataWatcher, int nameIndex, int visibilityIndex, String name) throws Exception {
		//		dataWatcher = setDataWatcherValue(dataWatcher, nameIndex, name != null ? name : "");// Pass an empty string to avoid exceptions
		//		dataWatcher = setDataWatcherValue(dataWatcher, visibilityIndex, (byte) (name != null && !name.isEmpty() ? 1 : 0));
		DataWatcher.setValue(dataWatcher, nameIndex, ENTITY_NAME, name != null ? name : "");// Pass an empty string to avoid exceptions
		DataWatcher.setValue(dataWatcher, visibilityIndex, ENTITY_NAME_VISIBLE, Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? (byte) (name != null && !name.isEmpty() ? 1 : 0) : (name != null && !name.isEmpty()));//Byte < 1.9, Boolean >= 1.9
		Object metaPacket = hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityMetadata.getConstructor(int.class, hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.DataWatcher, boolean.class).newInstance(id, dataWatcher, true);

		return metaPacket;
	}

	public static Object updateEntityLocation(Object entity, Location loc) throws Exception {
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.Entity.getDeclaredField("locX").set(entity, loc.getX());
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.Entity.getDeclaredField("locY").set(entity, loc.getY());
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.Entity.getDeclaredField("locZ").set(entity, loc.getZ());
		return entity;
	}

	//	public static Object buildDataWatcher(@Nullable Object entity) throws Exception {
	//		Object dataWatcher = NMSClass.DataWatcher.getConstructor(NMSClass.Entity).newInstance(entity);
	//		return dataWatcher;
	//	}
	//
	//	public static Object buildWatchableObject(int index, Object value) throws Exception {
	//		return buildWatchableObject(getDataWatcherValueType(value), index, value);
	//	}
	//
	//	public static Object buildWatchableObject(int type, int index, Object value) throws Exception {
	//		return NMSClass.WatchableObject.getConstructor(int.class, int.class, Object.class).newInstance(type, index, value);
	//	}
	//
	//	public static Object setDataWatcherValue(Object dataWatcher, int index, Object value) throws Exception {
	//		Object type = getDataWatcherValueType(value);
	//
	//		Object map = AccessUtil.setAccessible(NMSClass.DataWatcher.getDeclaredField("dataValues")).get(dataWatcher);
	//		NMUClass.gnu_trove_map_hash_TIntObjectHashMap.getDeclaredMethod("put", int.class, Object.class).invoke(map, index, buildWatchableObject(type, index, value));
	//
	//		return dataWatcher;
	//	}
	//
	//	public static Object getDataWatcherValue(Object dataWatcher, int index) throws Exception {
	//		Object map = AccessUtil.setAccessible(NMSClass.DataWatcher.getDeclaredField("dataValues")).get(dataWatcher);
	//		Object value = NMUClass.gnu_trove_map_hash_TIntObjectHashMap.getDeclaredMethod("get", int.class).invoke(map, index);
	//
	//		return value;
	//	}
	//
	//	public static int getWatchableObjectIndex(Object object) throws Exception {
	//		int index = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("b")).getInt(object);
	//		return index;
	//	}
	//
	//	public static int getWatchableObjectType(Object object) throws Exception {
	//		int type = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("a")).getInt(object);
	//		return type;
	//	}
	//
	//	public static Object getWatchableObjectValue(Object object) throws Exception {
	//		Object value = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("c")).get(object);
	//		return value;
	//	}
	//
	//	public static Object getDataWatcherValueType(Object value) {
	//		int type = 0;
	//		if (value instanceof Number) {
	//			if (value instanceof Byte) {
	//				type = 0;
	//			} else if (value instanceof Short) {
	//				type = 1;
	//			} else if (value instanceof Integer) {
	//				type = 2;
	//			} else if (value instanceof Float) {
	//				type = 3;
	//			}
	//		} else if (value instanceof String) {
	//			type = 4;
	//		} else if (value != null && value.getClass().equals(NMSClass.ItemStack)) {
	//			type = 5;
	//		} else if (value != null && (value.getClass().equals(NMSClass.ChunkCoordinates) || value.getClass().equals(NMSClass.BlockPosition))) {
	//			type = 6;
	//		} else if (value != null && value.getClass().equals(NMSClass.Vector3f)) {
	//			type = 7;
	//		}
	//
	//		return type;
	//	}

	public static Object buildArmorStandSpawnPacket(Object armorStand) throws Exception {
		Object spawnPacket = hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getConstructor(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.EntityLiving).newInstance(armorStand);
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setInt(spawnPacket, 30);

		return spawnPacket;
	}

	public static Object buildTeleportPacket(int id, Location loc, boolean onGround, boolean heightCorrection) throws Exception {
		Object packet = hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityTeleport.newInstance();
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("a")).set(packet, id);
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("b")).set(packet, (int) (loc.getX() * 32D));
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("c")).set(packet, (int) (loc.getY() * 32D));
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("d")).set(packet, (int) (loc.getZ() * 32D));
		hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.AccessUtil.setAccessible(hu.tryharddood.advancedkits.Utils.BossBarAPI.reflection.NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("e")).set(packet, (byte) (int) (loc.getYaw() * 256F / 360F));
		AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("f")).set(packet, (byte) (int) (loc.getPitch() * 256F / 360F));

		return packet;
	}
}
