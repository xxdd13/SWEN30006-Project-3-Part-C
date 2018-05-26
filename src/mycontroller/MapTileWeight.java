package mycontroller;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class MapTileWeight implements ITileWeight {
	
	public MapTileWeight() {
	}

	@Override
	public double getWeight(Node node, HashMap<Coordinate, MapTile> map) {
		if (map.get(node.coord).isType(MapTile.Type.WALL)) {
			return  Double.POSITIVE_INFINITY; //road 
		}else{
			return 1; // wall can not pass
		}
		
	}

}
