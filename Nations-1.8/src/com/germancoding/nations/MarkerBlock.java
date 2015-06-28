package com.germancoding.nations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MarkerBlock {

	public NationPlayer player;
	public Location currentLocation;
	public ChangedBlock lastChangedBlock;

	// public Location upwardLocation;

	public MarkerBlock(NationPlayer p) {
		this.player = p;
	}

	public void calculateLocation() {
		int up = 3;
		/*
		 * if(upward)
		 * {
		 * up = 10;
		 * upwardLocation = player.getBukkitPlayer().getEyeLocation().add(0, up, 0);
		 * }
		 */
		currentLocation = player.getBukkitPlayer().getEyeLocation().add(0, up, 0);
		/*
		 * if(upwardLocation != null && currentLocation != null)
		 * {
		 * Location clone = currentLocation.clone();
		 * clone.setY(upwardLocation.getY());
		 * if(clone.distanceSquared(upwardLocation) <= 25) // five blocks 5 * 5
		 * {
		 * currentLocation = player.getBukkitPlayer().getEyeLocation().add(0, 10, 0);
		 * }
		 * }
		 */
	}

	@SuppressWarnings("deprecation")
	public void update() {
		calculateLocation();
		/*
		 * if (!Nations.getNationWorld().equals(currentLocation.getWorld())) {
		 * Nations.logger.warning("Marker Block in wrong world! Should be in " + Nations.getNationWorld().getName() + " but is in " + currentLocation.getWorld().getName() + "!");
		 * return;
		 * }
		 */
		Map<Material, Byte> map = getBlockData();
		if (this.lastChangedBlock != null)
			this.lastChangedBlock.restore();
		Block toCreate = this.currentLocation.getBlock();
		if (toCreate.getType() != Material.WOOL) // There could be already a marker - TODO: Maybe implement a better check for existing markers?
			this.lastChangedBlock = new ChangedBlock(toCreate.getLocation(), toCreate.getType(), toCreate.getData());
		Material m = map.keySet().iterator().next();
		Byte data = map.values().iterator().next();
		Iterator<NationPlayer> i = Nations.getIteratorOfPlayers();
		while (i.hasNext()) {
			NationPlayer p = i.next();
			if (p != player) {
				if (!p.getBukkitPlayer().getWorld().equals(currentLocation.getWorld())) {
					// Nations.logger.warning("Player " + p.getBukkitPlayer().getName() + " in wrong world! Should be in " + Nations.getNationWorld().getName() + " but is in " + p.getBukkitPlayer().getWorld() + "!");
					continue;
				}
				if (p.getBukkitPlayer().getLocation().distanceSquared(currentLocation) < (Bukkit.getViewDistance() * 16) * (Bukkit.getViewDistance() * 16))
					p.getBukkitPlayer().sendBlockChange(currentLocation, m, data);
			}
		}
	}

	public Map<Material, Byte> getBlockData() {
		ItemStack base = new ItemStack(Material.WOOL, 1);
		if (!player.hasNation())
			base.setDurability((short) 15);
		else if (player.getNation().equalsIgnoreCase("Zwerge"))
			base.setDurability((short) 11);
		else
			base.setDurability((short) 14);
		HashMap<Material, Byte> map = new HashMap<Material, Byte>();
		map.put(base.getType(), (byte) base.getDurability());
		return map;
	}

}
