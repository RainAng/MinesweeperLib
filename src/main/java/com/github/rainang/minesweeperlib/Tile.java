package com.github.rainang.minesweeperlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 A <code>Minesweeper</code> tile.
 */
public final class Tile
{
	private final int x;
	
	private final int y;
	
	private boolean mine;
	
	private boolean open;
	
	private boolean flag;
	
	private int mineCount;
	
	private Tile[] neighbors;
	
	/**
	 Constructs a tile with the specified coordinates.
	 
	 @param x the x-coordinate
	 @param y the y-coordinate
	 */
	Tile(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 Lists all neighbors of this tile.
	 <p>
	 This method is called once every time the game <code>Difficulty</code> is changed.
	 </p>
	 
	 @param ms the <code>Minesweeper</code> object to reference
	 */
	void initializeNeighbors(Minesweeper ms)
	{
		List<Tile> list = new ArrayList<>();
		for (int j = y - 1; j < y + 2; j++)
			for (int i = x - 1; i < x + 2; i++)
				if (!(i == x && j == y))
					if (i >= 0 && j >= 0 && i < ms.getWidth() && j < ms.getHeight())
						list.add(ms.getTile(i, j));
		
		boolean bx = x == 0 || x == ms.getWidth() - 1;
		boolean by = y == 0 || y == ms.getHeight() - 1;
		int n = bx ^ by ? 5 : bx ? 3 : 8;
		neighbors = list.toArray(new Tile[n]);
	}
	
	/**
	 Resets the attributes of this tile to its initial state.
	 */
	void reset()
	{
		restart();
		mine = false;
		mineCount = 0;
	}
	
	/**
	 Resets the attributes of this tile to its initial state but leaves mine data the same.
	 */
	void restart()
	{
		open = false;
		flag = false;
	}
	
	/**
	 Toggles the <code>mine</code> attribute of this tile.
	 */
	void toggleMine()
	{
		mine = !mine;
		for (Tile neighbor : neighbors)
			neighbor.mineCount += mine ? 1 : -1;
	}
	
	/**
	 Attempts to perform a flag action. A flag action cannot occur if the tile is open.
	 
	 @return <code>true</code> if toggling occurred
	 */
	boolean toggleFlag()
	{
		if (isOpen())
			return false;
		flag = !flag;
		return true;
	}
	
	/**
	 Attempts to perform an open action. An open action cannot occur if the tile is open or contains a flag. If the
	 action is successful, no mine is revealed, and no mines are nearby, all neighboring tiles' <code>open</code>
	 method will be invoked. This may start a chain of <code>open</code> method invocations until no more tiles may be
	 opened.
	 
	 @return the amount of tiles opened. This will be negated if a mine is revealed.
	 */
	int open()
	{
		if (isOpen() || hasFlag())
			return 0;
		
		open = true;
		
		if (mine)
			return -1;
		
		int i = 1;
		if (mineCount == 0)
			for (Tile neighbor : neighbors)
				i += neighbor.open();
		return i;
	}
	
	/**
	 Attempts to perform a chord action. A chord action cannot occur if this tile is <i>not</i> open, no mines are
	 nearby, or the nearby mine and flag counts are not equal. Otherwise, this invokes the <code>open</code> method on
	 all neighboring tiles.
	 
	 @return the amount of tiles opened. This will be negated if a mine is revealed.
	 
	 @see #open()
	 */
	int chord()
	{
		if (!open || mineCount == 0 || getMineCount() != getFlagCount())
			return 0;
		int i = 0;
		for (Tile neighbor : neighbors)
		{
			int j = neighbor.open();
			if (j == -1)
				return -(i + 1);
			i += j;
		}
		return i;
	}
	
	/**
	 Returns the x-coordinate of this tile.
	 
	 @return the x-coordinate of this tile
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 Returns the y-coordinate of this tile.
	 
	 @return the y-coordinate of this tile
	 */
	public int getY()
	{
		return y;
	}
	
	/**
	 Returns <code>true</code> if this tile contains a mine.
	 
	 @return <code>true</code> if this tile contains a mine
	 */
	public boolean isMine()
	{
		return mine;
	}
	
	/**
	 Returns <code>true</code> if this tile is open.
	 
	 @return <code>true</code> if this tile is open
	 */
	public boolean isOpen()
	{
		return open;
	}
	
	/**
	 Returns <code>true</code> if this tile contains a flag.
	 
	 @return <code>true</code> if this tile contains a flag
	 */
	public boolean hasFlag()
	{
		return flag;
	}
	
	/**
	 Returns the number of neighboring tiles that contain a mine.
	 
	 @return the number of neighboring tiles that contain a mine
	 */
	public int getMineCount()
	{
		return mineCount;
	}
	
	/**
	 Returns the number of neighboring tiles that contain a flag.
	 
	 @return the number of neighboring tiles that contain a flag
	 */
	public int getFlagCount()
	{
		int i = 0;
		for (Tile neighbor : neighbors)
			if (neighbor.flag)
				i++;
		return i;
	}
	
	/**
	 Returns a list of all neighboring tiles.
	 
	 @return a list of all neighboring tiles
	 */
	public List<Tile> getNeighbors()
	{
		List<Tile> list = new ArrayList<>();
		Collections.addAll(list, neighbors);
		return list;
	}
}
