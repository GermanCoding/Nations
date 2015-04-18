package com.germancoding.nations;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TitleManager
{
	private static Class<?> nmsChatSerializer = Reflection.getNMSClass("ChatSerializer");

	public static void sendTitle(Player p, String title)
	{
		try
		{
			Object handle = Reflection.getHandle(p);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object serialized = Reflection.getMethod(nmsChatSerializer, "a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', title) + "\"}" });
			Object packet = PacketPlayOutTitle.class.getConstructor(new Class[] { EnumTitleAction.class, Reflection.getNMSClass("IChatBaseComponent") }).newInstance(new Object[] { EnumTitleAction.TITLE, serialized });
			Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException localNoSuchMethodException)
		{
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (InvocationTargetException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void sendSubTitle(Player p, String subtitle)
	{
		try
		{
			Object handle = Reflection.getHandle(p);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object serialized = Reflection.getMethod(nmsChatSerializer, "a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', subtitle) + "\"}" });
			Object packet = PacketPlayOutTitle.class.getConstructor(new Class[] { EnumTitleAction.class, Reflection.getNMSClass("IChatBaseComponent") }).newInstance(new Object[] { EnumTitleAction.SUBTITLE, serialized });
			Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException localNoSuchMethodException)
		{
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void sendTimings(Player p, int fadeIn, int stay, int fadeOut)
	{
		try
		{
			Object handle = Reflection.getHandle(p);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object packet = PacketPlayOutTitle.class.getConstructor(new Class[] { EnumTitleAction.class, Integer.TYPE, Integer.TYPE, Integer.TYPE }).newInstance(new Object[] { EnumTitleAction.TIMES, Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut) });
			Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException localNoSuchMethodException)
		{
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void reset(Player p)
	{
		try
		{
			Object handle = Reflection.getHandle(p);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object packet = PacketPlayOutTitle.class.getConstructor(new Class[] { EnumTitleAction.class }).newInstance(new Object[] { EnumTitleAction.RESET });
			Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException localNoSuchMethodException)
		{
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void clear(Player p)
	{
		try
		{
			Object handle = Reflection.getHandle(p);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object packet = PacketPlayOutTitle.class.getConstructor(new Class[] { EnumTitleAction.class }).newInstance(new Object[] { EnumTitleAction.CLEAR });
			Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException localNoSuchMethodException)
		{
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}
