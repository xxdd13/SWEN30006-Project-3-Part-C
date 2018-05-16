package mycontroller;

import java.util.HashMap;
import java.util.List;

import tiles.MapTile;
import utilities.Coordinate;

public interface IPathFinder {

	public List<Coordinate> planRoute(Coordinate start, List<Coordinate> finish, HashMap<Coordinate, MapTile> map);
	
}
