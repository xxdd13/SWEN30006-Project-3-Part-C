package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class LavaTrapCost implements ITileWeight {

	public LavaTrapCost() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getWeight(Node node, HashMap<Coordinate, MapTile> map) {
		// TODO Auto-generated method stub
		return 10;
	}

}
