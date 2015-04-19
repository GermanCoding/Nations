package com.germancoding.nations;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.germancoding.nations.classes.NationClass;
import com.germancoding.nations.tasks.KitCooldownTask;

/**
 * Wrapper class for giving kits to players.
 * 
 * @author Max
 *
 */
public class KitManager {

	/**
	 * Gives the player the kit for his current class.
	 * 
	 * @param p
	 *            The player to give.
	 * @param check
	 *            If true the plugin will check whether the player is allowed to request a kit (cooldown). If false the check will be not performed, but the cooldown will be reset to 1 hour.
	 */
	public static void handleKit(NationPlayer p, boolean check) {
		if (check) {
			if (KitCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString()) != 0) {
				int n = KitCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString());
				p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Kit anfordern ist erst in " + TimeParser.secondsToDate(n) + " wieder möglich!");
				return;
			}
		}
		if (!p.hasClass()) {
			p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Keine Klasse gefunden!");
			return;
		}
		try {
			NationClass clasS = p.getClasS();
			for (ItemStack i : clasS.getKitItems()) {
				p.getBukkitPlayer().getInventory().addItem(i);
			}
			p.getBukkitPlayer().updateInventory();
			p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] Kit '" + Util.convertEnumToNormalString(clasS.getClassType()) + "' ausgegeben");
		} catch (Throwable e) {
			p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Fehler in der Kit-Ausgabe! (" + e.toString() + ")");
			return;
		}
		new KitCooldownTask(p.getBukkitPlayer().getUniqueId().toString(), 3600);
	}

}
