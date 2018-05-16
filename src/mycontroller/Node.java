package mycontroller;

import java.util.ArrayList;
import java.util.List;

import utilities.Coordinate;
import world.World;

public class Node implements Comparable<Node> {

	public Coordinate coordinate;
	public Node parent;
	public double cost;
	public boolean traversed = false;
	
	public Node(Node parent, Coordinate coordinate) {
		this.parent = parent;
		this.coordinate = coordinate;
	}
	
	public List<Node> getChildren() {
		List<Node >children = new ArrayList<>();
		for (int i=-1; i<2; i=i+2) {
			if (coordinate.x+i < World.MAP_WIDTH && coordinate.x+i >=0) {
				children.add(new Node(this, new Coordinate(coordinate.x+i, coordinate.y)));
			}
			if (coordinate.y+i < World.MAP_HEIGHT && coordinate.y+i >=0) {
				children.add(new Node(this, new Coordinate(coordinate.x, coordinate.y+i)));
			}
		}
		return children;
	}
	
	public void setCost(double cost) {
		this.cost = cost;
	}
	

	@Override
	public int compareTo(Node o) {
		return Double.compare(this.cost, o.cost);
	}

}
