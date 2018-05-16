package tilecosts;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public class GrassTrapCost implements ITileCost {

	public GrassTrapCost() {
		// TODO Auto-generated constructor stub
	}

	@Override
/** returns the cost of the tile straight ahead **/
	public double getCost(Node node, HashMap<Coordinate, MapTile> map) {
		int next_x = 2*node.coordinate.x - node.parent.coordinate.x;
		int next_y = 2*node.coordinate.y - node.parent.coordinate.y;
		Coordinate nextCoord = new Coordinate(next_x, next_y);
		Node nextNode = new Node(node, nextCoord);
		MapTile tile = map.get(nextNode.coordinate);
		ITileCost tileCost = TileCostPool.getInstance().getTileCost(tile);
		double cost = tileCost.getCost(nextNode, map);
		return cost;
	}

}
