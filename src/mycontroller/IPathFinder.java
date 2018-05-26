package mycontroller;

import java.util.HashMap;
import java.util.List;

import tiles.MapTile;
import utilities.Coordinate;

public interface IPathFinder {
	List<Coordinate> getShortestPath(Coordinate start, List<Coordinate> targets, HashMap<Coordinate, MapTile> map);
}