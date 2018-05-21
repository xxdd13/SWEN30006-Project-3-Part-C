package mycontroller;

import java.util.HashMap;

import tiles.*;

public class TileWeightFactory {

	private static TileWeightFactory instance = new TileWeightFactory();
	private HashMap<Class<?>, ITileWeight> pool;
	
	public TileWeightFactory() {
		initiliazePool();
	}
	
	public void initiliazePool() {
		pool = new HashMap<>();
		pool.put((new MapTile(MapTile.Type.ROAD)).getClass(), new MapTileWeight());
		pool.put((new LavaTrap()).getClass(), new LavaWeight());
		pool.put((new HealthTrap()).getClass(), new HealthWeight());
	}
	
	public static TileWeightFactory getInstance() {
		return instance;
	}
	
	public ITileWeight getWeight(MapTile tile) 
	{
		return pool.get(tile.getClass());
	}

}
