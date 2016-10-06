package com.github.rainang.minesweeperlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

class ConsoleDemo
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0)
			return;
		if (args[0].equals("demo"))
			new ConsoleDemo();
	}
	
	private static final String BOMB = "\u25AA";
	private static final String FLAG = "\u25A3";
	private static final String TILE = "\u25A1";
	
	private static final String ANSI_RESET = "\u001B[0m";
	
	private static final String ANSI_LINE = "\u001B[4m";
	
	private static final String ANSI_BLACK = "\u001B[30m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_PURPLE = "\u001B[35m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_WHITE = "\u001B[37m";
	
	private static final String WHITE_SPACE = "                              ";
	
	private boolean auto_chord;
	
	private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	private final Minesweeper ms = new Minesweeper(new Random());
	private final List<Cmd> list = new ArrayList<>();
	
	private ConsoleDemo() throws IOException
	{
		list.add(new Cmd(new String[]{"-h", "-help"}, "list commands\n", c -> listCommands()));
		
		list.add(new Cmd(new String[]{"-n", "-new"}, "start new game", c -> newGame()));
		list.add(new Cmd(new String[]{"-r", "-restart"}, "restart game", c -> restartGame()));
		list.add(new Cmd(new String[]{"-exit"}, "exit demo\n", c -> System.exit(0)));
		
		list.add(new Cmd(new String[]{"-b", "-beginner"}, "set 9x9 board with 10 mines", c -> setDifficulty
				(Minesweeper.Difficulty.BEGINNER)));
		list.add(new Cmd(new String[]{"-i", "-intermediate"}, "set 16x16 board with 40 mines", c -> setDifficulty
				(Minesweeper.Difficulty.INTERMEDIATE)));
		list.add(new Cmd(new String[]{"-e", "-expert"}, "set 30x16 board with 99 mines", c -> setDifficulty
				(Minesweeper.Difficulty.EXPERT)));
		
		list.add(new Cmd(new String[]{"-c <width> <height> <mines>"}, "", this::setCustom));
		list.add(new Cmd(new String[]{"-custom <width> <height> <mines>"}, "\n" + WHITE_SPACE + "set custom board " +
				"size and mine count\n" + WHITE_SPACE + "width and height range is between 5 and 32\n" + WHITE_SPACE +
				"mine range is between 5 and (width * height - 9)\n", this::setCustom));
		
		list.add(new Cmd(new String[]{"<x> <y>"}, "clear a tile", c ->
		{
		}));
		list.add(new Cmd(new String[]{"<x> <y> c"}, "chord a tile", c ->
		{
		}));
		list.add(new Cmd(new String[]{"<x> <y> f"}, "flag a tile\n", c ->
		{
		}));
		
		list.add(new Cmd(new String[]{"-ac", "-auto-chord"}, "toggle auto-chord on/off\n", c -> toggleChord()));
		list.add(new Cmd(new String[]{"-p", "-print"}, "print board", c -> printBoard()));
		
		System.out.println(ANSI_LINE + "com.github.rainang.minesweeperapi.Minesweeper API Demo\n" + ANSI_RESET);
		listCommands();
		
		loop:
		while (true)
		{
			String string = br.readLine().toLowerCase();
			System.out.println();
			String[] split = string.split(" ");
			
			for (Cmd c : list)
				if (c.equals(split[0]))
				{
					c.execute(split);
					continue loop;
				}
			
			if (split.length >= 2)
			{
				int x, y;
				
				try
				{
					x = Integer.parseInt(split[0]);
					if (x < 0 || x >= ms.getWidth())
					{
						System.out.println("X is out of range: " + x);
						continue;
					}
					y = Integer.parseInt(split[1]);
					if (y < 0 || y >= ms.getHeight())
					{
						System.out.println("Y is out of range: " + y);
						continue;
					}
					
					if (split.length == 2)
					{
						Tile tile = ms.getTile(x, y);
						if (tile.isOpen())
						{
							if (auto_chord)
								ms.clear(x, y, true);
						} else
							ms.clear(x, y, false);
					} else
						switch (split[2])
						{
							case "c":
							case "chord":
								ms.clear(x, y, true);
								break;
							case "f":
							case "flag":
								ms.flag(x, y);
								break;
						}
					
					printBoard();
				} catch (NumberFormatException e)
				{
					System.out.println(String.format("Invalid parameters: %s", Arrays.toString(split)));
				}
			} else
			{
				System.out.println(String.format("Unknown command: %s", string));
				System.out.println("type -h or -help for list of commands");
			}
		}
	}
	
	private void newGame()
	{
		ms.newGame();
		printBoard();
	}
	
	private void restartGame()
	{
		ms.restartGame();
		printBoard();
	}
	
	private void setDifficulty(Minesweeper.Difficulty difficulty)
	{
		ms.setDifficulty(difficulty);
		printBoard();
	}
	
	private void setCustom(String[] cmd)
	{
		if (cmd.length != 4)
			return;
		int w, h, m;
		try
		{
			w = Math.min(32, Math.max(5, Integer.parseInt(cmd[2])));
			h = Math.min(32, Math.max(5, Integer.parseInt(cmd[3])));
			m = Math.min(w * h - 9, Math.max(5, Integer.parseInt(cmd[4])));
			ms.setDifficulty(w, h, m);
			printBoard();
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e)
		{
			System.out.println(String.format("Invalid arguments: %s", Arrays.toString(cmd)));
		}
	}
	
	private void toggleChord()
	{
		auto_chord = !auto_chord;
		System.out.println("Auto-chord " + auto_chord + "\n");
	}
	
	private void printBoard()
	{
		DecimalFormat df = new DecimalFormat("00");
		
		String w = df.format(ms.getWidth());
		String h = df.format(ms.getHeight());
		df = new DecimalFormat("000");
		String c = df.format(ms.getClicks());
		String a = df.format(ms.getActions());
		String m = df.format(ms.getMines());
		String f = df.format(ms.getFlagsUsed());
		System.out.println(String.format("Width  = %s \tMines = %s \tClicks  = %s", w, m, c));
		System.out.print(String.format("Height = %s \tFlags = %s \tActions = %s\n\n  ", h, f, a));
		
		df = new DecimalFormat("00");
		for (int x = 0; x < ms.getWidth(); x++)
			System.out.print(" " + df.format(x));
		System.out.println();
		
		for (int y = 0; y < ms.getHeight(); y++)
		{
			System.out.print(df.format(y) + " ");
			for (int x = 0; x < ms.getWidth(); x++)
			{
				Tile tile = ms.getTile(x, y);
				printTile(tile);
			}
			System.out.println(ANSI_RESET);
		}
		
		if (ms.getState() == Minesweeper.GameState.END)
		{
			System.out.print("\nGame Over!");
			if (ms.isWon())
				System.out.println(" YOU WIN!");
			else
				System.out.println(" YOU LOSE!");
			
			System.out.println("New Game or Restart? -n/-r\n");
		}
	}
	
	private void printTile(Tile tile)
	{
		if (tile.isOpen())
		{
			if (tile.isMine())
			{
				if (tile == ms.getLosingTile())
					System.out.print(ANSI_RED);
				else
					System.out.print(ANSI_WHITE);
				System.out.print(" " + BOMB + " ");
			} else
			{
				int i = tile.getMineCount();
				switch (i)
				{
					default:
						System.out.print("   ");
						return;
					case 1:
						System.out.print(ANSI_BLUE);
						break;
					case 2:
						System.out.print(ANSI_GREEN);
						break;
					case 3:
						System.out.print(ANSI_RED);
						break;
					case 4:
						System.out.print(ANSI_CYAN);
						break;
					case 5:
						System.out.print(ANSI_YELLOW);
						break;
					case 6:
						System.out.print(ANSI_PURPLE);
						break;
					case 7:
						System.out.print(ANSI_BLACK);
						break;
					case 8:
						System.out.print(ANSI_RESET);
						break;
				}
				System.out.print(" " + i + " ");
			}
		} else
		{
			if (tile.isFlag())
			{
				if (ms.getState() == Minesweeper.GameState.END && !tile.isMine())
					System.out.print(ANSI_RED + " " + FLAG + " ");
				else
					System.out.print(ANSI_RESET + " " + FLAG + " ");
			} else
				System.out.print(ANSI_WHITE + " " + TILE + " ");
		}
	}
	
	private void listCommands()
	{
		System.out.println("Commands:");
		list.forEach(Cmd::print);
	}
	
	private class Cmd
	{
		private String[] commands;
		private String description;
		private Consumer<String[]> consumer;
		
		private Cmd(String[] commands, String description, Consumer<String[]> consumer)
		{
			this.commands = commands;
			this.description = description;
			this.consumer = consumer;
		}
		
		private boolean equals(String cmd)
		{
			for (String s : commands)
				if (s.equals(cmd))
					return true;
			return false;
		}
		
		private void print()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("    " + commands[0]);
			if (commands.length > 1)
				for (int i = 1; i < commands.length; i++)
					sb.append(" | " + commands[i]);
			
			while (sb.length() < WHITE_SPACE.length())
				sb.append(" ");
			sb.append(description);
			System.out.println(sb.toString());
		}
		
		private void execute(String[] split)
		{
			consumer.accept(split);
		}
		
	}
}
