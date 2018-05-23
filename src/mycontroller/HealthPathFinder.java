package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

public class HealthPathFinder extends DijkstraPathFinder {


	public void setWeight(Node node, HashMap<Coordinate, MapTile> map) {
		MapTile tile = map.get(node.coordinate);
		if (tile instanceof LavaTrap) {
			ITileWeight weights = TileWeightFactory.getInstance().getWeight(tile);
			node.setWeight( Double.POSITIVE_INFINITY-1 + node.parent.weight);
		}else {
			ITileWeight weights = TileWeightFactory.getInstance().getWeight(tile);
			node.setWeight(weights.getWeight(node, map) + node.parent.weight);
		}
		
	}

	

}
