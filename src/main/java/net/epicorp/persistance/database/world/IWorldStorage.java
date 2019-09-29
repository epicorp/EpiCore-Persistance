package net.epicorp.persistance.database.world;

import net.epicorp.persistance.Persistent;
import org.bukkit.Location;
import java.util.function.BiConsumer;

public interface IWorldStorage {
	/**
	 * retrieves the data at the given location, or null if none exists, there is no guarrentee that the data will or will
	 * not be cloned
	 * @see IWorldStorage#setData(Persistent, int, int, int)
	 * @param x the x block coordinate
	 * @param y the y block coordinate
	 * @param z the z block coordinate
	 * @param <T> the datatype expected to be at that location
	 * @return the data at the given location
	 */
	<T extends Persistent> T getData(int x, int y, int z);

	/**
	 * Sets the data to the given location
	 * @param data the data
	 * @param x the block x
	 * @param y the block y
	 * @param z the block z
	 * @param <T> the datatype entered
	 */
	<T extends Persistent> void setData(T data, int x, int y, int z);

	/**
	 * removes the data at that position
	 * @param x the block x
	 * @param y the block y
	 * @param z the block z
	 * @param <T> the expected datatype of the object in that location
	 * @return null or the former data associated with the location
	 */
	<T extends Persistent> T removeData(int x, int y, int z);


	/**
	 * load the chunk from storage
	 * @param x chunk x
	 * @param z chunk z
	 */
	void loadChunk(int x, int z);

	/**
	 * unload the chunk from storage
	 * @param x chunk x
	 * @param z chunk z
	 */
	void unloadChunk(int x, int z);

	/**
	 * iterate through all the stored data in the database
	 * @param data consumer
	 */
	void forEach(BiConsumer<Location, Persistent> data);

	/**
	 * saves all the data in the world storage
	 * @param close true if the server is shutting down (if should call {@link Persistent#close()})
	 */
	void saveAll(boolean close);

	/**
	 * removes all data from the world
	 */
	void clear();
}
