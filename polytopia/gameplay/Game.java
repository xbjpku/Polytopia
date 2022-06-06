package polytopia.gameplay;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

import polytopia.gameplay.Player.Tech;
import polytopia.window.GameWindow;

public class Game {

	public static TileMap map = null;
	public static Player[] players = null; 
	public static GameWindow window = null;

	public static Player getHumanPlayer() {
		// Human player is always index 0
		return players[0];
	}

	private static int turn = 1;
	public static int currentPlayerId = 0;
	public static int getTurn() {return turn;}
	public static Player getCurrentPlayer() {
		return players[currentPlayerId];
	}

	public static void nextPlayer() {
		if (currentPlayerId+1 == players.length) {
			currentPlayerId = 0;
			turn++;
		}
		else
			currentPlayerId++;
		Game.getCurrentPlayer().play();
	}

	/* Start a new Game instance with given settings.*/
	public static void start(int mapSize, int mapSeed, String mapType, 
							String[] playerFactions, 
							GameWindow window) {

		/* Use TileMap::MapGenerator::generate() */
		Game.players = new Player[playerFactions.length];
		for (int i = 0; i < players.length; i++)
			players[i] = new Player(playerFactions[i], i, (i == 0) ? false : true);
			
		Game.map = new TileMap(mapSize, mapSeed, mapType, Game.players);
		Game.window = window;
	}

	/* Resume a previous game by loading MAP, PLAYERS and other stats. */
	public static void resume(){}


	public static void debugCommandLine() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.printf ("debug> ");
			switch (scanner.nextLine().strip().toLowerCase()) {
				case "i":
				case "info":
					if (Game.getCurrentPlayer() == null)
						System.out.println("no current player");
					else {
						System.out.printf ("Current player: %s\n", Game.getCurrentPlayer().getFaction().toString());
						System.out.printf ("\tcities: ");
						for (City city : Game.getCurrentPlayer().getCities())
							System.out.printf ("(%d, %d), ", city.getOwnerTile().getX(), city.getOwnerTile().getY());
						System.out.println();
						System.out.printf ("\tstars: %d\n", Game.getCurrentPlayer().getStars());
						System.out.printf ("\ttechs: ");
						for (Tech tech : Game.getCurrentPlayer().getTechs())
							System.out.printf ("%s, ", tech.toString());
						System.out.println();
					}
					break;
				case "s":
				case "star":
					if (Game.getCurrentPlayer() == null)
						System.out.println("no current player");
					else {
						System.out.printf ("new star count: ");
						int newStar = 0;
						try {newStar = scanner.nextInt();}
						catch (InputMismatchException e){break;}
						Game.getCurrentPlayer().setStars (newStar);
					}
					break;
				case "t":
				case "tech":
					if (Game.getCurrentPlayer() == null)
						System.out.println("no current player");
					else {
						System.out.printf ("new tech (ALL for all): ");
						String newTech = scanner.next().strip();
						System.out.println(newTech);
						if (newTech.equals("ALL")) {
							for (Tech tech : Tech.values())
								if (!Game.getCurrentPlayer().getTechs().contains(tech))
									Game.getCurrentPlayer().addTech(tech);
						}
						else {
							try {
								Tech tech = Tech.valueOf(newTech);
								if (!Game.getCurrentPlayer().getTechs().contains(tech))
									Game.getCurrentPlayer().addTech(tech);
							} catch (IllegalArgumentException e) {
								System.out.println ("invalid Tech name");
							}
						}
					}
					break;
				case "q":
				case "query":
					if (Game.getCurrentPlayer() == null) {
						System.out.println("no current player");
						break;
					}

					System.out.printf("(x,y) of query: ");
					int x,y;
					try {x = scanner.nextInt(); y = scanner.nextInt();}
					catch (InputMismatchException e) {break;}
					if (!TileMap.isValid(Game.map.getGrid(), x, y))
						System.out.printf ("(%d, %d) is not on map", x, y);
					else {
						Tile tile = Game.map.getGrid()[x][y];
						System.out.printf ("(%d, %d), %s, with %s\n",
									tile.getX(), tile.getY(), 
									tile.getTerrainType().toString(),
									tile.getVariation() != null ?
									tile.getVariation().toString() : "nothing");
						if (tile.getVariation() instanceof Improvement) {
							Improvement improvement = (Improvement) (tile.getVariation());
							System.out.printf ("%s at level %d\n", improvement.toString(), improvement.getLevel());
						}
						else if (tile.getVariation() instanceof City) {
							City city = (City)(tile.getVariation());
							System.out.printf ("City %s at (%d, %d)\n", city.getName(), 
												tile.getX(), tile.getY());
							System.out.printf ("Level: %d\t Population: %d\n", city.getLevel(), city.getPopulation());

							System.out.println ("Territory:");
							ArrayList<Tile> territory = city.getTerritory();
							for (Tile t : territory) {
								System.out.printf ("(%d, %d), %s, with %s\n",
													t.getX(), t.getY(), 
													t.getTerrainType().toString(),
													t.getVariation() instanceof TileVariation ?
													t.getVariation().toString() : "nothing");
							}
							System.out.println();
						}

						ArrayList<Action> actions = new ArrayList<>();
						if (tile.getVariation() != null) {
							for (Action action : tile.getVariation().getActions()) {
								if (action.isVisibleTo (Game.getCurrentPlayer()))
									actions.add(action);
							}
						}
						if (tile.getUnit() != null) {
							for (Action action : tile.getUnit().getActions()) {
								if (action.isVisibleTo (Game.getCurrentPlayer()))
									actions.add(action);
							}
						}
						for (Action action : tile.getActions()) {
							if (action.isVisibleTo (Game.getCurrentPlayer()))
								actions.add(action);
						}
						for (Action action : Game.getCurrentPlayer().getActions()) {
							if (action.isVisibleTo (Game.getCurrentPlayer()))
								actions.add(action);
						}

						if (actions.size() == 0) {
							System.out.printf ("no visible actions\n");
							break;
						}
						
						System.out.printf ("list of actions: \n");
						for (int idx = 0; idx < actions.size(); idx++) {
							Action action = actions.get(idx);
							System.out.printf ("(%d) %s, %s\n", idx, 
												action.isPerformableTo (Game.getCurrentPlayer()) ? "√" : "x", 
												action.toString());
							ArrayList<Consequence> conseqs = action.getConsequences(Game.getCurrentPlayer());
							System.out.printf ("\t");
							for (Consequence c : conseqs) {
								System.out.printf ("-> %s ", c.toString());
							}
							System.out.printf ("\n");
						}

						System.out.printf ("pick action (-1 to skip): ");
						int actionIdx = -1;
						try {actionIdx = scanner.nextInt();}
						catch (InputMismatchException e) {break;}
						if (actionIdx >= 0 && actionIdx < actions.size()) {
							if (actions.get(actionIdx).isPerformableTo (Game.getCurrentPlayer())) {
								Action action = actions.get(actionIdx);
								new Thread(()->{
									action.apply(Game.getCurrentPlayer());
								}).start();
							}
							else
								System.out.println ("action not performable");
						}
					}
					break;
				case "0":
					new Thread(()->{
						new ActionEndTurn().apply(Game.getCurrentPlayer());
					}).start();
				case "":
					break;
				default:
					System.out.println ("unknown command\n"+
										"Usage:\n"+
										"\t info(i) for dumping current player info\n"+
										"\t player(p) for changing current player\n"+
										"\t star(s) for setting current player star count\n"+
										"\t tech(t) for setting current player tech\n"+
										"\t query(q) for selecting tile, and more actions\n");
			}
		}
	}

}