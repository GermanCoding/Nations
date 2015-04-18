package com.germancoding.nations;

public class Balancer {

	public static boolean elfenOverfilled() {
		int elfen = Nations.getElfenPayerCount();
		int dwarfs = Nations.getDwarfPlayerCount();
		if ((elfen + dwarfs) < 1)
			return false;
		double percentage = ((double) elfen) / ((double) (elfen + dwarfs));
		if (percentage > 0.55) // 55 % is the limit
			return true;
		return false;
	}

	public static boolean dwarfsOverfilled() {
		int elfen = Nations.getElfenPayerCount();
		int dwarfs = Nations.getDwarfPlayerCount();
		if ((elfen + dwarfs) < 1)
			return false;
		double percentage = ((double) dwarfs) / ((double) (elfen + dwarfs));
		if (percentage > 0.55)
			return true;
		return false;
	}

}
