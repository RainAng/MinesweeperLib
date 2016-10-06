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
import java.util.function.Predicate;

import static com.github.rainang.minesweeperlib.Minesweeper.Difficulty.*;

class ConsoleDemo
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0)
			return;
		if (args[0].equals("demo"))
			new ConsoleDemo();
	}
	
	private static final String WHITE_SPACE = "               ";
	
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
	
	private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	private final Minesweeper ms = new Minesweeper(new Random());
	
	private ConsoleDemo() throws IOException
	{
		List<Command> list = new ArrayList<>();
		list.add(new Command("-h", "list commands", c -> list.forEach(System.out::println)));
		list.add(new Command("-p", "print board", c -> printBoard()));
		
		list.add(new Command("-n", "start new game", c -> newGame()));
		list.add(new Command("-r", "restart game", c -> restartGame()));
		list.add(new Command("-x", "exit demo", c -> System.exit(0)));
		
		list.add(new Command("-b", "set beginner board; 9x9, 10 mines", c -> setDifficulty(BEGINNER)));
		list.add(new Command("-i", "set intermediate board; 16x16, 40 mines", c -> setDifficulty(INTERMEDIATE)));
		list.add(new Command("-e", "set expert board; 30x16, 99 mines", c -> setDifficulty(EXPERT)));
		
		String s = "\n" + WHITE_SPACE + "set custom board size and mine count";
		list.add(new Command("-c", new String[]{"width", "height", "mines"}, s, this::setCustom));
		
		list.add(new Command(new String[]{"x", "y"}, "clear/chord a tile", args -> open(args[0], args[1], false)));
		list.add(new Command("f", new String[]{"x", "y"}, "flag a tile", args -> open(args[1], args[2], true)));
		
		System.out.println(ANSI_LINE + "MinesweeperLib Demo\n" + ANSI_RESET);
		System.out.println("type -h for list of commands\n");
		printBoard();
		
		loop:
		while (true)
		{
			String string = br.readLine().toLowerCase();
			System.out.println();
			String[] split = string.split(" ");
			
			boolean flag = false;
			for (Command c : list)
				if (c.execute(split))
				{
					flag = true;
					break;
				}
			if (flag)
				continue;
			
			System.out.println(String.format("Unknown command: %s", string));
			System.out.println("type -h for list of commands");
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
			w = Math.min(32, Math.max(5, Integer.parseInt(cmd[1])));
			h = Math.min(32, Math.max(5, Integer.parseInt(cmd[2])));
			m = Math.min(w * h - 9, Math.max(5, Integer.parseInt(cmd[3])));
			ms.setDifficulty(w, h, m);
			printBoard();
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e)
		{
			System.out.println(String.format("Invalid arguments: %s", Arrays.toString(cmd)));
		}
	}
	
	private void open(String sx, String sy, boolean flag)
	{
		int x, y;
		
		try
		{
			x = Integer.parseInt(sx);
			if (x < 0 || x >= ms.getWidth())
			{
				System.out.println("X is out of range: " + x);
				return;
			}
			y = Integer.parseInt(sy);
			if (y < 0 || y >= ms.getHeight())
			{
				System.out.println("Y is out of range: " + y);
				return;
			}
			
			if (flag)
				ms.flag(x, y);
			else if (ms.getTile(x, y).isOpen())
				ms.clear(x, y, true);
			else
				ms.clear(x, y, false);
			
			printBoard();
		} catch (NumberFormatException e)
		{
			System.out.println(String.format("Invalid parameters: %s", Arrays.toString(new String[]{sx, sy})));
		}
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
			System.out.println("\nOps=" + ms.countOpenings() + " 3BV=" + ms.count3BV());
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
	
	private class Command
	{
		private final String command;
		
		private final String[] args;
		
		private final String description;
		
		private final Consumer<String[]> function;
		
		private final Predicate<String[]> validation;
		
		private Command(String command, String description, Consumer<String[]> function)
		{
			this.command = command;
			this.args = null;
			this.description = description;
			this.function = function;
			this.validation = args -> command.equals(args[0]);
		}
		
		private Command(String command, String[] args, String description, Consumer<String[]> function)
		{
			this.command = command;
			this.args = args;
			this.description = description;
			this.function = function;
			this.validation = a -> command.equals(a[0]) && a.length > 2;
		}
		
		private Command(String[] args, String description, Consumer<String[]> function)
		{
			this.command = null;
			this.args = args;
			this.description = description;
			this.function = function;
			this.validation = a ->
			{
				if (a.length != 2)
					return false;
				try
				{
					Integer.parseInt(a[0]);
					Integer.parseInt(a[1]);
					return true;
				} catch (Exception ignored) {}
				return false;
			};
		}
		
		private boolean execute(String[] args)
		{
			if (!validation.test(args))
				return false;
			function.accept(args);
			return true;
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("    ");
			if (command != null)
				sb.append(command + " ");
			if (args != null)
				for (String arg : args)
					sb.append("<").append(arg).append("> ");
			while (sb.length() < WHITE_SPACE.length())
				sb.append(" ");
			sb.append(description);
			return sb.toString();
		}
	}
}
