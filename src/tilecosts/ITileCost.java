package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public interface ITileCost {

	public double getCost(Node node, HashMap<Coordinate, MapTile> map);
}
