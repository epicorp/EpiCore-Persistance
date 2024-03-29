package net.epicorp.persistance.database;

import net.epicorp.persistance.Persistent;
import net.epicorp.persistance.database.world.IWorldStorage;
import net.epicorp.persistance.database.world.WorldStorage;
import net.epicorp.persistance.registry.IPersistenceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * register as spigot listener ree
 */
public class BlockDatabase implements IBlockDatabase {
	protected Map<UUID, IWorldStorage> serverData;
	protected BiFunction<IPersistenceRegistry, World, IWorldStorage> newWorldStorage;
	protected IPersistenceRegistry persistenceRegistry;

	public BlockDatabase(IPersistenceRegistry persistenceRegistry, Map<UUID, IWorldStorage> serverData, BiFunction<IPersistenceRegistry, World, IWorldStorage> newWorldStorage) {
		this.serverData = serverData;
		this.newWorldStorage = newWorldStorage;
		this.persistenceRegistry = persistenceRegistry;
	}

	public BlockDatabase(IPersistenceRegistry persistenceRegistry, BiFunction<IPersistenceRegistry, World, IWorldStorage> newWorldStorage) {
		this(persistenceRegistry, new HashMap<>(), newWorldStorage);
	}

	public BlockDatabase(IPersistenceRegistry persistenceRegistry, String name, Plugin plugin, Map<UUID, IWorldStorage> serverData) {
		this(persistenceRegistry, serverData, (i, w) -> new WorldStorage(i, name, plugin, w));
	}

	public BlockDatabase(IPersistenceRegistry persistenceRegistry, String name, Plugin plugin) {
		this(persistenceRegistry, (i, w) -> new WorldStorage(i, name, plugin, w));
	}

	@Override
	public void save(boolean _final) {
		this.serverData.forEach((u, i) -> i.saveAll(_final));
	}

	@Override
	public void init() {
		Bukkit.getWorlds().forEach(w -> {for (Chunk chunk : w.getLoadedChunks()) this.chunkLoad(new ChunkLoadEvent(chunk, false));});
	}

	@Override
	public void clear() {
		this.serverData.forEach((u, w) -> w.clear());
	}

	@Override
	public <T extends Persistent> T getData(World world, int x, int y, int z) {
		return this.getWorld(world).getData(x, y, z);
	}

	@Override
	public <T extends Persistent> void setData(T data, World world, int x, int y, int z) {
		this.getWorld(world).setData(data, x, y, z);
	}

	@Override
	public <T extends Persistent> T removeData(World world, int x, int y, int z) {
		return this.getWorld(world).removeData(x, y, z);
	}

	@Override
	public void forEach(BiConsumer<Location, Persistent> data) {
		this.serverData.forEach((u, w) -> w.forEach(data));
	}

	protected IWorldStorage getWorld(World world) {
		return this.serverData.computeIfAbsent(world.getUID(), (u) -> this.newWorldStorage.apply(this.persistenceRegistry, world));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void chunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		this.getWorld(event.getWorld()).loadChunk(chunk.getX(), chunk.getZ());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void chunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		this.getWorld(event.getWorld()).unloadChunk(chunk.getX(), chunk.getZ());
	}
}
