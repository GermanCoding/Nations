package com.germancoding.nations.unsave;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Location;

public class v1_8 extends FakeDragon {
	private Object dragon;
	private int id;

	public v1_8(String name, Location loc) {
		super(name, loc);
	}

	@Override
	public Object getSpawnPacket() {
		Class<?> Entity = UnsaveUtils.getCraftClass("Entity");
		Class<?> EntityLiving = UnsaveUtils.getCraftClass("EntityLiving");
		Class<?> EntityEnderDragon = UnsaveUtils.getCraftClass("EntityEnderDragon");
		Object packet = null;
		try {
			dragon = EntityEnderDragon.getConstructor(UnsaveUtils.getCraftClass("World")).newInstance(getWorld());

			Method setLocation = UnsaveUtils.getMethod(EntityEnderDragon, "setLocation", new Class<?>[] { double.class, double.class, double.class, float.class, float.class });
			setLocation.invoke(dragon, getX(), getY(), getZ(), getPitch(), getYaw());

			Method setInvisible = UnsaveUtils.getMethod(EntityEnderDragon, "setInvisible", new Class<?>[] { boolean.class });
			setInvisible.invoke(dragon, isVisible());

			Method setCustomName = UnsaveUtils.getMethod(EntityEnderDragon, "setCustomName", new Class<?>[] { String.class });
			setCustomName.invoke(dragon, name);

			Method setHealth = UnsaveUtils.getMethod(EntityEnderDragon, "setHealth", new Class<?>[] { float.class });
			setHealth.invoke(dragon, health);

			Field motX = UnsaveUtils.getField(Entity, "motX");
			motX.set(dragon, getXvel());

			Field motY = UnsaveUtils.getField(Entity, "motX");
			motY.set(dragon, getYvel());

			Field motZ = UnsaveUtils.getField(Entity, "motX");
			motZ.set(dragon, getZvel());

			Method getId = UnsaveUtils.getMethod(EntityEnderDragon, "getId", new Class<?>[] {});
			this.id = (Integer) getId.invoke(dragon);

			Class<?> PacketPlayOutSpawnEntityLiving = UnsaveUtils.getCraftClass("PacketPlayOutSpawnEntityLiving");

			packet = PacketPlayOutSpawnEntityLiving.getConstructor(new Class<?>[] { EntityLiving }).newInstance(dragon);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getDestroyPacket() {
		Class<?> PacketPlayOutEntityDestroy = UnsaveUtils.getCraftClass("PacketPlayOutEntityDestroy");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityDestroy.newInstance();
			Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
			a.setAccessible(true);
			a.set(packet, new int[] { id });
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getMetaPacket(Object watcher) {
		Class<?> DataWatcher = UnsaveUtils.getCraftClass("DataWatcher");

		Class<?> PacketPlayOutEntityMetadata = UnsaveUtils.getCraftClass("PacketPlayOutEntityMetadata");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityMetadata.getConstructor(new Class<?>[] { int.class, DataWatcher, boolean.class }).newInstance(id, watcher, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getTeleportPacket(Location loc) {
		// Class<?> PacketPlayOutEntityTeleport = UnsaveUtils.getCraftClass("PacketPlayOutEntityTeleport");

		Object packet = null;

		try {
			// packet = new net.minecraft.server.v1_8_R1.PacketPlayOutEntityTeleport(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), true);
			// packet = PacketPlayOutEntityTeleport.getConstructor(new Class<?>[] { int.class, int.class, int.class, int.class, byte.class, byte.class , boolean.class}).newInstance(this.id, loc.getBlockX() * 32,
			// loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360), true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getWatcher() {
		Class<?> Entity = UnsaveUtils.getCraftClass("Entity");
		Class<?> DataWatcher = UnsaveUtils.getCraftClass("DataWatcher");

		Object watcher = null;
		try {
			watcher = DataWatcher.getConstructor(new Class<?>[] { Entity }).newInstance(dragon);
			Method a = UnsaveUtils.getMethod(DataWatcher, "a", new Class<?>[] { int.class, Object.class });

			a.invoke(watcher, 0, isVisible() ? (byte) 0 : (byte) 0x20);
			a.invoke(watcher, 6, (Float) health);
			a.invoke(watcher, 7, (Integer) 0);
			a.invoke(watcher, 8, (Byte) (byte) 0);
			a.invoke(watcher, 10, name);
			a.invoke(watcher, 11, (Byte) (byte) 1);
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (SecurityException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		} catch (InvocationTargetException e) {

			e.printStackTrace();
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}
		return watcher;
	}
}
