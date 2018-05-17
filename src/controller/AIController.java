package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mycontroller.DijkstraPathFinder;
import mycontroller.LavaNavigation;
import mycontroller.Navigation;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class AIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	
	
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	//private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	/////////////////////
	private boolean isGoingBackward = false;
	private boolean afterReversing = false;
	private boolean isFollowingCoordinate = false;
	private final float FINAL_CAR_SPEED =(float) 3;
	private float CAR_SPEED = FINAL_CAR_SPEED;
	private float BREAK_THRESHOLD = (float) 0.03;
	private float CAR_SPEED_THRESHOLD1 = (float) 1;
	private float CAR_SPEED_THRESHOLD2 = (float) 1.1;
	private float CHANGE_AHEAD_SPEED = (float) 1.0;
	private int totalKeys;
	private int currentKey;
	Navigation navigation;
	List<Coordinate> route;
	private List<Coordinate> visited = new ArrayList<>();
	/////////////////////
	
	public Coordinate[] keyList = new Coordinate[getKey()-1];
	
	public AIController(Car car) {
		super(car);
		navigation = new LavaNavigation(getMap(), new DijkstraPathFinder(),visited);
		totalKeys = getKey();
	}
	
	Coordinate initialGuess;
	boolean notSouth = true;
	
	private boolean hasAllKeys() {
		for (int i=0;i<(keyList.length-1);i++) {
			if (keyList[i]==null) return false;
		}
		return true;
	}
	
	private void viewGetKey(HashMap<Coordinate, MapTile> view) {
		view.forEach((key,val) -> {

			if (val instanceof LavaTrap) {
				int keyNum =((LavaTrap) val).getKey();
				if( keyNum>0 && keyList[keyNum-1]==null) {
					keyList[keyNum-1]=key;
					System.out.println("found key: "+keyNum+" at "+key);
				} 
			}
		});
	}
	
	@Override
	public void update(float delta) {
		//System.out.println(keyList.length);
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		Coordinate currentCoordinate = new Coordinate(this.getPosition());
		MapTile currentTile = currentView.get(currentCoordinate);
		
		//testOnly , jump straight to pathfind
		keyList[0] = new Coordinate("23,15");
		keyList[1] = new Coordinate("16,13");
		keyList[2] = new Coordinate("19,2");
		
		
		
		if(hasAllKeys()) {  //ALL keys have been found
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			//plan new route
			
			if(route==null||route.size()<=0) { //current route finish
				System.out.println("current key: "+(getKey()-1));
				route = navigation.planRoute(currentCoordinate, keyList[getKey()-2]);			
			}else {
				if(currentCoordinate.equals(route.get(0))) {
					route.remove(0);
				}
				
			}
			checkStateChange();
			
			/*check if car is following current route*/
			if(!isFollowingCoordinate) {
				
				
				/*if car is not going back then speed up until car speed limit*/
				if(getSpeed() < CAR_SPEED){
					applyForwardAcceleration();
				}
				
				/*condition checking which turn should apply based on car orientation*/
				if(checkNorth(currentCoordinate)){
					//System.out.println("checking north");
					if(getOrientation().equals(WorldSpatial.Direction.EAST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.WEST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						
						if(checkEast(currentCoordinate)){
							lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
							applyLeftTurn(getOrientation(),delta);
						}else if(checkWest(currentCoordinate)) {
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							applyRightTurn(getOrientation(),delta);	
						}
						applyForwardAcceleration();
						System.out.println("north south");
						
					}
					
					
					else{
						isFollowingCoordinate = true;
					}
				}
				else if(checkSouth(currentCoordinate)) {
					
					
					
					if(getOrientation().equals(WorldSpatial.Direction.WEST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
						System.out.println("U turn ! south to north");
						
						//check whether to turn left or right
						if(checkEast(currentView)) {
							System.out.println("choose turn right");
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x+1,currentCoordinate.y);
							route = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
							System.out.println("new route: "+route);
						}
						else if(checkWest(currentView)) {
							System.out.println("choose turn left");
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x-1,currentCoordinate.y);
							route = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
							System.out.println("new route: "+route);
							
						}
						
					}
					
					else{
						isFollowingCoordinate = true;
					}//as
				}
				else if(checkEast(currentCoordinate)) {
					//System.out.println("checking east");
					
					if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.WEST)){
						
						System.out.println("east west");
						
						if(checkSouth(currentCoordinate)){
							lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
							applyLeftTurn(getOrientation(),delta);
						}else if(checkNorth(currentCoordinate)) {
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							applyRightTurn(getOrientation(),delta);	
						}
						applyForwardAcceleration();
						
					}
					
					else{
						isFollowingCoordinate = true;
					}
				}
				else if(checkWest(currentCoordinate)) {
					//System.out.println("checking west 		current direction:"+getOrientation());
					
					if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
						
						if(checkNorth(currentCoordinate)){
							System.out.println("west east n");
							lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
							applyLeftTurn(getOrientation(),delta);
						}else if(checkSouth(currentCoordinate)) {
							System.out.println("west east s");
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							applyRightTurn(getOrientation(),delta);	
						}
						applyForwardAcceleration();
					}
					
					else{
						isFollowingCoordinate = true;
					}
				}
			
			}
			
			/*car is following the route planned
			 * check if car is turning right or left or going backward direction
			 * route speed limit is set here
			 */
			else {
				readjust(lastTurnDirection,delta);
				if(isTurningRight){
					applyRightTurn(getOrientation(),delta);
				}
				else if(isTurningLeft){
					applyLeftTurn(getOrientation(),delta);
				}
				
				/* car is moving forward*/

				else if(checkFollowingCoordinate(getOrientation(),currentCoordinate)){
					afterReversing = false;
					/*check if next 3 coordinate in route, if there is change in front then slow down*/
					if(CheckTurningAhead(getOrientation(),currentCoordinate,currentView, delta)) {
						
						CAR_SPEED = CAR_SPEED_THRESHOLD1;
						if(getSpeed() > CAR_SPEED_THRESHOLD1) {
							applyBrake();
						}
						else if(CAR_SPEED < CAR_SPEED_THRESHOLD2){
							applyForwardAcceleration();
							
						}
					}
					/*if there is no turn ahead, remain original car speed*/
					if(!CheckTurningAhead(getOrientation(),currentCoordinate,currentView, delta) && getSpeed() < CAR_SPEED){
						applyForwardAcceleration();
						CAR_SPEED = FINAL_CAR_SPEED ;
					}
					/*if trap is under car foot*/
					if(currentView.get(currentCoordinate) instanceof TrapTile) {
						if(((TrapTile)currentView.get(currentCoordinate)).canAccelerate()	 ) {
								//not turning
							//applyForwardAcceleration();
					
						}
					}
				}
				/*if not following coordinate,switch to top function*/
				else if(!checkFollowingCoordinate(getOrientation(),currentCoordinate)){
					isFollowingCoordinate = false;
					CAR_SPEED = CHANGE_AHEAD_SPEED;
				}
			
			}
			
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		}else {//missing keys, keep searching
			viewGetKey(currentView); 
			
			checkStateChange();

			// If you are not following a wall initially, find a wall to stick to!
			if(!isFollowingWall){
				if(getSpeed() < CAR_SPEED){
					applyForwardAcceleration();
				}
				// Turn towards the north
				if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				if(checkNorth(currentView)){
					// Turn right until we go back to east!
					if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
					}
					else{
						isFollowingWall = true;
					}
				}
			}
			// Once the car is already stuck to a wall, apply the following logic
			else{
				
				// Readjust the car if it is misaligned.
				readjust(lastTurnDirection,delta);
				
				if(isTurningRight){
					applyRightTurn(getOrientation(),delta);
				}
				else if(isTurningLeft){
					// Apply the left turn if you are not currently near a wall.
					if(!checkFollowingWall(getOrientation(),currentView)){
						applyLeftTurn(getOrientation(),delta);
					}
					else{
						isTurningLeft = false;
					}
				}
				// Try to determine whether or not the car is next to a wall.
				else if(checkFollowingWall(getOrientation(),currentView)){
					// Maintain some velocity
					if(getSpeed() < CAR_SPEED){
						applyForwardAcceleration();
					}
					// If there is wall ahead, turn right!
					if(checkWallAhead(getOrientation(),currentView)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						isTurningRight = true;				
						
					}

				}
				// This indicates that I can do a left turn if I am not turning right
				else{
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					isTurningLeft = true;
				}
			}
		}
		
		
		
		
		

	}
	
	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		if(lastTurnDirection != null){
			if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
				adjustRight(getOrientation(),delta);
			}
			else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
				adjustLeft(getOrientation(),delta);
			}
		}
		
	}
	
	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(getAngle() > WorldSpatial.NORTH_DEGREE){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE){
				turnRight(delta);
			}
			break;
		case WEST:
			if(getAngle() > WorldSpatial.WEST_DEGREE){
				turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(getAngle() < WorldSpatial.NORTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(getAngle() < WorldSpatial.SOUTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(getAngle() < WorldSpatial.WEST_DEGREE){
				turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = getOrientation();
		}
		else{
			if(previousState != getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = getOrientation();
			}
		}
	}
	
	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnLeft(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnRight(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnRight(delta);
			}
			break;
		default:
			break;
		
		}
		
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
		default:
			return false;
		
		}
	}
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
		default:
			return false;
		}
		
	}
	

	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	




/////////////////////////////////////////////



	/*check if turning is ahead, if yes then slow down the car speed*/
	private boolean CheckTurningAhead(WorldSpatial.Direction orientation, Coordinate currentCoordinate,HashMap<Coordinate, MapTile> currentView ,float delta) {
		boolean flag = false;
		Coordinate ahead1;
		Coordinate ahead2;
		Coordinate ahead3;
		switch(orientation){
		case EAST:
			ahead1 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
			ahead2 = new Coordinate(currentCoordinate.x+2, currentCoordinate.y);
			ahead3 = new Coordinate(currentCoordinate.x+3, currentCoordinate.y);
			if(route.size() > 3) {
				if(!ahead1.equals(route.get(0)) || !ahead2.equals(route.get(1)) || !ahead3.equals(route.get(2))){
					flag = true;
				}
			}
			break;
			
	
		case NORTH:
			ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
			ahead2 = new Coordinate(currentCoordinate.x, currentCoordinate.y+2);
			ahead3 = new Coordinate(currentCoordinate.x, currentCoordinate.y+3);
			if(route.size() > 3) {
				if(!ahead1.equals(route.get(0)) || !ahead2.equals(route.get(1)) || !ahead3.equals(route.get(2))){
					flag = true;
				}
			}
			break;
		case SOUTH:
			ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
			ahead2 = new Coordinate(currentCoordinate.x, currentCoordinate.y-2);
			ahead3 = new Coordinate(currentCoordinate.x, currentCoordinate.y-3);
			if(route.size() > 3) {
				if(!ahead1.equals(route.get(0)) || !ahead2.equals(route.get(1)) || !ahead3.equals(route.get(2))){
					flag = true;
				}
			}
			break;
	
		case WEST:
			ahead1 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
			ahead2 = new Coordinate(currentCoordinate.x-2, currentCoordinate.y);
			ahead3 = new Coordinate(currentCoordinate.x-3, currentCoordinate.y);
			if(route.size() > 3) {
				if(!ahead1.equals(route.get(0)) || !ahead2.equals(route.get(1)) || !ahead3.equals(route.get(2))){
					flag = true;
				}
			}
			break;
		default:
			break;
		}
	
		return flag;
		
	}
	
	/**
	 * Check if the car is following next coordinate
	 * @param orientation
	 * @param currentCoordinate 
	 * @return
	 */
	private boolean checkFollowingCoordinate(WorldSpatial.Direction orientation, Coordinate currentCoordinate) {
		
		switch(orientation){
		case EAST:
			return checkEast(currentCoordinate);
		case NORTH:
			return checkNorth(currentCoordinate);
		case SOUTH:
			return checkSouth(currentCoordinate);
		case WEST:
			return checkWest(currentCoordinate);
		default:
			return false;
		}
		
	}
	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * checkEast will check up to next coordinate to the right.
	 * checkWest will check up to next coordinate to the left.
	 * checkNorth will check up to next coordinate to the top.
	 * checkSouth will check up to next coordinate below.
	 */
	public boolean checkEast(Coordinate currentCoordinate){
		// Check tiles to my right
		Coordinate next = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
		if(!route.isEmpty() && route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkWest(Coordinate currentCoordinate){
		// Check tiles to my left
		Coordinate next = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
		if(!route.isEmpty() && route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkNorth(Coordinate currentCoordinate){
		// Check tiles to towards the top
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
		if(!route.isEmpty() && route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkSouth(Coordinate currentCoordinate){
		// Check tiles towards the bottom
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
		if(!route.isEmpty() && route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	
	}
	/*same function as checkFollowingCoordinate, but in reverse direction*/
	private boolean checkReverseFollowingCoordinate(WorldSpatial.Direction orientation, Coordinate currentCoordinate) {
		
		switch(orientation){
		case EAST:
			return checkWest(currentCoordinate);
		case NORTH:
			return checkSouth(currentCoordinate);
		case SOUTH:
			return checkNorth(currentCoordinate);
		case WEST:
			return checkEast(currentCoordinate);
		default:
			return false;
		}
	}

}