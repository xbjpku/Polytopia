package polytopia.gameplay;

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

import polytopia.graphics.Visualizable;
import polytopia.graphics.Render;
import polytopia.gameplay.Player.Faction;
import polytopia.gameplay.Player.Tech;


/** A tile on the game map. */
public class Tile implements Visualizable {

	private int x;
	private int y;

	/* Describes terrain of tiles. */
	public enum TerrainType {
		FIELD, FOREST, MOUNTAIN, SHORE, OCEAN
	}
	private TerrainType terrain;

	/* Describes how the tile varies from basic terrain. 
	   Generally, variation can be a Resource, Improvement or City. */
	private TileVariation variation; 

	/* Describes artstyle of this tile, for visualization. 
	   TODO: Change String to Faction. */
	private Faction style;

	/* Describes the unit (if exists) on this tile. */
	private Unit unit;

	/* Describes the owner city of this tile. */
	private City ownerCity;

	private Action[] actions;

	public Tile(int x, int y, TerrainType terrain, TileVariation variation, Faction style) {
		this.x = x;
		this.y = y;
		this.terrain = terrain;
		this.variation = variation;
		this.style = style;
		this.unit = null;
		this.ownerCity = null;

		this.actions = new Action[] {new ActionBuildLumberHut(this), new ActionBuildPort(this), 
									new ActionClearForest(this), new ActionBuildSawmill(this),
									new ActionBuildForge(this), new ActionBuildWindmill(this), 
									new ActionBuildCustomsHouse(this), new ActionBuildTemple(this),
									new ActionBuildForestTemple(this), new ActionBuildAquaTemple(this),
									new ActionBuildMountainTemple(this), new ActionBurnForest(this),
									new ActionGrowForest(this)};
	}

	public int getX() {return this.x;}
	public int getY() {return this.y;}
	public TerrainType getTerrainType() {return this.terrain;}
	public TileVariation getVariation() {return this.variation;}
	public Faction getStyle() {return this.style;}
	public City getOwnerCity() {return this.ownerCity;}
	public Unit getUnit() {return this.unit;}
	public void setUnit(Unit unit) {this.unit = unit;}
	public boolean hasEnemy(Player player) {
		return this.unit.getOwnerPlayer() != null && this.unit.getOwnerPlayer() != player;
	}
	public boolean hasAlly(Player player) {
		return this.unit.getOwnerPlayer() != null && this.unit.getOwnerPlayer() == player;
	}

	public Action[] getActions() {return this.actions;}

	public void setTerrainType(TerrainType type) {this.terrain = type;}
	public void setVariation(TileVariation variation) {this.variation = variation;}
	public void setStyle(Faction style) {this.style = style;}
	public void setOwnerCity(City city) {this.ownerCity = city;}

	// Selection response
	public void visualize(/* GUI component */) {
		System.out.printf("Tile (%d, %d) selected\n", this.x, this.y);

		/* Note for Shaw:
			Tile Jump Animation:
				Make the *OWNER CITY* of this TILE jump, if it exists.
				NON-BLOCKING. */
	}

	// Checks for ownership and unit occupation
	public boolean isOwnedBy(Player player) {
		if (ownerCity != null && ownerCity.getOwnerPlayer() == player) {
			return true;
		}
		return false;
	}

	public boolean isAccessibleTo(Player player) {
		if (this.isOwnedBy(player)) {
			if (unit == null || unit.getOwnerPlayer() == player)
				return true;
		}
		return false;
	}

	public boolean hasTemple() {
		return this.variation instanceof Improvement
				&& (((Improvement)(this.variation)).getImprovementType() == Improvement.ImprovementType.TEMPLE
				|| ((Improvement)(this.variation)).getImprovementType() == Improvement.ImprovementType.FOREST_TEMPLE
				|| ((Improvement)(this.variation)).getImprovementType() == Improvement.ImprovementType.AQUA_TEMPLE
				|| ((Improvement)(this.variation)).getImprovementType() == Improvement.ImprovementType.MOUNTAIN_TEMPLE);
	}



	public static void main(String[] args) {
		TilesTest display = new TilesTest();

		/* Makeshift Interactive Commandline, for debugging. */

		/* Dump basic info. */
		for(Player player : Game.players) {
			System.out.println (player.getFaction().toString());
			City capital = player.getCapital();
			System.out.printf ("Capital %s at (%d, %d)\n", capital.getName(), 
								capital.getOwnerTile().getX(), capital.getOwnerTile().getY());

			System.out.println ("Territory:");
			ArrayList<Tile> territory = capital.getTerritory();
			for (Tile tile : territory) {
				System.out.printf ("(%d, %d), %s, with %s\n",
									tile.getX(), tile.getY(), 
									tile.getTerrainType().toString(),
									tile.getVariation() instanceof TileVariation ?
									tile.getVariation().toString() : "nothing");
			}
			System.out.println();
		}

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.printf ("debug> ");
			switch (scanner.nextLine().strip().toLowerCase()) {
				case "i":
				case "info":
					if (Game.currentPlayer == null)
						System.out.println("no current player");
					else {
						System.out.printf ("Current player: %s\n", Game.currentPlayer.getFaction().toString());
						System.out.printf ("\tcities: ");
						for (City city : Game.currentPlayer.getCities())
							System.out.printf ("(%d, %d), ", city.getOwnerTile().getX(), city.getOwnerTile().getY());
						System.out.println();
						System.out.printf ("\tstars: %d\n", Game.currentPlayer.getStars());
						System.out.printf ("\ttechs: ");
						for (Tech tech : Game.currentPlayer.getTechs())
							System.out.printf ("%s, ", tech.toString());
						System.out.println();
					}
					break;
				case "p":
				case "player":
					for (int i = 0; i < Game.players.length; i++) {
						System.out.printf ("%d: %s\n", i, Game.players[i].getFaction().toString());
					}
					System.out.printf("switch to: ");
					int nextPlayer = 0;
					try {nextPlayer = scanner.nextInt();}
					catch (InputMismatchException e){break;}
					if (nextPlayer >= 0 && nextPlayer < Game.players.length) {
						Game.currentPlayer = Game.players[nextPlayer];
						System.out.printf ("current player switched to %s\n", Game.currentPlayer.getFaction().toString());
					}
					else
						System.out.println ("invalid player number");
					break;
				case "s":
				case "star":
					if (Game.currentPlayer == null)
						System.out.println("no current player");
					else {
						System.out.printf ("new star count: ");
						int newStar = 0;
						try {newStar = scanner.nextInt();}
						catch (InputMismatchException e){break;}
						Game.currentPlayer.setStars (newStar);
					}
					break;
				case "t":
				case "tech":
					if (Game.currentPlayer == null)
						System.out.println("no current player");
					else {
						System.out.printf ("new tech (ALL for all): ");
						String newTech = scanner.next().strip();
						System.out.println(newTech);
						if (newTech.equals("ALL")) {
							for (Tech tech : Tech.values())
								if (!Game.currentPlayer.getTechs().contains(tech))
									Game.currentPlayer.addTech(tech);
						}
						else {
							try {
								Tech tech = Tech.valueOf(newTech);
								if (!Game.currentPlayer.getTechs().contains(tech))
									Game.currentPlayer.addTech(tech);
							} catch (IllegalArgumentException e) {
								System.out.println ("invalid Tech name");
							}
						}
					}
					break;
				case "q":
				case "query":
					if (Game.currentPlayer == null) {
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
								if (action.isVisibleTo (Game.currentPlayer))
									actions.add(action);
								
							}
						}
						for (Action action : tile.getActions()) {
							if (action.isVisibleTo (Game.currentPlayer))
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
												action.isPerformableTo (Game.currentPlayer) ? "√" : "x", 
												action.toString());
							ArrayList<Consequence> conseqs = action.getConsequences(Game.currentPlayer);
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
							if (actions.get(actionIdx).isPerformableTo (Game.currentPlayer))
								actions.get(actionIdx).apply(Game.currentPlayer);
							else
								System.out.println ("action not performable");
						}
					}
					break;
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

			// refresh the graphics
			// display.update();
		}
	}
}

class TilesTest {

	private Object cond = new Object();
	private boolean loaded = false;
	private TestCanvas canvas = null;

	public void update() {
		this.canvas.repaint();
	}

	class TestCanvas extends JPanel {

			public TestCanvas() {
				/* Makeshift camera. */
				addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e) {
                		Render.camera.setStart(e.getX(), e.getY());

					}
        		});
				addMouseListener(new MouseAdapter() {
            		public void mouseReleased(MouseEvent e) {
						Point2D des = Render.camera.inverseTransPoint(new Point2D.Double((double)e.getX(), (double)e.getY()));
						int x = (int)Math.ceil(des.getX());
						int y = (int)Math.ceil(des.getY());
						if (!TileMap.isValid(Game.map.getGrid(), x, y)) {
							System.out.printf ("(%d, %d) is not on map", x, y);
							Render.setSelcected(null);
						}
						else {
							Tile tile = Game.map.getGrid()[y][x];
							if (Game.getHumanPlayer().getVision().contains(tile)) {
								Render.setSelcected(tile);
								//repaint();
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
							}
							else {
								Render.setSelcected(null);
								//repaint();
								System.out.printf ("(%d, %d), FOG\n", tile.getX(), tile.getY());
							}
						}
					}
				});

				addMouseWheelListener(new MouseAdapter() {
            		public void mouseWheelMoved(MouseWheelEvent e) {
						Render.camera.changeScale(e.getWheelRotation());
						//repaint();
            		}
        		});

				
        		addMouseMotionListener(new MouseAdapter() {
            		public void mouseDragged(MouseEvent e) {
						Render.camera.changePos(e.getX(), e.getY());
						//repaint();
            		}
       			});
				addMouseMotionListener(new MouseAdapter() {

					public void mouseMoved(MouseEvent e) {
						Render.camera.setMousePos(e.getX(), e.getY());
					}
				});
			}

			@Override
        	public void paintComponent(Graphics g) {
				super.paintComponent(g);  

				Graphics2D g2d = (Graphics2D) g;
				Render.render(g2d);
        	}
		}


	/** For testing the tiling visualization. */
	public TilesTest() {
		
		class TestWindow extends JFrame {
			private JFrame frame = null;

			public TestWindow() {
				frame = new JFrame("Tiles::Test");
				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				/* Makeshift: a fixed JPanel to draw on. */
				canvas = new TestCanvas();
				canvas.setSize(3000, 3000);
				canvas.setBackground (Color.BLACK);

				Random rnd = new Random(0);

				/* Use TileMap::MapGenerator::generate() */
				Game.players = new Player[] {new Player("Oumaji")};
				Game.map = new TileMap(18, (int)System.currentTimeMillis(), "CONTINENTS", Game.players);


				frame.setLayout(null);
				frame.add(canvas);
				frame.setVisible(true);

				synchronized (cond) {
					loaded = true;
					cond.notify();
				}
			}
		}
		javax.swing.SwingUtilities.invokeLater(() -> {
            new TestWindow();
        });

		synchronized (cond) {
			while (!loaded) {
				// Wait for the map to be loaded
				try {cond.wait();}
				catch (InterruptedException e) {}
			}
		}

		int delay = 15; //milliseconds
  		new Timer(delay, (ActionEvent evt) -> {
			  this.update();
		  }).start();
	}
}

