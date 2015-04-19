package com.germancoding.nations;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.google.common.base.Preconditions;

/**
 * Class for saving old block states and restoring them.
 * Note: This is only virtual, used for the markers. The blocks are not changed in real, only packets to the clients are send.
 * 
 * @author Max
 *
 */
public class ChangedBlock {

	private Location location;
	private Material type;
	private byte data;

	/**
	 * 
	 * @param l
	 *            Location of this block.
	 * @param type
	 *            Original type for restoring.
	 * @param data
	 *            Original data for restoring.
	 */
	public ChangedBlock(Location l, Material type, byte data) {
		Preconditions.checkNotNull(l);
		Preconditions.checkNotNull(type);
		this.location = l;
		this.type = type;
		this.data = data;
	}

	/**
	 * Resets the block to the original state (when this object was created) using a virtual block change.
	 */
	@SuppressWarnings("deprecation")
	public void restore() {
		Block baseBlock = location.getBlock();
		if (baseBlock != null) {
			Iterator<NationPlayer> i = Nations.getIteratorOfPlayers();
			while (i.hasNext()) {
				NationPlayer p = i.next();
				Location playerLocation = p.getBukkitPlayer().getLocation();
				if (playerLocation.distanceSquared(location) < ((Bukkit.getViewDistance() * 16) * (Bukkit.getViewDistance() * 16) - 1))
					p.getBukkitPlayer().sendBlockChange(location, type, data);
			}
		} else {
			Nations.plugin.getLogger().warning("Unable to restore block at " + location.getX() + ", " + location.getBlockY() + ", " + location.getZ() + ", " + location.getWorld().getName());
		}
	}

}
