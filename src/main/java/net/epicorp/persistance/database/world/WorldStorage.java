package net.epicorp.persistance.database.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.epicorp.persistance.Persistent;
import net.epicorp.persistance.database.world.chunk.ChunkData;
import net.epicorp.persistance.database.world.chunk.IChunkData;
import net.epicorp.persistance.registry.IPersistenceRegistry;
import net.epicorp.utilities.objects.If;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import java.awt.Point;
import java.io.File;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class WorldStorage implements IWorldStorage {
	protected final UUID worldID;
	protected File parent;
	protected Function<Point, IChunkData> newChunk;
	protected final IPersistenceRegistry registry;
	protected int cacheSize = 128;

	protected Long2ObjectMap<IChunkData> worldData;

	protected Long2ObjectLinkedOpenHashMap<IChunkData> cache = new Long2ObjectLinkedOpenHashMap<>();

	public WorldStorage(IPersistenceRegistry registry, World world, Long2ObjectMap<IChunkData> worldData, Function<Point, IChunkData> newChunk) {
		this.worldID = world.getUID();
		this.worldData = worldData;
		this.newChunk = newChunk;
		this.registry = registry;
	}

	public WorldStorage(IPersistenceRegistry registry, String name, Plugin plugin, World world) {
		this(registry, world, new Long2ObjectOpenHashMap<>(), null); // assigned after

		this.parent = new File(plugin.getDataFolder(), name+"_"+world.getName()); // parent file for default impl
		this.newChunk = (p) -> new ChunkData(world, registry, p, this.parent); // default impl needs parent file
	}
	@Override
	public void loadChunk(int x, int z) {
		this.getChunkData(x, z); // autoloads chunks as well
	}

	@Override
	public void unloadChunk(int x, int z) {
		long key = this.getKey(x, z);
		IChunkData data = this.worldData.remove(key);
		if(data != null) this.cache.put(key, data);
		while (this.cache.size() > this.cacheSize)
			If.nonNull(this.cache.removeLast(), c -> c.save(true));
	}

	@Override
	public void forEach(BiConsumer<Location, Persistent> data) {
		this.worldData.forEach((l, c) -> If.nonNull(c, () -> c.forEach(data)));
		this.cache.forEach((l, c) -> If.nonNull(c, () -> c.forEach(data)));
	}

	@Override
	public void saveAll(boolean close) {
		this.worldData.forEach((l, c) -> If.nonNull(c, () -> c.save(close)));
		this.cache.forEach((l, c) -> If.nonNull(c, () -> c.save(close)));
	}

	@Override
	public void clear() {
		this.forEach((l, w) -> w.close());
		this.worldData.clear();
		this.cache.clear();
		if(this.parent != null) this.parent.delete();
	}


	@Override
	public <T extends Persistent> T getData(int x, int y, int z) {
		IChunkData chunkData = this.getChunkDataBlock(x, z);
		return chunkData.getData(x & 15, y, z & 15);
	}

	@Override
	public <T extends Persistent> void setData(T data, int x, int y, int z) {
		IChunkData chunkData = this.getChunkDataBlock(x, z);
		chunkData.setData(data, x & 15, y, z & 15);
	}

	@Override
	public <T extends Persistent> T removeData(int x, int y, int z) {
		return this.getChunkDataBlock(x, z).removeData(x & 15, y, z & 15);
	}

	/**
	 * gets the chunk data for the given block
	 * @param x x coordinate of the block
	 * @param z z coordinate of the block
	 * @return
	 */
	protected IChunkData getChunkDataBlock(int x, int z) {
		Point chunk = this.getChunk(x, z);
		return this.getChunkData(chunk.x, chunk.y);
	}

	/**
	 * gets the chunk data for the given chunk
	 * @param x the x coordinate of the chunk
	 * @param z the z coordinate of the chunk
	 * @return
	 */
	protected IChunkData getChunkData(int x, int z) {
		long key = this.getKey(x, z);
		if(this.worldData.containsKey(key))
			return this.worldData.get(key);
		else if(this.cache.containsKey(key)) {
			IChunkData data;
			this.worldData.put(key, data = this.cache.remove(key));
			return data;
		} else {
			IChunkData data;
			this.worldData.put(key, data = this.newChunk.apply(new Point(x, z)));
			return data;
		}
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
		return this.cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
}
