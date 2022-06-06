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
import polytopia.graphics.Motion;
import polytopia.graphics.Movable;
import polytopia.gameplay.Player.Faction;
import polytopia.gameplay.Player.Tech;


/** A tile on the game map. */
public class Tile implements Visualizable, Movable {

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

	private Motion motion;

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
		return this.unit != null && this.unit.getOwnerPlayer() != player;
	}
	public boolean hasAlly(Player player) {
		return this.unit != null && this.unit.getOwnerPlayer() != player;
	}

	public Action[] getActions() {return this.actions;}

	public void setTerrainType(TerrainType type) {this.terrain = type;}
	public void setVariation(TileVariation variation) {this.variation = variation;}
	public void setStyle(Faction style) {this.style = style;}
	public void setOwnerCity(City city) {this.ownerCity = city;}

	public void setMotion(Motion m) {this.motion = m;}
	public Motion getMotion(){return this.motion;}

	// Selection response
	public void visualize() {

		Player humanPlayer = Game.getHumanPlayer();
		if (!humanPlayer.getVision().contains(this)) {
			// FOG
			long current = System.currentTimeMillis();
			Motion t = Motion.getInstanceOfTextureMotion("FOG", this, current, current + 400);
			Render.addMotion(t);

			current = System.currentTimeMillis();
			t = Motion.getInstanceOfMovableMotion(this, this, current, current + 200);
			t.setMotionType(Motion.MotionType.PRESSED);
			Render.addMotion(t);
			this.setMotion(t);
			return;
		}

		if (this.getOwnerCity() == null)
			return;

		if (!humanPlayer.getVision().contains(this.getOwnerCity().getOwnerTile())) 
			return;
		
		ArrayList<Action> visibleActions = new ArrayList<Action>();
		for (Action action : this.getActions()) {
			if (action.isVisibleTo(Game.getHumanPlayer()))
				visibleActions.add(action);
		}
		if (this.getVariation() != null) {
			for (Action action : this.getVariation().getActions()) {
				if (action.isVisibleTo(Game.getHumanPlayer()))
					visibleActions.add(action);
			}
		}
		Game.window.showActions(visibleActions);

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfMovableMotion(this.getOwnerCity().getOwnerTile(), this.getOwnerCity().getOwnerTile(), current, current + 100);
		Render.addMotion(t);
		this.getOwnerCity().getOwnerTile().setMotion(t);

		// Sword Animation
		/*current = System.currentTimeMillis();
		t = Motion.getInstanceOfTextureMotion("SWORD", this, current, current + 200);
		t.setMotionType(Motion.MotionType.ROTATE);
		Render.addMotion(t);*/

		// Fog Animation
		/*long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", this, current, current + 400);
		Render.addMotion(t);*/

		//Grow Population
		/*long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("POPULATION-" + getOwnerCity().getOwnerPlayer().getFaction().toString(),
		 this, getOwnerCity().getOwnerTile(),  current, current + 500);
		Render.addMotion(t);*/

		//Get star
		/*long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("STAR", this,  current, current + 5000);
		Render.addMotion(t);*/
	}

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

}


