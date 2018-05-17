package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class HealthTileCost implements ITileCost {
	
	public HealthTileCost() {
	}

	@Override
	public double getCost(Node node, HashMap<Coordinate, MapTile> map) {
		if (!map.get(node.coordinate).isType(MapTile.Type.WALL)) {
			return 1;
		}
		return Double.POSITIVE_INFINITY;
	}

}
