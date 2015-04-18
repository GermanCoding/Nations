package com.germancoding.nations;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardHandler {

	public static void addPlayer(NationPlayer p) {
		Nations.sm.getMainScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		Scoreboard playerboard = Nations.sm.getNewScoreboard();
		Objective nations = playerboard.registerNewObjective("nations", "dummy");
		nations.setDisplaySlot(DisplaySlot.SIDEBAR);
		nations.setDisplayName(ChatColor.GOLD + "=====Nations=====");
		Score elfenScore = nations.getScore(ChatColor.RED + "Elfen:");
		elfenScore.setScore(0);
		Score zwergenScore = nations.getScore(ChatColor.BLUE + "Zwerge:");
		zwergenScore.setScore(0);
		Score yourKills = nations.getScore(ChatColor.GRAY + "Deine Kills:");
		yourKills.setScore(0);
		Score yourDeath = nations.getScore(ChatColor.GRAY + "Deine Tode:");
		yourDeath.setScore(0);
		p.setBoard(playerboard);
		p.getBukkitPlayer().setScoreboard(playerboard);
	}

	public static void removePlayer(NationPlayer p) {
		p.getBukkitPlayer().setScoreboard(Nations.sm.getMainScoreboard());
	}

	public static void disable() {
		Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
		while (iterator.hasNext()) {
			removePlayer(iterator.next());
		}
	}

	public static void updateBoards() {
		Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
		while (iterator.hasNext()) {
			updateBoard(iterator.next());
		}
	}

	public static void updateBoard(NationPlayer p) {
		Scoreboard board = p.getBukkitPlayer().getScoreboard();
		if (board == Bukkit.getScoreboardManager().getMainScoreboard() || board == null || board == p.getBoard()) {
			p.getBukkitPlayer().setScoreboard(p.getBoard());
			board = p.getBoard();
			Objective nations = board.getObjective(DisplaySlot.SIDEBAR);
			Score elfenScore = nations.getScore(ChatColor.RED + "Elfen:");
			elfenScore.setScore(Nations.getPointsOfElfen());
			Score zwergenScore = nations.getScore(ChatColor.BLUE + "Zwerge:");
			zwergenScore.setScore(Nations.getPointsOfDwarfs());
			Score yourKills = nations.getScore(ChatColor.GRAY + "Deine Kills:");
			yourKills.setScore(p.getKills());
			Score yourDeath = nations.getScore(ChatColor.GRAY + "Deine Tode:");
			yourDeath.setScore(p.getDeath());
		}
		// if(p.hasNation())
		// {
		// Objective subName = board.getObjective(DisplaySlot.BELOW_NAME);
		// Score factionName =
		// subName.getScore(Bukkit.getOfflinePlayer(p.getNation()));
		// factionName.setScore(p.getKills());
		// }
	}

}
