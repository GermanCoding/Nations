package com.germancoding.nations.skills;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;

public class BlutrauschSkill extends Skill {

	public int activate(NationPlayer np) {
		final Player p = np.getBukkitPlayer();
		p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
		p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 3)); // Level IV???
		p.setMetadata("ABILITY_BLUTRAUSCH", new FixedMetadataValue(Nations.plugin, 1));
		new BukkitRunnable() {

			@Override
			public void run() {
				p.removeMetadata("ABILITY_BLUTRAUSCH", Nations.plugin);
			}
		}.runTaskLater(Nations.plugin, 20 * 60);
		return 60;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.BLUTRAUSCH;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Gibt 60 Sekunden lang", "extremen Schadensboost!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1200; // 20 min
	}

	@Override
	public Material getItemType() {
		return Material.ENDER_PEARL;
	}

	@Override
	public String getFriendlyName() {
		return "Blutrausch";
	}
}
