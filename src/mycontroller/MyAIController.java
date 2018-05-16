package mycontroller;

import java.util.HashMap;
import java.util.List;

import controller.AIController;
import controller.CarController;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;


public class MyAIController extends CarController{
	Navigation navigation;
	List<Coordinate> route;
	AIController ai;
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false;
	private boolean isGoingBackward = false;
	private boolean afterReversing = false;
	private boolean isFollowingCoordinate = false;
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private float CAR_SPEED = 2;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	private float BREAK_THRESHOLD = (float) 0.03;
	private float CAR_SPEED_THRESHOLD1 = (float) 1;
	private float CAR_SPEED_THRESHOLD2 = (float) 1.1;
	private float CHANGE_AHEAD_SPEED = (float) 1.2;
	
	
	public MyAIController(Car car) {
		super(car);
		ai = new AIController(car);
		navigation = new Navigation(getMap(), new DijkstraPathFinder());
		route = navigation.planRoute(new Coordinate(this.getPosition()));
	}

	@Override
	/* The car updating its movement based on coordinate by coordinate
	 * whenever it meet trap it will replan a new route
	 * whenever the car step on the coordinate on route planned, it will remove the first coordinate from route array
	 * during driving speed is keep changing just like real life
	 * the route speed limit is set based on big map given
	 * given map tile and coordinate must be percise since the car is following coordinate by coordinate
	 * when it step on the trap, it will try to accelerate(if possible)
	 * 
	 * @see controller.CarController#update(float)
	 */
	public void update(float delta) {

		Coordinate currentCoordinate = new Coordinate(this.getPosition());
		HashMap<Coordinate, MapTile> currentView = getView();
		
		//已经踩过的lava就不再作为探索目标
		MapTile currentTile = currentView.get(currentCoordinate);
		if (currentTile instanceof LavaTrap && !navigation.visited.contains(currentCoordinate)) {
			navigation.visited.add(currentCoordinate);
			
			//route = navigation.getRoute();
		}
		
		if(route.size()<=0) {
			checkStateChange();
			route = navigation.planRoute(new Coordinate(this.getPosition()));
			
		}
		else {
			if(currentCoordinate.equals(route.get(0))) {
				route.remove(0);
			}
			if (navigation.updateMap(this.getView(), currentCoordinate)) {
				route = navigation.getRoute();
			}
		}
		checkStateChange();
		
		/*car is going to change direction in next move(seen ahead the next coordinate is not in the same direction)*/
		if(!isFollowingCoordinate) {

			/*if car is not going back then speed up until car speed limit*/
			if(getSpeed() < CAR_SPEED){
				applyForwardAcceleration();
			}
			/*condition checking which turn should apply based on car orientation*/
			if(checkNorth(currentCoordinate)){
				if(getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.WEST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				
				else{
					isFollowingCoordinate = true;
				}
			}
			else if(checkSouth(currentCoordinate)) {
				System.out.println("moving south");
				if(getOrientation().equals(WorldSpatial.Direction.WEST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					System.out.println("northhhh");
				}
				
				else{
					isFollowingCoordinate = false;
				}//as
			}
			else if(checkEast(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				
				else{
					isFollowingCoordinate = true;
				}
			}
			else if(checkWest(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
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
					CAR_SPEED = 1 ;
				}
				/*if trap is beneath car foot*/
				if(currentView.get(currentCoordinate) instanceof TrapTile) {
					if(((TrapTile)currentView.get(currentCoordinate)).canAccelerate()) {
						
						
						applyForwardAcceleration();
					}
				}
			}
			/*if not following coordinate,switch to top function*/
			else if(!checkFollowingCoordinate(getOrientation(),currentCoordinate)){
				isFollowingCoordinate = false;
				CAR_SPEED = CHANGE_AHEAD_SPEED;
			}
		}
	}

	/*Supporting functions down here*/
	
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
