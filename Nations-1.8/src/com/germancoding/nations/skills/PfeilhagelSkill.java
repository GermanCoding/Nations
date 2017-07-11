package com.germancoding.nations.skills;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;

public class PfeilhagelSkill extends Skill {

	public int activate(NationPlayer np) {
		Random random = new Random();
		int amount = (random.nextInt(100) + 1) + 100; // Spawn between 101 and 200 arrows
		for (int i = 0; i < amount; i++) {
			double multiply = random.nextDouble();
			float multiply2 = random.nextFloat();
			int multiply3 = random.nextInt(3);
			int randomX = random.nextInt(20) - 10; // Spawn our arrows near the player, but not all at the same place -
			int randomZ = random.nextInt(20) - 10; // let the random object generate some places for our arrows to get a good effect
			World w = np.getBukkitPlayer().getLocation().getWorld();
			Vector direction = np.getBukkitPlayer().getLocation().getDirection().normalize().multiply(10d).multiply(multiply); // Make our arrows fast
			Location location = np.getBukkitPlayer().getEyeLocation().add(randomX, 2 + multiply3, randomZ);
			Arrow a = w.spawnArrow(location, direction, 2f + multiply2, 5f);
			a.setShooter(np.getBukkitPlayer());
			a.setMetadata("pfeilhagel", new MetadataValue() { // TODO: Use a FixedMetaDataValue

				@Override
				public Object value() {
					return true;
				}

				@Override
				public void invalidate() {
					;
				}

				@Override
				public Plugin getOwningPlugin() {
					return Nations.plugin;
				}

				@Override
				public String asString() {
					return "Pfeilhagel-Pfeil";
				}

				@Override
				public short asShort() {
					return 2;
				}

				@Override
				public long asLong() {
					return 2;
				}

				@Override
				public int asInt() {
					return 2;
				}

				@Override
				public float asFloat() {
					return 2;
				}

				@Override
				public double asDouble() {
					return 2;
				}

				@Override
				public byte asByte() {
					return 2;
				}

				@Override
				public boolean asBoolean() {
					return true;
				}
			});
		}
		return 3;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.PFEILHAGEL;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Schieße einen Pfeilhagel", "auf deine Gegner!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 300; // 5 min
	}

	@Override
	public Material getItemType() {
		return Material.FEATHER;
	}

	@Override
	public String getFriendlyName() {
		return "Pfeilhagel";
	}
}
