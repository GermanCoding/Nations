package com.germancoding.nations;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ForceField {

	public static ArrayList<ForceField> FIELDS = new ArrayList<ForceField>();
	private String nation;
	private Location min;
	private Location max;
	private Location center;

	public ForceField(String nation, Location min, Location max) {
		setNation(nation);
		setMin(min);
		setMax(max);
		calculateCenter();
		FIELDS.add(this);
	}

	private void calculateCenter() {
		double centerX = ((getMax().getX() - getMin().getX()) / 2) + getMin().getX();
		double centerY = ((getMax().getY() - getMin().getY()) / 2) + getMin().getY();
		double centerZ = ((getMax().getZ() - getMin().getZ()) / 2) + getMin().getZ();
		Location center = new Location(getMin().getWorld(), centerX, centerY, centerZ);
		setCenter(center);
	}

	public boolean isInsideAndAwayFromEdges(Location l) {
		return isInsideField(l) && !isCloseToInsideEdge(l);
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

	public boolean isCloseToInsideEdge(Location l, int range) {
		if (!isInsideField(l))
			return false;

		int x = l.getBlockX();
		int z = l.getBlockZ();

		int minX = getMin().getBlockX();
		int minZ = getMin().getBlockZ();

		if (Util.differ(x, minX) <= range || Util.differ(z, minZ) <= range) {
			return true;
		}

		int maxX = getMax().getBlockX();
		int maxZ = getMax().getBlockZ();

		if (Util.differ(x, maxX) <= range || Util.differ(z, maxZ) <= range) {
			return true;
		}

		return false;
	}
	
	public boolean isCloseToInsideEdge(Location l) {
		return isCloseToInsideEdge(l, 3);
	}

	public boolean isNearField(Location l) {
		if (!l.getWorld().getName().equals(getMin().getWorld().getName())) {
			if (Nations.DEBUG)
				Nations.logger.warning("ForceField.isNearField() called on two different worlds!");
			return false;
		}

		double visibility = Math.pow((Bukkit.getViewDistance() * 16), 2);
		if (getMin().distanceSquared(l) <= visibility)
			return true;
		if (getMax().distanceSquared(l) <= visibility)
			return true;
		if (getCenter().distanceSquared(l) <= visibility)
			return true;
		return false;
	}

	public boolean canPlayerPassField(NationPlayer p, Location l) {
		if (!p.hasNation())
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

	public void delete() {
		FIELDS.remove(this);
	}

	public Location getCenter() {
		return center;
	}

	public void setCenter(Location center) {
		this.center = center;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " [nation " + getNation() + " | min: " + getMin() + " | max: " + getMax() + "]";
	}

}
