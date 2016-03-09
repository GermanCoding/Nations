package com.germancoding.nations.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.germancoding.nations.NationPlayer;

public abstract class Skill implements Listener {

	public void register(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public abstract int activate(NationPlayer np);

	public abstract SkillType getSkillType();

	public abstract String[] getDescription();

	public abstract int getDefaultCooldown();

	public abstract Material getItemType();

	public abstract String getFriendlyName();

	public static Skill[] SKILLS = { new BlutrauschSkill(), new GolembeschwörungSkill(), new PfeilhagelSkill(), new SchutzSkill(), new TornadoSkill(), new UnsterblichkeitSkill(), new AusbruchDerWindeSkill(), new InflammatorSkill(), new FlySkill(), new PredatorSkill(), new ExplosionSkill(), new NachtDerUntotenSkill() };
}
