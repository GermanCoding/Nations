package com.germancoding.nations.classes;

import org.bukkit.inventory.ItemStack;

public abstract class NationClass {

	public static NationClass[] CLASSES = { new KriegerClass(), new WindmagierClass(), new BogenschuetzeClass(), new MedizinmannClass(), new TiermeisterClass(), new SprengmeisterClass() };

	public abstract ItemStack[] getKitItems();

	public abstract ClassType getClassType();

	public static NationClass fromClassType(ClassType t) {
		for (NationClass c : CLASSES)
			if (c.getClassType() == t)
				return c;
		return null;
	}

	@Override
	public String toString() {
		return getClassType().name();
	}

}
