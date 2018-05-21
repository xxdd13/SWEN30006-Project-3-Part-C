package mycontroller;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public interface ITileWeight {

	public double getWeight(Node node, HashMap<Coordinate, MapTile> map);
}
