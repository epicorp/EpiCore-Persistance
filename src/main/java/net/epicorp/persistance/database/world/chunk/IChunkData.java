package net.epicorp.persistance.database.world.chunk;

import net.epicorp.persistance.Persistent;
import org.bukkit.Location;
import java.util.function.BiConsumer;

public interface IChunkData {
	/**
	 * apply the data at the relative positions, or null if none is found
	 * @param x 0-15
	 * @param y 0-256
	 * @param z 0-15
	 * @param <T>
	 * @return
	 */
	<T extends Persistent> T getData(int x, int y, int z);

	/**
	 * Sets the data to the given location
	 * @param data the data
	 * @param x 0-15
	 * @param y 0-256
	 * @param z 0-15
	 * @param <T> the datatype entered
	 */
	<T extends Persistent> void setData(T data, int x, int y, int z);

	/**
	 * iterate through all the stored data in the database
	 * @param data consumer
	 */
	void forEach(BiConsumer<Location, Persistent> data);

	/**
	 * save the current chunk to a persistent location
	 * @param remove if the chunk should use the {@link Persistent#close()} before serializing it
	 */
	void save(boolean remove);
}
