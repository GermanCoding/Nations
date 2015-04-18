package com.germancoding.nations.tasks;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.germancoding.nations.Nations;

public class PvpTask implements Runnable {

	public final Player p;
	public int task;
	public Object lock = new Object();
	public boolean cooldown = true;
	private static Object mainLock = new Object();

	private static ArrayList<PvpTask> instances = new ArrayList<PvpTask>();

	public static void shutdown() {
		synchronized (mainLock) {
			instances.clear();
		}
	}

	public static boolean isInCooldown(Player p) {
		synchronized (mainLock) {
			for (PvpTask t : instances) {
				if (t.p.equals(p) || t.p == p) {
					return t.isInCooldown();
				}
			}
			return false;
		}
	}

	public boolean isInCooldown() {
		synchronized (lock) {
			return cooldown;
		}
	}

	public PvpTask(Player p, int seconds) {
		this.p = p;
		if (!isInCooldown(p)) {
			synchronized (mainLock) {
				instances.add(this);
			}
			this.task = Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, this, 20 * seconds);
		} else {
			synchronized (mainLock) {
				for (PvpTask t : instances) {
					if (t.p.equals(p) || t.p == p) {
						Nations.scheduler.cancelTask(t.task);
						t.task = Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, t, 20 * seconds);
					}
				}
			}
		}
	}

	@Override
	public void run() {
		synchronized (lock) {
			cooldown = false;
		}
		synchronized (mainLock) {
			instances.remove(this);
		}
	}
}
