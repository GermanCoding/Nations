package com.germancoding.nations.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//import net.minecraft.server.v1_6_R3.Packet52MultiBlockChange;
//import net.minecraft.server.v1_6_R3.Packet53BlockChange;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;

public class SchutzSkill extends Skill implements Listener {

	private static final HashMap<String, List<Location>> lastModified = new HashMap<String, List<Location>>();
	private static final HashMap<String, Integer> state = new HashMap<String, Integer>();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Location from = e.getFrom();
		Location to = e.getTo();
		if (from.getBlock().getLocation().equals(to.getBlock().getLocation()))
			return;
		if (!lastModified.containsKey(e.getPlayer().getName()))
			return;
		int state1 = state.get(e.getPlayer().getName());
		if (state1 == 1) {
			List<Location> list = lastModified.get(e.getPlayer().getName());
			List<Location> circle = getCircle(to, 10);
			for (Location l : list) {
				if (!circle.contains(l)) {
					for (Player p : e.getPlayer().getWorld().getPlayers()) {
						p.sendBlockChange(l, l.getBlock().getType(), l.getBlock().getData());
					}
				}
			}
			ArrayList<Location> newList = new ArrayList<Location>();
			for (Location l : circle) {
				newList.add(l.clone());
				if (!list.contains(l)) {
					for (Player p : e.getPlayer().getWorld().getPlayers()) {
						p.sendBlockChange(l, Material.WOOL, (byte) 14);
					}
				}
			}
			lastModified.put(e.getPlayer().getName(), newList);
		} else if (state1 == 2) {
			List<Location> list = lastModified.get(e.getPlayer().getName());
			for (Location l : list) {
				for (Player p : e.getPlayer().getWorld().getPlayers()) {
					p.sendBlockChange(l, l.getBlock().getType(), l.getBlock().getData());
				}
			}
			lastModified.remove(e.getPlayer().getName());
			state.put(e.getPlayer().getName(), 3);
		}
	}

	private Random random = new Random();

	@EventHandler
	public void blockSchutzDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (isNearSchutzPlayer(p) && random.nextInt(5) != 0) {
				e.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public int activate(final NationPlayer np) {
		state.put(np.getBukkitPlayer().getName(), 1);
		Nations.pm.registerEvents(new SchutzSkill(), Nations.plugin);
		final Player p = np.getBukkitPlayer();
		List<Location> list = getCircle(np.getBukkitPlayer().getLocation(), 10);
		lastModified.put(np.getBukkitPlayer().getName(), list);
		for (Location l : list) {
			p.sendBlockChange(l, Material.WOOL, (byte) 14);
		}
		/**
		 * @Override public void run() { List<Location> list =
		 *           lastModified.get(np.getBukkitPlayer().getName());
		 *           for(Location l: list) { Bukkit.broadcastMessage("Restore: "
		 *           + l.getBlock().getType().toString()); p.sendBlockChange(l,
		 *           l.getBlock().getType(), l.getBlock().getData());
		 *           //Packet53BlockChange packet = new
		 *           Packet53BlockChange(l.getBlockX(), l.getBlockY(),
		 *           l.getBlockZ(), ((org.bukkit.craftbukkit.v1_6_R3.CraftWorld)
		 *           l.getWorld()).getHandle()); //packet.material =
		 *           l.getBlock().getTypeId(); //packet.data =
		 *           l.getBlock().getData();
		 *           //((org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer)
		 *           p).getHandle().playerConnection.sendPacket(packet); }
		 *           List<Location> newList = getCircle(p.getLocation(), 5);
		 *           lastModified.put(p.getName(), newList); for(Location l:
		 *           list) { p.sendBlockChange(l, Material.WOOL, (byte) 14); } }
		 *           }, 5l, 5l);
		 **/

		Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, new Runnable() {
			@Override
			public void run() {
				state.put(np.getBukkitPlayer().getName(), 2);
			}
		}, 20 * 90);
		return 90;
	}

	public static boolean isNearSchutzPlayer(Player check) {
		for (String s : lastModified.keySet()) {
			Player p = Bukkit.getPlayer(s);
			if (p != null && p.isOnline()) {
				if (p.getWorld().getName().equals(check.getWorld().getName()) && !check.getName().equals(s)) {
					if (p.getLocation().distance(check.getLocation()) < 10.1D) {
						NationPlayer np = Nations.instanceOf(p);
						if (np != null) {
							NationItemStack i = Util.getItemStackByType(np, SkillType.SCHUTZ);
							LevelManager.addExperience(np, i, 0.5);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	// Thanks to my friend at the bukkit forums for this piece of code!
	public static List<Location> getCircle(Location centerLoc, int radius) {
		List<Location> circle = new ArrayList<Location>();
		World world = centerLoc.getWorld();
		/**
		 * Block heightBlock = centerLoc.getBlock(); while(heightBlock == null
		 * || heightBlock.getType() == Material.AIR) { centerLoc =
		 * centerLoc.subtract(0, 1, 0); heightBlock = centerLoc.getBlock(); }
		 **/
		int x = 0;
		int z = radius;
		int error = 0;
		int d = 2 - 2 * radius;
		while (z >= 0) {
			circle.add(new Location(world, centerLoc.getBlockX() + x, centerLoc.getY(), centerLoc.getBlockZ() + z));
			circle.add(new Location(world, centerLoc.getBlockX() - x, centerLoc.getY(), centerLoc.getBlockZ() + z));
			circle.add(new Location(world, centerLoc.getBlockX() - x, centerLoc.getY(), centerLoc.getBlockZ() - z));
			circle.add(new Location(world, centerLoc.getBlockX() + x, centerLoc.getY(), centerLoc.getBlockZ() - z));
			error = 2 * (d + z) - 1;
			if ((d < 0) && (error <= 0)) {
				x++;
				d += 2 * x + 1;
			} else {
				error = 2 * (d - x) - 1;
				if ((d > 0) && (error > 0)) {
					z--;
					d += 1 - 2 * z;
				} else {
					x++;
					d += 2 * (x - z);
					z--;
				}
			}
		}
		return circle;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.SCHUTZ;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Schützt alle Verbündeten", "im Umkreis von einigen Blöcken!", "Achtung: Dich nicht!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1200; // 20 min
	}

	@Override
	public Material getItemType() {
		return Material.WATER_BUCKET;
	}

	@Override
	public String getFriendlyName() {
		return "Schutz";
	}
}
