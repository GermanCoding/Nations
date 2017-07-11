package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BogenschuetzeClass extends NationClass {

	@Override
	public ItemStack[] getKitItems() {
		ItemStack bows = new ItemStack(Material.BOW, 2);
		ItemStack arrow1 = new ItemStack(Material.ARROW, 64);
		ItemStack arrow2 = new ItemStack(Material.ARROW, 64);
		ItemStack arrow3 = new ItemStack(Material.ARROW, 64);
		ItemStack woodHelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack woodChestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack woodBoots = new ItemStack(Material.LEATHER_BOOTS, 1);
		ItemStack woodLeggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack[] items = { bows, arrow1, arrow2, arrow3, woodBoots, woodChestplate, woodHelmet, woodLeggings };
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.BOGENSCHUETZE;
	}

}
