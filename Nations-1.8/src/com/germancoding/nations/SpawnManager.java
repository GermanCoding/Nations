package com.germancoding.nations;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpawnManager {

	private static ArrayList<Location> SPAWNS_ELFEN = new ArrayList<Location>();
	private static ArrayList<Location> SPAWNS_ZWERGE = new ArrayList<Location>();

	public static void loadElfenLocations(ArrayList<Location> spawns) {
		for (Location l : spawns) {
			if (l != null) {
				SPAWNS_ELFEN.add(l);
			}
		}
		if (Nations.DEBUG)
			Nations.logger.info("[SpawnManager] Loaded " + spawns.size() + " spawns for Elfen.");
	}

	public static void loadZwergeLocations(ArrayList<Location> locations) {
		for (Location l : locations) {
			if (l != null)
				SPAWNS_ZWERGE.add(l);
		}
		if (Nations.DEBUG)
			Nations.logger.info("[SpawnManager] Loaded " + locations.size() + " spawns for Dwarfs.");
	}

	public static void addLocationZwerge(Location l) {
		SPAWNS_ZWERGE.add(l);
	}

	public static void addLocationElfen(Location l) {
		SPAWNS_ELFEN.add(l);
	}

	public static List<Location> getAllLocationsFromZwerge() {
		return SPAWNS_ZWERGE;
	}

	public static List<Location> getAllLocationsFromElfen() {
		return SPAWNS_ELFEN;
	}

	// Get a save spawn without enemys.
	public static Location getSaveSpawnLocation(NationPlayer p) {
		World w = p.getBukkitPlayer().getWorld();
		ArrayList<Location> toCheck = new ArrayList<Location>(); // Add all
																	// enemy-positions
																	// here
		for (int i = 0; i < w.getPlayers().size(); i++) {
			Player pl = w.getPlayers().get(i);
			NationPlayer np = Nations.instanceOf(pl);
			if (np != null && np.hasNation() && !np.getNation().equals(p.getNation()))
				toCheck.add(pl.getLocation());
		}
		String nation = p.getNation();
		ArrayList<Location> SPAWNS;
		if (nation.equalsIgnoreCase("Zwerge")) {
			if (SPAWNS_ZWERGE.isEmpty())
				return w.getSpawnLocation();
			SPAWNS = SPAWNS_ZWERGE;
		} else {
			if (SPAWNS_ELFEN.isEmpty())
				return w.getSpawnLocation();
			SPAWNS = SPAWNS_ELFEN;
		}
		Location found = null;
		double distance = 50000;
		while (distance > 0 && found == null) {
			for (Location l : SPAWNS) {
				boolean save = true;
				if (found != null)
					break;
				for (Location check : toCheck) {
					if (l.distanceSquared(check) >= distance)
						continue;
					else {
						save = false;
						break;
					}
				}
				if (save) {
					found = l;
					break;
				}
			}
			distance -= 100;
		}
		if (found == null) {
			found = w.getSpawnLocation();
			if (Nations.DEBUG)
				Nations.plugin.getLogger().warning("No save spawn found but there were some?! Returning default spawn...");
		}
		return found;
	}

	public static Location getFirstNationSpawn(NationPlayer p) {
		String nation = p.getNation();
		ArrayList<Location> SPAWNS;
		if (nation.equalsIgnoreCase("Zwerge")) {
			if (SPAWNS_ZWERGE.isEmpty())
				return null;
			SPAWNS = SPAWNS_ZWERGE;
		} else {
			if (SPAWNS_ELFEN.isEmpty())
				return null;
			SPAWNS = SPAWNS_ELFEN;
		}
		return SPAWNS.get(0);
	}
}
