package com.github.rainang.minesweeperlib;

import java.awt.event.ActionListener;
import java.util.*;

/**
 A Minesweeper board class.
 */
public final class Minesweeper
{
	public static final String NAME = "MinesweeperLib";
	
	public static final String VERSION = "${version}";
	
	private final Random rng = new Random();
	
	private final Stopwatch clock = new Stopwatch();
	
	private GameState gameState = GameState.INIT;
	
	private List<GameEvent.Listener> listeners = new ArrayList<>();
	
	private Tile[][] tiles;
	
	private int mines;
	
	private int winCondition;
	
	private int cleared;
	
	private int clicks;
	
	private int actions;
	
	private int flagsUsed;
	
	private boolean restarted;
	
	private boolean noFlagging;
	
	private Tile losingTile;
	
	private long seed;
	
	/**
	 Constructs a new board. The difficulty is set to <code>BEGINNER</code> by default.
	 */
	public Minesweeper()
	{
		setDifficulty(Difficulty.BEGINNER);
	}
	
	/**
	 Sets the game to a standard Minesweeper difficulty.
	 <p>
	 Note: This method will invoke the <code>newGame</code> method once the new difficulty has been set.
	 </p>
	 
	 @param difficulty the difficulty setting
	 
	 @see Difficulty
	 */
	public void setDifficulty(Difficulty difficulty)
	{
		switch (difficulty)
		{
		default:
		case BEGINNER:
			setDifficulty(9, 9, 10);
			return;
		case INTERMEDIATE:
			setDifficulty(16, 16, 40);
			return;
		case EXPERT:
			setDifficulty(30, 16, 99);
		}
	}
	
	/**
	 Sets a custom game difficulty. The minimum board size is <code>5x5</code>, maximum is <code>64x64</code>. Mines
	 must be less than or equal to 10 less the total amount of tiles. That is, <code>mines <= width * height -
	 10</code>. Each parameter will be clamped within its respective range.
	 <p>
	 Note: This method will invoke the <code>newGame</code> method once the new difficulty has been set.
	 </p>
	 
	 @param width  the width of the board
	 @param height the height of the board
	 @param mines  the amount of mines
	 */
	public void setDifficulty(int width, int height, int mines)
	{
		width = Math.min(64, Math.max(5, width));
		height = Math.min(64, Math.max(5, height));
		mines = Math.min(width * height - 10, Math.max(5, mines));
		
		this.tiles = new Tile[width][height];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				tiles[x][y] = new Tile(x, y);
		
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				tiles[x][y].initializeNeighbors(this);
		
		this.mines = mines;
		
		winCondition = getWidth() * getHeight() - mines;
		
		for (GameEvent.Listener l : listeners)
			l.onGameEvent(GameEvent.DIFFICULTY_CHANGE_EVENT, this, null);
		
		newGame();
	}
	
	
	/**
	 Generates a new board. This resets all the counters and generates new mines. This method uses a randomly generated
	 seed for generating mines.
	 */
	public void newGame()
	{
		newGame(rng.nextLong());
	}
	
	/**
	 Generates a new board. This resets all the counters and generates new mines. This method uses the specified
	 <code>seed</code> for generating mines.
	 
	 @param seed the seed to use for generating mines
	 */
	public void newGame(long seed)
	{
		this.seed = seed;
		rng.setSeed(seed);
		
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
				tiles[x][y].reset();
		
		for (int i = 0; i < getMines(); i++)
		{
			int x = rng.nextInt(getWidth());
			int y = rng.nextInt(getHeight());
			Tile t = tiles[x][y];
			if (t.isMine())
				i--;
			else
				t.toggleMine();
		}
		resetBoard(false);
		for (GameEvent.Listener l : listeners)
			l.onGameEvent(GameEvent.NEW_GAME_EVENT, this, null);
	}
	
	/**
	 Resets the board. This resets all counters but leaves mines as is.
	 */
	public void restartGame()
	{
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
				tiles[x][y].restart();
		resetBoard(true);
		for (GameEvent.Listener l : listeners)
			l.onGameEvent(GameEvent.RESTART_GAME_EVENT, this, null);
	}
	
	/**
	 Pauses or resumes the game. If the <code>gameState</code> is <code>PLAY</code>, the game will be paused. If the
	 <code>gameState</code> is <code>PAUSE</code>, the game will be resumed.
	 */
	public void pauseGame()
	{
		if (gameState == GameState.PLAY)
			setGameState(GameState.PAUSE);
		else if (gameState == GameState.PAUSE)
			setGameState(GameState.PLAY);
		else
			return;
		
		for (GameEvent.Listener l : listeners)
			l.onGameEvent(GameEvent.PAUSE_EVENT, this, null);
	}
	
	private void resetBoard(boolean restart)
	{
		cleared = 0;
		clicks = 0;
		actions = 0;
		flagsUsed = 0;
		losingTile = null;
		restarted = restart;
		setGameState(GameState.INIT);
	}
	
	/**
	 Registers a game event listener. Every listener's <code>onGameEvent</code> method will be invoked by order of
	 registry. That is, the first listener registered will be the first to be invoked.
	 
	 @param listener the <code>GameEvent.Listener</code> object to register
	 
	 @return <code>true</code> if the listener was successfully added
	 */
	public boolean addGameEventListener(GameEvent.Listener listener)
	{
		return listeners.add(listener);
	}
	
	
	/**
	 Removes a game event listener.
	 
	 @param listener the <code>GameEvent.Listener</code> object to register
	 
	 @return <code>true</code> if the listener was successfully added
	 */
	public boolean removeGameEventListener(GameEvent.Listener listener)
	{
		return listeners.remove(listener);
	}
	
	// GAME INPUT
	
	/**
	 Attempts to perform a flag action on the specified coordinate. A flag action cannot occur if the tile is open.
	 
	 @param x the x-coordinate of the tile
	 @param y the y-coordinate of the tile
	 
	 @return <code>true</code> if the flag action was successful
	 */
	public boolean flag(int x, int y)
	{
		if (gameState == GameState.END || gameState == GameState.PAUSE || noFlagging)
			return false;
		
		Tile tile = getTile(x, y);
		
		if (tile == null)
			return false;
		
		boolean b = tile.toggleFlag();
		
		if (gameState == GameState.INIT)
			return b;
		
		clicks++;
		actions += b ? 1 : 0;
		flagsUsed += b ? tile.hasFlag() ? 1 : -1 : 0;
		
		for (GameEvent.Listener l : listeners)
			l.onGameEvent(GameEvent.FLAG_EVENT, this, tile);
		
		return b;
	}
	
	/**
	 Attempts to perform an open action on the specified coordinate. An open action cannot occur if the tile is open or
	 contains a flag. If the action is successful, no mine is revealed, and no mines are nearby, all neighboring tiles'
	 <code>open</code> method will be invoked. This may start a chain of <code>open</code> method invocations until no
	 more tiles may be opened.
	 
	 @param x the x-coordinate of the tile
	 @param y the y-coordinate of the tile
	 
	 @return the amount of tiles opened. This will be negated if a mine is revealed.
	 */
	public int open(int x, int y)
	{
		return doAction(x, y, false);
	}
	
	/**
	 Attempts to perform a chord action on the specified coordinate. A chord action cannot occur if this tile is
	 <i>not</i> open, no mines are nearby, or the nearby mine and flag counts are not equal. Otherwise, this invokes
	 the <code>open</code> method on all neighboring tiles.
	 
	 @param x the x-coordinate of the tile
	 @param y the y-coordinate of the tile
	 
	 @return the amount of tiles opened. This will be negated if a mine is revealed.
	 
	 @see #open
	 */
	public int chord(int x, int y)
	{
		return doAction(x, y, true);
	}
	
	private int doAction(int x, int y, boolean chord)
	{
		if (gameState == GameState.END || gameState == GameState.PAUSE)
			return 0;
		
		Tile tile = getTile(x, y);
		
		if (tile == null)
			return 0;
		
		if (gameState == GameState.INIT)
		{
			if (chord)
				return 0;
			
			setGameState(GameState.PLAY);
			
			if (!restarted)
				relocateMines(tile);
		}
		
		int i = chord ? tile.chord() : tile.open();
		clicks++;
		actions += i > 0 ? 1 : 0;
		cleared += Math.abs(i);
		
		if (i != 0)
		{
			GameEvent e = chord ? GameEvent.CHORD_EVENT : GameEvent.OPEN_EVENT;
			for (GameEvent.Listener l : listeners)
				l.onGameEvent(e, this, tile);
		}
		
		if (i < 0)
		{
			setGameState(GameState.END);
			for (Tile[] ts : tiles)
				for (Tile t : ts)
				{
					if (t.isOpen() && t.isMine())
						losingTile = t;
					t.open();
				}
			for (GameEvent.Listener l : listeners)
				l.onGameEvent(GameEvent.LOSE_EVENT, this, tile);
			return i;
		} else if (cleared == winCondition)
		{
			setGameState(GameState.END);
			for (GameEvent.Listener l : listeners)
				l.onGameEvent(GameEvent.WIN_EVENT, this, tile);
		}
		
		return i;
	}
	
	private void relocateMines(Tile tile)
	{
		List<Tile> list = tile.getNeighbors();
		list.add(tile);
		
		int relocate = 0;
		
		for (Tile t : list)
			if (t.isMine())
			{
				t.toggleMine();
				relocate++;
			}
		
		while (relocate > 0)
		{
			int x = rng.nextInt(getWidth());
			int y = rng.nextInt(getHeight());
			Tile t = tiles[x][y];
			if (t != null && !t.isMine() && !list.contains(t))
			{
				t.toggleMine();
				relocate--;
			}
		}
	}
	
	private void setGameState(GameState gameState)
	{
		if (this.gameState == gameState)
			return;
		this.gameState = gameState;
		switch (gameState)
		{
		case INIT:
			clock.reset();
			break;
		case PLAY:
			clock.start();
			break;
		case PAUSE:
		case END:
			clock.stop();
			break;
		}
	}
	
	/**
	 Set <code>noFlagging</code> to <code>true</code> to start a no-flagging game. This method invokes the
	 <code>newGame</code> if the setting is changed.
	 
	 @param noFlagging <code>true</code> to start a no-flagging game
	 */
	public void setNoFlagging(boolean noFlagging)
	{
		if (this.noFlagging == noFlagging)
			return;
		
		this.noFlagging = noFlagging;
		newGame();
	}
	
	/**
	 Returns the width of the board.
	 
	 @return the width of the board
	 */
	public int getWidth()
	{
		return tiles.length;
	}
	
	/**
	 Returns the height of the board.
	 
	 @return the height of the board
	 */
	public int getHeight()
	{
		return tiles[0].length;
	}
	
	/**
	 Returns the amount of mines on the board.
	 
	 @return the amount of mines on the board
	 */
	public int getMines()
	{
		return mines;
	}
	
	/**
	 Returns the <code>Tile</code> object at the specified coordinates.
	 
	 @param x the x-coordinate of the tile
	 @param y the y-coordinate of the tile
	 
	 @return the <code>Tile</code> object at the specified coordinates, null if the coordinates are invalid
	 */
	public Tile getTile(int x, int y)
	{
		return (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) ? null : tiles[x][y];
	}
	
	private List<Tile> getTiles()
	{
		List<Tile> list = new ArrayList<>();
		for (Tile[] ts : tiles)
			Collections.addAll(list, ts);
		ActionListener l;
		return list;
	}
	
	/**
	 Returns the number of clicks on this board.
	 
	 @return the number of clicks on this board
	 */
	public int getClicks()
	{
		return clicks;
	}
	
	/**
	 Returns the number of actions on this board.
	 
	 @return the number of actions on this board
	 */
	public int getActions()
	{
		return actions;
	}
	
	/**
	 Returns the number of flags on this board.
	 
	 @return the number of flags on this board
	 */
	public int getFlagsUsed()
	{
		return flagsUsed;
	}
	
	/**
	 Returns the current game time in milliseconds.
	 
	 @return the current game time in milliseconds
	 */
	public long getTime()
	{
		return clock.getTime();
	}
	
	/**
	 Returns the seed used for generating this board's mines.
	 
	 @return the seed used for generating this board's mines
	 */
	public long getSeed()
	{
		return seed;
	}
	
	/**
	 Returns the current game state.
	 
	 @return the current game state
	 */
	public GameState getGameState()
	{
		return gameState;
	}
	
	/**
	 Returns the last opened tile that lost the game.
	 
	 @return the last opened tile that lost the game, null if the game is <i>not</i>lost
	 */
	public Tile getLosingTile()
	{
		return losingTile;
	}
	
	/**
	 Returns <code>true</code> if this board is restarted.
	 
	 @return <code>true</code> if this board is restarted
	 */
	public boolean isRestarted()
	{
		return restarted;
	}
	
	public boolean isNoFlagging()
	{
		return noFlagging;
	}
	
	/**
	 Returns <code>true</code> if the game is over, and the player won.
	 
	 @return <code>true</code> if the game is over, and the player won
	 */
	public boolean isGameWon()
	{
		return gameState == GameState.END && losingTile == null;
	}
	
	/**
	 Returns the number of openings on this board. This may return an inaccurate value if invoked during the
	 <code>INIT</code> game state due to the relocation of mines when the game begins.
	 
	 @return the number of openings on this board
	 */
	public int countOpenings()
	{
		List<Tile> list = getTiles();
		list.removeIf(t -> t.isMine() || t.getMineCount() > 0);
		
		int i = 0;
		while (!list.isEmpty())
		{
			Queue<Tile> q = new LinkedList<>();
			q.offer(list.remove(0));
			while (!q.isEmpty())
			{
				Tile t = q.poll();
				list.remove(t);
				t.getNeighbors()
				 .stream()
				 .filter(list::contains)
				 .forEach(n ->
				 {
					 list.remove(n);
					 q.offer(n);
				 });
			}
			i++;
		}
		return i;
	}
	
	/**
	 Returns the 3BV value of this board. This may return an inaccurate value if invoked during the
	 <code>INIT</code> game state due to the relocation of mines when the game begins.
	 
	 @return the 3BV value of this board
	 */
	public int count3BV()
	{
		List<Tile> list = getTiles();
		int shores = 0;
		for (Tile t : list)
		{
			if (t.isMine() || t.getMineCount() == 0)
				continue;
			for (Tile n : t.getNeighbors())
				if (!n.isMine() && n.getMineCount() == 0)
				{
					shores++;
					break;
				}
		}
		
		list.removeIf(t -> t.isMine() || t.getMineCount() == 0);
		return list.size() - shores + countOpenings();
	}
	
	private class Stopwatch
	{
		private long time;
		
		private long timeStart;
		
		private boolean running;
		
		private void start()
		{
			running = true;
			timeStart = System.currentTimeMillis();
		}
		
		private void stop()
		{
			running = false;
			time += System.currentTimeMillis() - timeStart;
		}
		
		private void reset()
		{
			running = false;
			time = 0;
		}
		
		private long getTime()
		{
			if (running)
				return System.currentTimeMillis() - timeStart + time;
			return time;
		}
	}
	
}
