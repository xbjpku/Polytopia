package polytopia.gameplay;

import polytopia.graphics.Visualizable;

public class Unit implements Visualizable {

	private Player ownerPlayer;
	private City ownerCity;

	// Selection response
	public void visualize(/* GUI component */) {
		System.out.println("Unit selected");
	}

	public Player getOwnerPlayer() {return this.ownerPlayer;}
	public City getOwnerCity() {return this.ownerCity;}

}