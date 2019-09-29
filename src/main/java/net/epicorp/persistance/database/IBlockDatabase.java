package net.epicorp.persistance.database;

import net.epicorp.persistance.Persistent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.function.BiConsumer;

public interface IBlockDatabase extends Listener {

	/**
	 * saves all the data in the world storage
	 * Hint: {@link org.bukkit.event.server.PluginDisableEvent} or {@link JavaPlugin#onDisable()}
	 * @param _final if the server is shutting down, this should be true, if auto-saving, it should be false
	 */
	void save(boolean _final);

	/**
	 * initalizes the block database, such as loading data for already loaded chunks
	 */
	void init();

	/**
	 * removes all data from the database
	 */
	void clear();

	/**
	 * retrieves the data at the given location, or null if none exists, there is no guarantee that the data will or will
	 * not be cloned
	 * @see IBlockDatabase#setData(Persistent, Location)
	 * @param world the world in which this data lies
	 * @param x the x block coordinate
	 * @param y the y block coordinate
	 * @param z the z block coordinate
	 * @param <T> the datatype expected to be at that location
	 * @return the data at the given location
	 */
	<T extends Persistent> T getData(World world, int x, int y, int z);

	/**
	 * Sets the data to the given location
	 * @param data the data
	 * @param world the world in which this object lies
	 * @param x the block x
	 * @param y the block y
	 * @param z the block z
	 * @param <T> the datatype entered
	 */
	<T extends Persistent> void setData(T data, World world, int x, int y, int z);

	/**
	 * removes the data in the given location
	 * @param world the world
	 * @param x the block's x
	 * @param y the block's y
	 * @param z the block's z
	 * @param <T> the type of data expected at that location
	 * @return the data formerly associated with that location
	 */
	<T extends Persistent> T removeData(World world, int x, int y, int z);

	/**
	 * iterate through all the stored data in the database
	 * @param data consumer
	 */
	void forEach(BiConsumer<Location, Persistent> data);

	/**
	 * retrieves the data at the given location, or null if none exists, there is no guarrentee that the data will or will
	 * not be cloned
	 * @see IBlockDatabase#setData(Persistent, Location)
	 * @param location the block's coordinate
	 * @param <T> the datatype expected to be at that location
	 * @return the data at the given location
	 */
	default <T extends Persistent> T getData(Location location) {
		return getData(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * Sets the data to the given location
	 * @param data the data
	 * @param location the block's location
	 * @param <T> the datatype entered
	 */
	default <T extends Persistent> void setData(T data, Location location) {
		setData(data, location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * removes the data at the given location
	 * @param location the block's location
	 * @param <T> the datatype expected at that location
	 * @return
	 */
	default <T extends Persistent> T removeData(Location location) {
		return removeData(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}


}
