package com.github.rainang.minesweeperlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 Minesweeper tile class.
 */
public final class Tile
{
	private int x;
	
	private int y;
	
	private boolean mine;
	
	private boolean open;
	
	private boolean flag;
	
	private int count;
	
	private Tile[] neighbors;
	
	/**
	 @param x tile's x position
	 @param y tile's y position
	 */
	Tile(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 Initializes this tile, listing all neighboring tiles.
	 */
	void init(Minesweeper minesweeper)
	{
		boolean bx = x == 0 || x == minesweeper.getWidth() - 1;
		boolean by = y == 0 || y == minesweeper.getHeight() - 1;
		int n = bx ^ by ? 5 : bx ? 3 : 8;
		neighbors = new Tile[n];
		
		List<Tile> list = new ArrayList<>();
		for (int y = this.y - 1; y < this.y + 2; y++)
			for (int x = this.x - 1; x < this.x + 2; x++)
				if (!(x == this.x && y == this.y))
					if (x >= 0 && y >= 0 && x < minesweeper.getWidth() && y < minesweeper.getHeight())
						list.add(minesweeper.getTile(x, y));
		
		for (int i = 0; i < neighbors.length; i++)
			neighbors[i] = list.get(i);
	}
	
	/**
	 Resets all tile data to its initial state.
	 */
	void reset()
	{
		restart();
		mine = false;
		count = 0;
	}
	
	/**
	 A soft reset leaving mine data as is.
	 */
	void restart()
	{
		open = false;
		flag = false;
	}
	
	/**
	 Toggles this tile's mine on/off. Then updates neighboring tiles of changes.
	 */
	void mine()
	{
		mine = !mine;
		for (Tile neighbor : neighbors)
			neighbor.count += mine ? 1 : -1;
	}
	
	/**
	 Toggles this tile's flag on/off if it is not cleared. Otherwise, this does nothing.
	 
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
	 Opens this tile if it is neither flagged nor opened.
	 <p>If this tile has no mines nearby, all non-flagged, unopened neighbors will be opened. This method will
	 continue opening neighboring tiles recursively insofar as the condition for opening neighboring tiles are met
	 .</p>
	 
	 @return the amount of tiles opened; -1 if a mine has been revealed
	 */
	int open()
	{
		if (open || flag)
			return 0;
		
		open = true;
		
		if (mine)
			return -1;
		
		int i = 1;
		if (count == 0)
			for (Tile neighbor : neighbors)
				i += neighbor.open();
		return i;
	}
	
	/**
	 Opens all neighboring non-flagged, unopened tiles if this tile is opened and contains a nearby flag count equal
	 to the amount of miens nearby. If this tile has no mines nearby, this does nothing.
	 <p>This may initiate a recursive neighbor opening chain.</p>
	 
	 @return the amount of tiles opened; -1 if a mine has been revealed
	 
	 @see #open()
	 */
	int chord()
	{
		if (!open || count == 0 || count != getFlagCount())
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
	 @return the y position of this tile
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 @return the x position of this tile
	 */
	public int getY()
	{
		return y;
	}
	
	/**
	 @return true if this tile contains a mine
	 */
	public boolean isMine()
	{
		return mine;
	}
	
	/**
	 @return true if this tile has been cleared
	 */
	public boolean isOpen()
	{
		return open;
	}
	
	/**
	 @return true if this tile is flagged
	 */
	public boolean isFlag()
	{
		return flag;
	}
	
	/**
	 @return the amount of mines around this tile
	 */
	public int getMineCount()
	{
		return count;
	}
	
	/**
	 @return the amount of flags around this tile
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
	 @return a list of tiles surrounding this tile
	 */
	public List<Tile> getNeighbors()
	{
		List<Tile> list = new ArrayList<>();
		Collections.addAll(list, neighbors);
		return list;
	}
}
