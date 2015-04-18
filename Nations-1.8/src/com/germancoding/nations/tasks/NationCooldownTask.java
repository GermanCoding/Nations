package com.germancoding.nations.tasks;

import java.util.ArrayList;

import com.germancoding.nations.Nations;

public class NationCooldownTask implements Runnable {

	int task;
	final String p;
	Object lock = new Object();
	private static Object mainLock = new Object();
	int cooldown;

	private static ArrayList<NationCooldownTask> instances = new ArrayList<NationCooldownTask>();

	public static void shutdown() {
		synchronized (mainLock) {
			instances.clear();
		}
	}

	public static String[] values() {
		synchronized (mainLock) {
			String[] values = new String[instances.size()];
			for (int i = 0; i < instances.size(); i++) {
				values[i] = instances.get(i).p;
			}
			return values;
		}
	}

	public static synchronized int getRemainTime(String player) {
		synchronized (mainLock) {
			for (NationCooldownTask t : instances) {
				if (t.p.equals(player)) {
					return t.getRemainTime();
				}
			}
			return 0;
		}
	}

	public int getRemainTime() {
		synchronized (lock) {
			return cooldown;
		}
	}

	public NationCooldownTask(String player, int seconds) {
		this.p = player;
		this.cooldown = seconds;
		if (getRemainTime(player) == 0) {
			synchronized (mainLock) {
				instances.add(this);
			}
			this.task = Nations.scheduler.scheduleSyncRepeatingTask(Nations.plugin, this, 20L, 20L);
		} else {
			synchronized (mainLock) {
				for (NationCooldownTask t : instances) {
					if (t.p.equals(player)) {
						Nations.scheduler.cancelTask(t.task);
						t.task = Nations.scheduler.scheduleSyncRepeatingTask(Nations.plugin, t, 20L, 20L);
						t.cooldown = seconds;
					}
				}
			}
		}
		if(Nations.DEBUG)
		{
			Nations.logger.info("[Cooldown] [Nation] New Cooldown: " + player + " , " + seconds);
		}
	}

	@Override
	public void run() {
		synchronized (lock) {
			if (cooldown >= 1)
				cooldown--;
			else {
				synchronized (mainLock) {
					instances.remove(this);
				}
				Nations.scheduler.cancelTask(this.task);
				cooldown = 0;
				if(Nations.DEBUG)
				{
					Nations.logger.info("[Cooldown] [Nation] Cooldown finished at player " + p);
				}
			}
		}
	}

}
