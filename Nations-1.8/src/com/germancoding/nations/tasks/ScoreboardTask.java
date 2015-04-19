package com.germancoding.nations.tasks;

import com.germancoding.nations.ScoreboardHandler;

public class ScoreboardTask implements Runnable {

	@Override
	public void run() {
		ScoreboardHandler.updateBoards();
	}
}
