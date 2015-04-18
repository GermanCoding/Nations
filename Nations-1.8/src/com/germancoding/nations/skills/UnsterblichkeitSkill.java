package com.germancoding.nations.skills;

import org.bukkit.Material;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;

public class UnsterblichkeitSkill extends Skill{

	public int activate(final NationPlayer np) {
		np.getBukkitPlayer().setMetadata("unsterblichkeit", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		new BukkitRunnable() {

			@Override
			public void run() {
				np.getBukkitPlayer().removeMetadata("unsterblichkeit", Nations.plugin);
			}

		}.runTaskLater(Nations.plugin, 1200L);
		NationItemStack i = Util.getItemStackByType(np, SkillType.UNSTERBLICHKEIT);
		LevelManager.addExperience(np, i, 0.3);
		return 60;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.UNSTERBLICHKEIT;
	}

	@Override
	public String[] getDescription() {
		String[] desc = {"Werde für 60", "Sekunden unsterblich!", "Achtung: Du kannst keine", "Angriffe starten!"};
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1200; // 20 min
	}

	@Override
	public Material getItemType() {
		return Material.DIAMOND_CHESTPLATE;
	}

	@Override
	public String getFriendlyName() {
		return "Unsterblichkeit";
	}
}
