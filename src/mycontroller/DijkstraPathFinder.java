package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import tiles.MapTile;
import utilities.Coordinate;

public class DijkstraPathFinder implements IPathFinder {

	HashMap<Coordinate, Node> expanded;
	Queue<Node> frontier;
	
	public DijkstraPathFinder() {
		expanded = new HashMap<>();
		frontier = new PriorityQueue<>();
	}

	@Override
	public List<Coordinate> planRoute(Coordinate start, List<Coordinate> targets, HashMap<Coordinate, MapTile> map) {
		boolean pathFound = false;
		expanded.clear();
		frontier.clear();
		Node current = new Node(null, start);
		current.setWeight(0);
		expanded.put(current.coordinate, current);
		frontier.add(current);
		
		while (!frontier.isEmpty()) {
			current = frontier.remove();
			current.visited = true;
			
			if (targets.contains(current.coordinate)) {
				pathFound = true;
				break;
			}
			// expand current
			current.getChildren().forEach(child -> {
				setWeight(child, map);
				if (child.weight < Double.POSITIVE_INFINITY) {
				if (!expanded.containsKey(child.coordinate)) {
					expanded.put(child.coordinate, child);
					frontier.add(child);
				}
				else if (expanded.get(child.coordinate).visited == false) {
					if (child.weight < expanded.get(child.coordinate).weight) {
						frontier.remove(expanded.get(child.coordinate));
						frontier.add(child);
						expanded.remove(child.coordinate);
						expanded.put(child.coordinate, child);
					}
				}
				}
			});
		}
		
		//get path
		List<Coordinate> path = new ArrayList<>();
		if (!pathFound) {
			return null;
		}
		while (current != null) {
			path.add(0, current.coordinate);
			current = current.parent;
		}
		System.out.println("path: "+ path);
		return path;
	}
	
	public void setWeight(Node node, HashMap<Coordinate, MapTile> map) {
		MapTile tile = map.get(node.coordinate);
		ITileWeight weights = TileWeightFactory.getInstance().getWeight(tile);
		node.setWeight(weights.getWeight(node, map) + node.parent.weight);
	}

}
