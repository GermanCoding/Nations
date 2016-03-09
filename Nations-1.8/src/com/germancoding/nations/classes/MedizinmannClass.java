package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MedizinmannClass extends NationClass {

	@Override
	public ItemStack[] getKitItems() {
		ItemStack dHelmet = new ItemStack(Material.IRON_HELMET, 1);
		ItemStack dChestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
		ItemStack dBoots = new ItemStack(Material.IRON_BOOTS, 1);
		ItemStack dLeggings = new ItemStack(Material.IRON_LEGGINGS, 1);
		ItemStack milk = new ItemStack(Material.MILK_BUCKET, 3);
		ItemStack apple = new ItemStack(Material.APPLE, 32);
		ItemStack[] items = { dBoots, dChestplate, dHelmet, dLeggings, milk, apple};
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.MEDIZINMANN;
	}

}
