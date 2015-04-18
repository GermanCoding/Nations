package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MedizinmannClass extends NationClass{

	@Override
	public ItemStack[] getKitItems() {
		ItemStack dHelmet = new ItemStack(Material.IRON_HELMET, 1);
		ItemStack dChestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
		ItemStack dBoots = new ItemStack(Material.IRON_BOOTS, 1);
		ItemStack dLeggings = new ItemStack(Material.IRON_LEGGINGS, 1);
		ItemStack[] items = {dBoots, dChestplate, dHelmet, dLeggings};
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.MEDIZINMANN;
	}

}
