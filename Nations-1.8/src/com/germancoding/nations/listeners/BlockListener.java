package com.germancoding.nations.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.InventoryView;

import com.germancoding.nations.Balancer;
import com.germancoding.nations.ForceField;
import com.germancoding.nations.IllegalItemException;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.PlayerMarker;
import com.germancoding.nations.SkillManager;
import com.germancoding.nations.InventoryViewHandler;
import com.germancoding.nations.SpawnManager;
import com.germancoding.nations.tasks.NationCooldownTask;

public class BlockListener implements Listener {

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		synchronized (invLock3) {
			if(Nations.instanceOf((Player) e.getPlayer()) != null)
			criticalPlayers.add(e.getPlayer().getUniqueId().toString());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		synchronized (invLock3) {
			if (criticalPlayers.contains(e.getWhoClicked().getUniqueId().toString())) {
				try {
					if (SkillManager.isSkillItem(e.getCurrentItem()) || SkillManager.isSkillItem(e.getCursor())) {
						e.setCancelled(true);
						return;
					}
				} catch (IllegalItemException e1) {
					;
				}
			}
		}
		synchronized (invLock) {
			if (inv1.get(e.getWhoClicked()) != null && inv1.get(e.getWhoClicked()).equals(e.getView())) {
				e.setCancelled(true);
				Player player = null;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getName().equals(e.getWhoClicked().getName())) {
						player = p;
						break;
					}
				}
				NationPlayer p = Nations.instanceOf(player);
				if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || !e.getCurrentItem().getItemMeta().hasDisplayName())
					return;
				String nation = e.getCurrentItem().getItemMeta().getDisplayName();
				boolean cont = false;
				if (ChatColor.stripColor(nation).equalsIgnoreCase("Zwerge") && !Balancer.dwarfsOverfilled()) {
					p.setNation("Zwerge");
					Nations.setDwarfPlayerCount(Nations.getDwarfPlayerCount() + 1);
					cont = true;
				} else if (ChatColor.stripColor(nation).equalsIgnoreCase("Elfen") && !Balancer.elfenOverfilled()) {
					p.setNation("Elfen");
					Nations.setElfenPlayerCount(Nations.getElfenPayerCount() + 1);
					cont = true;
				} else {
					return;
				}
				if (cont) {
					Nations.broadcastMessageToNation(p, ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getBukkitPlayer().getName() + ChatColor.GOLD + " ist deinem Volk " + ChatColor.GREEN
							+ "beigetreten!");
					new NationCooldownTask(p.getBukkitPlayer().getUniqueId().toString(), 2592000);
					inv1.get(player).close();
					inv1.remove(player);
					Nations.updatePlayerListName(p);
					PlayerMarker.updatePlayer(p);
					Location spawn = SpawnManager.getFirstNationSpawn(p);
					if(spawn != null)
					{
						p.getBukkitPlayer().teleport(spawn, TeleportCause.PLUGIN);
					}
					InventoryViewHandler.openClassSelectMenu(p);
				}
			}
		}
		synchronized (invLock2) {
			if (inv2.get(e.getWhoClicked()) != null && inv2.get(e.getWhoClicked()).equals(e.getView())) {

			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		synchronized (invLock3) {
			HumanEntity p = e.getPlayer();
			criticalPlayers.remove(p.getUniqueId().toString());
		}
		synchronized (invLock) {
			inv1.remove(e.getPlayer());
		}
		synchronized (invLock2) {
			inv2.remove(e.getPlayer());
		}
	}
	
	@EventHandler
	public void handleForceFieldExplosion(EntityExplodeEvent e)
	{
		for(Block b: e.blockList())
		{
			for(ForceField f: ForceField.FIELDS)
			{
				if(f.isInsideField(b.getLocation()))
					e.setCancelled(true);
			}
		}
	}

	public static HashMap<Player, InventoryView> inv1 = new HashMap<Player, InventoryView>();
	public static HashMap<Player, InventoryView> inv2 = new HashMap<Player, InventoryView>();
	public static ArrayList<String> criticalPlayers = new ArrayList<String>();
	public static Object invLock = new Object();
	public static Object invLock2 = new Object();
	public static Object invLock3 = new Object();
}
