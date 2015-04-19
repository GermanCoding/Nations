package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WindmagierClass extends NationClass {

	@Override
	public ItemStack[] getKitItems() {
		ItemStack woodSword = new ItemStack(Material.WOOD_SWORD, 1);
		ItemStack woodHelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack woodChestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack woodBoots = new ItemStack(Material.LEATHER_BOOTS, 1);
		ItemStack woodLeggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack[] items = { woodSword, woodHelmet, woodChestplate, woodBoots, woodLeggings };
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.WINDMAGIER;
	}

}
