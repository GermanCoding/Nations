package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SprengmeisterClass extends NationClass{

	@Override
	public ItemStack[] getKitItems() {
		ItemStack helmet = new ItemStack(Material.IRON_HELMET);
		ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
		ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
		ItemStack boots = new ItemStack(Material.IRON_BOOTS);
		ItemStack[] items = {helmet, chestplate ,leggings, boots};
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.SPRENGMEISTER;
	}

}
