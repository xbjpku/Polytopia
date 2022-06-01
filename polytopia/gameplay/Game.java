package polytopia.gameplay;

public class Game {

	public static TileMap map = null;
	public static Player[] players = null; 
	public static Player currentPlayer = null;

	public static Player getHumanPlayer() {
		// Human player is always index 0
		return players[0];
	}
}