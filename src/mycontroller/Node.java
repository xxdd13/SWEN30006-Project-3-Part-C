package mycontroller;

import java.util.ArrayList;
import java.util.List;
import utilities.Coordinate;
import world.World;

public class Node implements Comparable<Node> {

	public double weight;
	public Node parent;
	public Coordinate coord;
	public boolean visited = false;
	
	public Node(Node parent, Coordinate coord) {
		this.coord = coord;
		this.parent = parent;	
	}
	
	public List<Node> getAdjacentNodes() {
		List<Node >adj = new ArrayList<>();
		int left,right,up,down;
		left = coord.x-1;
		right = coord.x+1;
		up = coord.y+1;
		down=coord.y-1;
		
		//add neighbour nodes
		
		if(left >-1)adj.add(new Node(this, new Coordinate(left, coord.y)));
		if(right <World.MAP_WIDTH)adj.add(new Node(this, new Coordinate(right, coord.y)));
		if(up <World.MAP_HEIGHT)adj.add(new Node(this, new Coordinate(coord.x, up)));
		if(down >-1)adj.add(new Node(this, new Coordinate(coord.x, down)));
		return adj;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	

	@Override
	public int compareTo(Node otherNode) {
		return Double.compare(this.weight, otherNode.weight);
	}

}
