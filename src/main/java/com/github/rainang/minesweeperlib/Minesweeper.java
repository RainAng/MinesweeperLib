package com.github.rainang.minesweeperlib;

import java.util.*;

public final class Minesweeper
{
	/** Standard Minesweeper difficulties */
	enum Difficulty
	{
		/** 9x9 board, 10 mines */
		BEGINNER,
		/** 16x16 board, 40 mines */
		INTERMEDIATE,
		/** 30x16 board, 99 mines */
		EXPERT
	}
	
	enum GameState
	{
		INIT,
		PLAY,
		PAUSE,
		END
	}
	
	private GameState state = GameState.INIT;
	
	private final Random rng;
	
	private final Stopwatch clock = new Stopwatch();
	
	private Tile[][] tiles;
	
	private int mines;
	
	private int winCondition;
	
	private int cleared;
	
	private int clicks;
	
	private int actions;
	
	private int flagsUsed;
	
	private boolean restarted;
	
	private Tile losingTile;
	
	/**
	 Constructs a new Minesweeper board
	 
	 @param rng the random number generator used in generating mines
	 */
	public Minesweeper(Random rng)
	{
		this.rng = rng;
		setDifficulty(Difficulty.BEGINNER);
	}
	
	/**
	 Sets the game to a standard Minesweeper difficulty
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
	 Sets a custom game difficulty. All parameters must be greater than or equal to 5. Mines must be less than or
	 equal to 9 less the total amount of tiles (width*height).
	 
	 @param width  the width of the board
	 @param height the height of the board
	 @param mines  the amount of mines
	 
	 @throws IllegalArgumentException if any argument is less than 5
	 */
	public void setDifficulty(int width, int height, int mines)
	{
		if (width < 5 || height < 5 || mines < 5)
			throw new IllegalArgumentException(String.format("Args must be greater than or equal to 5: %s/%s/%s",
					width, height, mines));
		
		this.tiles = new Tile[width][height];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				tiles[x][y] = new Tile(x, y);
		
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				tiles[x][y].init(this);
		
		this.mines = Math.min(mines, width * height - 9);
		
		winCondition = getWidth() * getHeight() - mines;
		newGame();
	}
	
	/**
	 Starts a new game, resetting all counters and regenerating mines.
	 */
	public void newGame()
	{
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
				getTile(x, y).reset();
		
		for (int i = 0; i < getMines(); i++)
		{
			int x = rng.nextInt(getWidth());
			int y = rng.nextInt(getHeight());
			if (getTile(x, y).isMine())
				i--;
			else
				getTile(x, y).mine();
		}
		resetCounters();
		restarted = false;
	}
	
	/**
	 Restarts the game, resetting all counters but leaving mines as is.
	 */
	public void restartGame()
	{
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
				getTile(x, y).restart();
		resetCounters();
		restarted = true;
	}
	
	/**
	 Pauses the game
	 */
	public void pauseGame()
	{
		if (state == GameState.PLAY)
			setState(GameState.PAUSE);
		else if (state == GameState.PAUSE)
			setState(GameState.PLAY);
	}
	
	private void resetCounters()
	{
		cleared = 0;
		clicks = 0;
		actions = 0;
		flagsUsed = 0;
		losingTile = null;
		setState(GameState.INIT);
	}
	
	// GAME INPUT
	
	/**
	 Flags the tile in the given position
	 
	 @param x the x position
	 @param y the y position
	 
	 @return true if flagging was successful
	 
	 @throws IndexOutOfBoundsException if the given position is invalid
	 */
	public boolean flag(int x, int y)
	{
		if (state == GameState.END || state == GameState.PAUSE)
			return false;
		if (state == GameState.INIT)
			setState(GameState.PLAY);
		
		Tile tile = getTile(x, y);
		boolean b = tile.flag();
		clicks++;
		actions += b ? 1 : 0;
		flagsUsed += b ? tile.isFlag() ? 1 : -1 : 0;
		return b;
	}
	
	/**
	 If <code>chord</code>, attempts a chord move on the tile on the given position. Otherwise, attempts to clear the
	 tile normally.
	 
	 @param x     the x position
	 @param y     the y position
	 @param chord true to apply a chord action instead of a normal one
	 
	 @return true if at least 1 tile has been cleared
	 
	 @throws NullPointerException if the given position is invalid
	 */
	public boolean clear(int x, int y, boolean chord)
	{
		if (state == GameState.END || state == GameState.PAUSE)
			return false;
		
		Tile tile = getTile(x, y);
		
		if (state == GameState.INIT)
		{
			if (chord)
				return false;
			setState(GameState.PLAY);
			
			if (!restarted)
			{
				List<Tile> list = tile.getNeighbors();
				list.add(tile);
				int i = 0;
				for (Tile t : list)
					if (t.isMine())
					{
						t.mine();
						i++;
					}
				while (i > 0)
				{
					int x1 = rng.nextInt(getWidth());
					int y1 = rng.nextInt(getHeight());
					Tile t = getTile(x1, y1);
					if (!t.isMine() && !list.contains(t))
					{
						t.mine();
						i--;
					}
				}
			}
		}
		
		int i = chord ? tile.chord() : tile.open();
		clicks++;
		actions += i > 0 ? 1 : 0;
		cleared += i;
		if (i == -1)
		{
			setState(GameState.END);
			for (Tile[] ts : tiles)
				for (Tile t : ts)
				{
					if (t.isOpen() && t.isMine())
						losingTile = t;
					t.open();
				}
			return true;
		} else if (cleared == winCondition)
			setState(GameState.END);
		return i > 0;
	}
	
	// BOARD SETTERS & GETTERS
	
	private void setState(GameState state)
	{
		if (this.state == state)
			return;
		this.state = state;
		switch (state)
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
	 @return the board width
	 */
	public final int getWidth()
	{
		return tiles.length;
	}
	
	/**
	 @return the board height
	 */
	public final int getHeight()
	{
		return tiles[0].length;
	}
	
	/**
	 @return the amount of mines
	 */
	public final int getMines()
	{
		return mines;
	}
	
	/**
	 @param x the x position
	 @param y the y position
	 
	 @return the tile at the given position, null if invalid
	 */
	public Tile getTile(int x, int y)
	{
		if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
			return null;
		return tiles[x][y];
	}
	
	/**
	 @return the number of clicks this game so far
	 */
	public int getClicks()
	{
		return clicks;
	}
	
	/**
	 @return the number of successful click actions this game so far
	 */
	public int getActions()
	{
		return actions;
	}
	
	/**
	 @return the current amount of flags on the board
	 */
	public int getFlagsUsed()
	{
		return flagsUsed;
	}
	
	/**
	 @return true if this game is restarted
	 */
	public boolean isRestarted()
	{
		return restarted;
	}
	
	/**
	 @return true if the game is over, and the player won
	 */
	public boolean isWon()
	{
		return state == GameState.END && losingTile == null;
	}
	
	/**
	 @return the current game state
	 */
	public GameState getState()
	{
		return state;
	}
	
	/**
	 @return the opened tile containing a mine
	 */
	public Tile getLosingTile()
	{
		return losingTile;
	}
	
	// BOARD DATA
	
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
				t.getNeighbors().stream().filter(list::contains).forEach(n ->
				{
					list.remove(n);
					q.offer(n);
				});
			}
			i++;
		}
		return i;
	}
	
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
	
	private List<Tile> getTiles()
	{
		List<Tile> list = new ArrayList<>();
		for (Tile[] ts : tiles)
			Collections.addAll(list, ts);
		return list;
	}
}
