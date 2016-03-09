package com.germancoding.nations;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Zombie{
	
	private org.bukkit.entity.Zombie entity;
	private NationPlayer owner;

	public Zombie(NationPlayer owner, Location spawnAt)
	{
		setEntity((org.bukkit.entity.Zombie) spawnAt.getWorld().spawnEntity(spawnAt, EntityType.ZOMBIE));
		setOwner(owner);
		
		ItemStack helmet = new ItemStack(Material.IRON_HELMET);
		ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
		ItemStack boots = new ItemStack(Material.IRON_LEGGINGS);
		entity.getEquipment().setBoots(boots);
		entity.getEquipment().setLeggings(leggings);
		entity.getEquipment().setChestplate(chestplate);
		entity.getEquipment().setHelmet(helmet);
	}
	
	public org.bukkit.entity.Zombie getEntity() {
		return entity;
	}

	public void setEntity(org.bukkit.entity.Zombie entity) {
		this.entity = entity;
	}

	public NationPlayer getOwner() {
		return owner;
	}

	public void setOwner(NationPlayer owner) {
		this.owner = owner;
	}
	
	

}
