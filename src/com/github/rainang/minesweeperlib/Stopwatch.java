package com.github.rainang.minesweeperlib;

/**
 Simple stopwatch class
 */
public class Stopwatch
{
	private long time;
	
	private long timeStart;
	
	private boolean running;
	
	public void start()
	{
		running = true;
		timeStart = System.currentTimeMillis();
	}
	
	public void stop()
	{
		running = false;
		time += System.currentTimeMillis() - timeStart;
	}
	
	public void reset()
	{
		running = false;
		time = 0;
	}
	
	public long getTime()
	{
		if (running)
			return System.currentTimeMillis() - timeStart + time;
		return time;
	}
}
