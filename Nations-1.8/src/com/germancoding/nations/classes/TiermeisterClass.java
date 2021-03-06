package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TiermeisterClass extends NationClass {

	@Override
	public ItemStack[] getKitItems() {
		ItemStack woodHelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack woodChestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack woodBoots = new ItemStack(Material.LEATHER_BOOTS, 1);
		ItemStack woodLeggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		// ItemStack ironSword = new ItemStack(Material.STONE_SWORD, 1);
		ItemStack bone = new ItemStack(Material.BONE, 32);
		ItemStack fish = new ItemStack(Material.RAW_FISH, 32);
		ItemStack[] items = { woodBoots, woodChestplate, woodHelmet, woodLeggings, bone, fish };
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.TIERMEISTER;
	}

}
