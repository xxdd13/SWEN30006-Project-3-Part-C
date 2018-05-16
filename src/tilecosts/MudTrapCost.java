package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class MudTrapCost implements ITileCost{

	public MudTrapCost() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getCost(Node node, HashMap<Coordinate, MapTile> map) {
		// TODO Auto-generated method stub
		return 5;
	}

}
