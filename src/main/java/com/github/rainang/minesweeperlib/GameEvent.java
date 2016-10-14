package com.github.rainang.minesweeperlib;

/**
 An enum of game event types.
 */
public enum GameEvent
{
	NEW_GAME_EVENT,
	RESTART_GAME_EVENT,
	OPEN_EVENT,
	CHORD_EVENT,
	FLAG_EVENT,
	PAUSE_EVENT,
	WIN_EVENT,
	LOSE_EVENT,
	DIFFICULTY_CHANGE_EVENT;
	
	/**
	 The listener interface for receiving game events. The class that is interested in processing a game event
	 implements this interface, and the object created with that class is registered to a <code>Minesweeper</code>
	 object, using the <code>addGameEventListener</code> method. When the game event occurs, that object's
	 <code>onGameEvent</code> method is invoked.
	 */
	public interface Listener
	{
		/**
		 Invoked when a game event occurs.
		 
		 @param event       the event type
		 @param minesweeper the event source
		 @param tile        the tile where this event originated. This is only present for <code>OPEN_EVENT</code>,
		 <code>CHORD_EVENT</code>, and <code>FLAG_EVENT</code> types, null otherwise.
		 */
		void onGameEvent(GameEvent event, Minesweeper minesweeper, Tile tile);
	}
}
