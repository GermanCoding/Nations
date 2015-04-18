package com.germancoding.nations;

import java.util.ArrayList;

import org.bukkit.Location;

public class ForceField {

	public static ArrayList<ForceField> FIELDS = new ArrayList<ForceField>();
	private String nation;
	private Location min;
	private Location max;

	public ForceField(String nation, Location min, Location max) {
		setNation(nation);
		setMin(min);
		setMax(max);
		FIELDS.add(this);
	}

	public boolean isInsideField(Location l) {
		if (!l.getWorld().getName().equals(min.getWorld().getName()))
			return false;
		int x = l.getBlockX();
		int y = l.getBlockY();
		int z = l.getBlockZ();
		int minX = min.getBlockX();
		int minY = min.getBlockY();
		int minZ = min.getBlockZ();
		int maxX = max.getBlockX();
		int maxY = max.getBlockY();
		int maxZ = max.getBlockZ();
		if (x >= minX && y >= minY && z >= minZ) {
			if (x <= maxX && y <= maxY && z <= maxZ)
				return true;
		}
		return false;
	}

	public boolean canPlayerPassField(NationPlayer p, Location l) {
		if(!p.hasNation())
			return true;
		if (getNation().equalsIgnoreCase(p.getNation()))
			return true;
		if (isInsideField(l))
			return false;
		else
			return true;
	}

	public String getNation() {
		return nation;
	}

	public void setNation(String nation) {
		this.nation = nation;
	}

	public Location getMin() {
		return min;
	}

	public void setMin(Location min) {
		if (max != null && min != null)
			if (!max.getWorld().getName().equals(min.getWorld().getName()))
				throw new IllegalArgumentException("Min and max worlds for a force field are not equal!");
		this.min = min;
	}

	public Location getMax() {
		return max;
	}

	public void setMax(Location max) {
		if (max != null && min != null)
			if (!max.getWorld().getName().equals(min.getWorld().getName()))
				throw new IllegalArgumentException("Min and max worlds for a force field are not equal!");
		this.max = max;
	}
	
	public void delete()
	{
		FIELDS.remove(this);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " [nation " + getNation() + " | min: " + getMin() + " | max: " + getMax() + "]";
	}

}
