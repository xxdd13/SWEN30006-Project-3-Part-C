package tilecosts;

import java.util.HashMap;

import tiles.*;

public class TileCostPool {

	private static TileCostPool instance = new TileCostPool();
	private HashMap<Class<?>, ITileCost> pool;
	
	public TileCostPool() {
		initiliazePool();
	}
	
	public void initiliazePool() {
		pool = new HashMap<>();
		pool.put((new MapTile(MapTile.Type.ROAD)).getClass(), new MapTileCost());
		pool.put((new MudTrap()).getClass(), new MudTrapCost());
		pool.put((new LavaTrap()).getClass(), new LavaTrapCost());
		pool.put((new GrassTrap()).getClass(), new GrassTrapCost());
		pool.put((new HealthTrap()).getClass(), new HealthTileCost());
	}
	
	public static TileCostPool getInstance() {
		return instance;
	}
	
	public ITileCost getTileCost(MapTile tile) {
		return pool.get(tile.getClass());
	}

}
