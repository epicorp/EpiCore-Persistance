package net.epicorp.persistance.registry;

import net.epicorp.persistance.Persistent;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * A registry that can assign an id to a persistent's class and retrieve it at a later time, this class is used for serialization of
 * persistents
 */
public interface IPersistenceRegistry {
	/**
	 * gets the integer id of the persistent
	 * @param persistant
	 * @return
	 */
	int getIntegerKey(Persistent persistant);

	/**
	 * creates a new un-initialized object of the class that was assigned that id
	 * @param id
	 * @return
	 */
	Persistent newInstance(int id);

	/**
	 * creates a new instance of the class using the provided initializer
	 * @param _class the class of the object
	 * @param <T>
	 * @return a new persistent instance
	 */
	<T extends Persistent> T newInstance(Class<T> _class);

	/**
	 * save the config file
	 * @throws IOException
	 */
	void save() throws IOException;

	/**
	 * loop through all ids in the registry
	 * @param idIterator
	 */
	void iterate(Consumer<Integer> idIterator);
}
