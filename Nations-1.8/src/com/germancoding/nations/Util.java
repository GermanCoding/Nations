package com.germancoding.nations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.germancoding.nations.skills.SkillType;

public class Util {

	public static int differ(int a, int b) {
		return Math.max(a, b) - Math.min(a, b);
	}
	
	public static NationItemStack getItemStackByType(NationPlayer p, SkillType t) {
		for (NationItemStack i : NationItemStack.getItemsOf(p.getBukkitPlayer().getUniqueId().toString())) {
			if (i.getType() == t)
				return i;
		}
		return null;
	}

	public static boolean canSee(Player p, Player other) {
		if (!p.getWorld().getName().equals(other.getWorld().getName()))
			return false;
		return p.hasLineOfSight(other) && p.getLocation().distanceSquared(other.getLocation()) <= (Bukkit.getViewDistance() * 16) * (Bukkit.getViewDistance() * 16);
	}

	/**
	 * Converts an enum to a "normal" string. Basically, a enum has mostly only UPPERCASE letters. This little converter converts "UPPERCASE" to "Uppercase".
	 * 
	 * @param t
	 *            Any enum to convert.
	 * @return The name of the enum which has the first letter uppercase and the rest lowercase.
	 */
	public static String convertEnumToNormalString(Enum<?> t) {
		return normalizeString(t.toString());
	}

	/**
	 * @see convertEnumToNormalString
	 */
	public static String normalizeString(String t) {
		String s = t.toLowerCase();
		char firstChar = Character.toUpperCase(s.charAt(0));
		s = s.substring(1);
		s = firstChar + s;
		return s;
	}

}
