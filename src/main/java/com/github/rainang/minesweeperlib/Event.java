package com.github.rainang.minesweeperlib;

public enum Event
{
	NEW_GAME,
	RESTART_GAME,
	TILE_CLEARED,
	TILE_CHORDED,
	TILE_FLAGGED,
	GAME_PAUSED,
	GAME_WON,
	GAME_LOST;
	
	public interface Listener
	{
		void onGameEvent(Minesweeper minesweeper, Event event);
	}
}
