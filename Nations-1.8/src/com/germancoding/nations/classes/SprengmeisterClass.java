package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SprengmeisterClass extends NationClass {

	@Override
	public ItemStack[] getKitItems() {
		ItemStack helmet = new ItemStack(Material.IRON_HELMET);
		ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
		ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
		ItemStack boots = new ItemStack(Material.IRON_BOOTS);
		ItemStack tnt = new ItemStack(Material.TNT, 3);
		ItemStack plates = new ItemStack(Material.IRON_PLATE, 5);
		ItemStack[] items = { helmet, chestplate, leggings, boots, tnt, plates};
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.SPRENGMEISTER;
	}

}
