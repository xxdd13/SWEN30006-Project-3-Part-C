package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class HealthWeight implements ITileWeight {
	
	public HealthWeight() {
	}

	@Override
	public double getWeight(Node node, HashMap<Coordinate, MapTile> map) {
		if (!map.get(node.coordinate).isType(MapTile.Type.WALL)) {
			return 1;
		}
		return Double.POSITIVE_INFINITY;
	}

}
