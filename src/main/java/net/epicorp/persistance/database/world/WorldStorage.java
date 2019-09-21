package net.epicorp.persistance.database.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.epicorp.persistance.Persistent;
import net.epicorp.persistance.database.world.chunk.ChunkData;
import net.epicorp.persistance.database.world.chunk.IChunkData;
import net.epicorp.persistance.registry.IPersistenceRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class WorldStorage implements IWorldStorage {
	protected final UUID worldID;
	protected final File parent;
	protected final Function<Point, IChunkData> newChunk;
	protected final IPersistenceRegistry registry;
	protected int cacheSize;

	protected Long2ObjectMap<IChunkData> worldData;

	protected Long2ObjectLinkedOpenHashMap<IChunkData> cache = new Long2ObjectLinkedOpenHashMap<>();

	public WorldStorage(IPersistenceRegistry registry, String name, Plugin plugin, World world, Long2ObjectMap<IChunkData> worldData, Function<Point, IChunkData> chunkData) {
		worldID = world.getUID();
		this.worldData = worldData;
		this.parent = new File(plugin.getDataFolder(), name);
		if(!parent.exists() && !parent.mkdirs())
			throw new RuntimeException(new FileNotFoundException("Bruh, " + parent + " was unable to be created"));
		this.newChunk = chunkData;
		this.registry = registry;
	}

	public WorldStorage(IPersistenceRegistry registry, String name, Plugin plugin, World world) {
		this(registry, name, plugin, world, new Long2ObjectOpenHashMap<>(), (p) -> new ChunkData(world, registry, p, new File(plugin.getDataFolder(), name)));
	}

	public WorldStorage(IPersistenceRegistry registry, String name, Plugin plugin, World world, Function<Point, IChunkData> chunkData) {
		this(registry, name, plugin, world, new Long2ObjectOpenHashMap<>(), chunkData);
	}

	public WorldStorage(IPersistenceRegistry registry, String name, Plugin plugin, World world, Map<Long, IChunkData> worldData) {
		this(registry, name, plugin, world, new Long2ObjectOpenHashMap<>(), (p) -> new ChunkData(world, registry, p, new File(plugin.getDataFolder(), name)));
	}

	@Override
	public void loadChunk(int x, int z) {
		long key = getKey(x, z);
		if(cache.containsKey(key))
			worldData.put(key, cache.remove(key));
		else
			worldData.put(key, getChunkData(x, z));
	}

	@Override
	public void unloadChunk(int x, int z) {
		long key = getKey(x, z);
		IChunkData data = worldData.remove(key);
		if(data != null)
			cache.put(key, data);
		while (cache.size() > cacheSize)
			cache.removeLast();
	}

	@Override
	public void forEach(BiConsumer<Location, Persistent> data) {
		worldData.forEach((l, c) -> c.forEach(data));
	}

	@Override
	public void saveAll(boolean close) {
		worldData.forEach((l, c) -> c.save(close));
		cache.forEach((l, c) -> c.save(close));
	}

	@Override
	public <T extends Persistent> T getData(int x, int y, int z) {
		IChunkData chunkData = getChunkData(x, z);
		return chunkData.getData(x & 15, y, z & 15);
	}

	@Override
	public <T extends Persistent> void setData(T data, int x, int y, int z) {
		IChunkData chunkData = getChunkData(x, z);
		chunkData.setData(data, x & 15, y, z & 15);
	}

	@Override
	public <T extends Persistent> T removeData(int x, int y, int z) {
		return getChunkData(x, z).removeData(x & 15, y, z & 15);
	}

	/**
	 * gets the chunk data for the given block
	 * @param x x coordinate of the block
	 * @param z y coordinate of the block
	 * @return
	 */
	protected IChunkData getChunkData(int x, int z) {
		Point chunk = getChunk(x, z);
		return worldData.computeIfAbsent(getKey(chunk.x, chunk.y), (l) -> newChunk.apply(getPoint(l)));
	}

	/**
	 * apply the chunk that contains a block
	 * @param x the block's X coordinate
	 * @param z the block's Z coordinate
	 * @return
	 */
	protected Point getChunk(int x, int z) {
		return new Point(x >> 4, z >> 4);
	}

	/**
	 * apply the chunk coordinates from a chunk key
	 * @param val the chunk key
	 * @return x and y of the chunk
	 */
	protected Point getPoint(long val) {
		return new Point((int) (val >> 32), (int) val);
	}

	/**
	 * apply the chunk key from the chunk's X and Z coordinates
	 * @param x
	 * @param z
	 * @return
	 */
	protected long getKey(int x, int z) {
		return (((long) x >> 4) << 32) | (z >> 4 & 0xffffffffL);
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
}
