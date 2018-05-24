package mycontroller;

import java.util.HashMap;

import mycontroller.Node;
import tiles.MapTile;
import utilities.Coordinate;

public interface ITileWeight {
	//using double instead of flora because double's infinity is true inf
	public double getWeight(Node node, HashMap<Coordinate, MapTile> map);
}
