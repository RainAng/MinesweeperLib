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
	 
	 @param ms the <code>Minesweeper</code> instance for referencing tiles
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
	 Resets this tile's attributes to its initial state.
	 */
	void reset()
	{
		restart();
		mine = false;
		mineCount = 0;
	}
	
	/**
	 Resets this tile's attributes to its initial state but leaves mine data the same.
	 */
	void restart()
	{
		open = false;
		flag = false;
	}
	
	/**
	 Toggles this tile's mine on/off.
	 */
	void mine()
	{
		mine = !mine;
		for (Tile neighbor : neighbors)
			neighbor.mineCount += mine ? 1 : -1;
	}
	
	/**
	 Toggles this tile's flag on/off.
	 <p>
	 If this tile is marked open, this does nothing.
	 </p>
	 
	 @return true if toggling occurred
	 */
	boolean flag()
	{
		if (open)
			return false;
		flag = !flag;
		return true;
	}
	
	/**
	 Marks this tile as opened.
	 <p>
	 If this tile contains a flag or is marked open, this does nothing. If this tile's mine count is equal to
	 <code>0</code>, this will call the <code>open</code> method on all neighboring tiles, potentially starting a chain
	 of <code>open</code> method calls.
	 </p>
	 
	 @return the amount of tiles opened or -1 if a mine has been revealed
	 */
	int open()
	{
		if (open || flag)
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
	 Performs a chord action on this tile.
	 <p>
	 If this tile has not been marked open, this does nothing. If this tile's mine count and flag count are not equal,
	 this does nothing. Otherwise, this calls the {@link #open() open} method on all neighboring tiles
	 that do not contain a flag.
	 </p>
	 
	 @return the amount of tiles opened or -1 if a mine has been revealed
	 
	 @see #open()
	 */
	int chord()
	{
		if (!open || mineCount == 0 || mineCount != getFlagCount())
			return 0;
		int i = 0;
		for (Tile neighbor : neighbors)
		{
			int j = neighbor.open();
			if (j == -1)
				return -1;
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
	 Returns true if this tile contains a mine.
	 
	 @return true if this tile contains a mine
	 */
	public boolean isMine()
	{
		return mine;
	}
	
	/**
	 Returns true if this tile has been cleared.
	 
	 @return true if this tile has been cleared
	 */
	public boolean isOpen()
	{
		return open;
	}
	
	/**
	 Returns true if this tile contains a flag.
	 
	 @return true if this tile contains a flag
	 */
	public boolean isFlag()
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
