package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class MapTileWeight implements ITileWeight {
	
	public MapTileWeight() {
	}

	@Override
	public double getWeight(Node node, HashMap<Coordinate, MapTile> map) {
		if (!map.get(node.coordinate).isType(MapTile.Type.WALL)) {
			return 2;
		}
		return Double.POSITIVE_INFINITY;
	}

}
