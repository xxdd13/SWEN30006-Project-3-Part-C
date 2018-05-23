package mycontroller;


import java.util.HashMap;

import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

public class HealthPathFinder extends DijkstraPathFinder {

	/**
	 * when finding path to heal, avoid lava
	 */
	public void setWeight(Node node, HashMap<Coordinate, MapTile> map) {
		MapTile tile = map.get(node.coord);
		if (tile instanceof LavaTrap) {
			node.setWeight( Double.POSITIVE_INFINITY + node.parent.weight);
		}else {
			ITileWeight weights = TileWeightFactory.getInstance().getWeight(tile);
			node.setWeight(weights.getWeight(node, map) + node.parent.weight);
		}
		
	}

	

}
