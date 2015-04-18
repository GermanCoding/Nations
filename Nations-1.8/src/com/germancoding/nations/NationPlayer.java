package com.germancoding.nations;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import com.germancoding.nations.classes.NationClass;
import com.germancoding.nations.tasks.DamageRemoverTask;

public class NationPlayer {

	private String nation;
	private NationClass clasS; //The word "class" is a keyword :(
	private Player bukkitPlayer;
	private Object lock = new Object();
	private int kills;
	private int death;
	private ChatMode mode;
	private Scoreboard board;
	private NationPlayer lastdamager;
	private boolean chat = true;

	public NationPlayer(Player p, String nation, NationClass clasS, int kills, int death, ChatMode mode) {
		synchronized (lock) {
			bukkitPlayer = p;
			this.nation = nation;
			this.clasS = clasS;
			this.kills = kills;
			this.death = death;
			this.mode = mode;
		}
	}

	public boolean hasNation() {
		if(nation == null)
			return false;
		return !nation.equalsIgnoreCase("null");
	}

	public boolean hasClass() {
		if(clasS == null)
			return false;
		return true;
	}

	public String getNation() {
		synchronized (lock) {
			return nation;
		}
	}

	public void setNation(String s) {
		synchronized (lock) {
			nation = s;
		}
	}

	public NationClass getClasS() {
		synchronized (lock) {
			return clasS;
		}
	}

	public void setClasS(NationClass s) {
		synchronized (lock) {
			clasS = s;
		}
	}

	public Player getBukkitPlayer() {
			return bukkitPlayer;
	}

	public void setBukkitPlayer(Player p) {
		synchronized (lock) {
			bukkitPlayer = p;
		}
	}

	public int getKills() {
		synchronized (lock) {
			return kills;
		}
	}

	public int getDeath() {
		synchronized (lock) {
			return death;
		}
	}

	public void setKills(int n) {
		synchronized (lock) {
			kills = n;
		}
	}

	public void setDeath(int n) {
		synchronized (lock) {
			death = n;
		}
	}

	public ChatMode getChatMode() {
		synchronized (lock) {
			return mode;
		}
	}

	public void setChatMode(ChatMode mode) {
		synchronized (lock) {
			this.mode = mode;
		}
	}

	public Scoreboard getBoard() {
		synchronized (lock) {
			return board;
		}
	}

	public void setBoard(Scoreboard board) {
		synchronized (lock) {
			this.board = board;
		}
	}

	public NationPlayer getLastdamager() {
		synchronized (lock) {
			return lastdamager;
		}
	}

	public void setLastdamager(NationPlayer lastdamager) {
		synchronized (lock) {
			this.lastdamager = lastdamager;
			if(lastdamager != null); // Notice to myself: Remove that ; when removing the comment below!
				//System.out.println("Setting " + this.getBukkitPlayer().getName() + "'s lastdamager to " + lastdamager.getBukkitPlayer().getName()); // TODO DEBUG!
		}
		if (lastdamager != null)
			new DamageRemoverTask(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof NationPlayer) {
			NationPlayer p = (NationPlayer) o;
			return p.getBukkitPlayer().getName().equals(this.getBukkitPlayer().getName());
		}
		return false;
	}

	public boolean canRecChat() {
		synchronized (lock) {
			return chat;
		}
	}

	public void setCanRecChat(boolean b) {
		synchronized (lock) {
			chat = b;
		}
	}
}
