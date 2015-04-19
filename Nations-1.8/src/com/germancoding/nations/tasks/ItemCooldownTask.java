package com.germancoding.nations.tasks;

import java.util.ArrayList;

import com.germancoding.nations.Nations;

public class ItemCooldownTask implements Runnable {

	int task;
	final int id;
	Object lock = new Object();
	private static Object mainLock = new Object();
	int cooldown;

	private static ArrayList<ItemCooldownTask> instances = new ArrayList<ItemCooldownTask>();

	public static void shutdown() {
		synchronized (mainLock) {
			instances.clear();
		}
	}

	public void stopAndRemove() {
		synchronized (mainLock) {
			instances.remove(this);
		}
		Nations.scheduler.cancelTask(this.id);
	}

	public static void kill(int ID) {
		synchronized (mainLock) {
			ItemCooldownTask found = null;
			for (ItemCooldownTask t : instances) {
				if (t.id == ID) {
					found = t;
				}
			}
			if (found != null)
				found.stopAndRemove();
		}
	}

	public static synchronized int getRemainTime(int ID) {
		synchronized (mainLock) {
			for (ItemCooldownTask t : instances) {
				if (t.id == ID) {
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

	public ItemCooldownTask(int ID, int seconds) {
		this.id = ID;
		this.cooldown = seconds;
		if (getRemainTime(ID) == 0) {
			synchronized (mainLock) {
				instances.add(this);
			}
			this.task = Nations.scheduler.scheduleSyncRepeatingTask(Nations.plugin, this, 20L, 20L);
		} else {
			synchronized (mainLock) {
				for (ItemCooldownTask t : instances) {
					if (t.id == ID) {
						Nations.scheduler.cancelTask(t.task);
						t.task = Nations.scheduler.scheduleSyncRepeatingTask(Nations.plugin, t, 20L, 20L);
						t.cooldown = seconds;
					}
				}
			}
		}
	}

	@Override
	public void run() {
		synchronized (lock) {
			if (cooldown > 1)
				cooldown--;
			else {
				synchronized (mainLock) {
					instances.remove(this);
				}
				Nations.scheduler.cancelTask(this.task);
				if (Nations.DEBUG) {
					Nations.logger.info("[Cooldown] [Item] Cooldown finished at item id " + id);
				}
			}
		}
	}

}
