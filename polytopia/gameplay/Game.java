package polytopia.gameplay;

public class Game {

	public static TileMap map = null;
	public static Player[] players = null; 

	public static Player getHumanPlayer() {
		// Human player is always index 0
		return players[0];
	}

	private static int turn = 0;
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
							String[] playerFactions) {

		/* Use TileMap::MapGenerator::generate() */
		Game.players = new Player[playerFactions.length];
		for (int i = 0; i < players.length; i++)
			players[i] = new Player(playerFactions[i], i, (i == 0) ? false : true);
			
		Game.map = new TileMap(mapSize, mapSeed, mapType, Game.players);
	}

	/* Resume a previous game by loading MAP, PLAYERS and other stats. */
	public static void resume(){}

}