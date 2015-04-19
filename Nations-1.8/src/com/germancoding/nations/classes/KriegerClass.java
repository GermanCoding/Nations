package com.germancoding.nations.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class KriegerClass extends NationClass {

	@Override
	public ItemStack[] getKitItems() {
		ItemStack ironSword = new ItemStack(Material.IRON_SWORD, 1);
		ItemStack ironHelmet = new ItemStack(Material.CHAINMAIL_HELMET, 1);
		ItemStack ironChestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
		ItemStack ironBoots = new ItemStack(Material.CHAINMAIL_BOOTS, 1);
		ItemStack ironLeggings = new ItemStack(Material.CHAINMAIL_LEGGINGS, 1);
		ItemStack[] items = { ironSword, ironHelmet, ironChestplate, ironBoots, ironLeggings };
		return items;
	}

	@Override
	public ClassType getClassType() {
		return ClassType.KRIEGER;
	}
}
