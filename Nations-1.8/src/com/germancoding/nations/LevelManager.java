package com.germancoding.nations;

import org.bukkit.ChatColor;

import com.germancoding.nations.skills.Skill;

public class LevelManager {

	public static void addExperience(NationPlayer p, NationItemStack i, double amount) {
		boolean levelup = false;
		Skill s = null;
		for (Skill skill : Skill.SKILLS)
			if (skill.getSkillType() == i.getType()) {
				s = skill;
				break;
			}
		if (s == null)
			throw new IllegalArgumentException("No skill matches the skill type " + i.getType() + "!");
		if (i.getExperience() == -1) // We already have the max level, do not add
			return;
		double currentExperience = i.getExperience() + amount; // Add experience
		int currentLevel = i.getLevel();

		double needed = 5;
		for (int level = 1; level <= currentLevel; level++) {
			needed += 1.3 * needed;
		}
		if (currentExperience >= needed) {
			levelup = true;
			currentExperience -= needed;
			currentLevel++;
			int cooldownBoni = i.getCooldownBoni() + 5; // Add the new boni
			int newCooldown = i.getDefaultCooldown() - cooldownBoni;
			i.setCooldownBoni(cooldownBoni);
			i.setCooldown(newCooldown);
			p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] Deine Fähigkeit " + s.getFriendlyName() + " hat sich auf Level " + currentLevel + " erhöht! Neue Cooldown: " + newCooldown + " Sekunden.");
			if (newCooldown <= i.getDefaultCooldown() / 10) {
				currentExperience = -1;
				newCooldown = i.getDefaultCooldown() / 10;
				i.setCooldown(newCooldown);
				i.setCooldownBoni(i.getDefaultCooldown() - newCooldown);
				p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] Du hast das maximale Level (" + currentLevel + ") für diese Fähigkeit erreicht. Endgültige Cooldown: " + i.getCooldown() + " Sekunden.");
			}
		}
		i.setExperience(currentExperience);
		i.setLevel(currentLevel);
		if (currentExperience >= 5 && levelup)
			addExperience(p, i, 0); // Redo the check to be save - possibility of two level-ups
	}

}
