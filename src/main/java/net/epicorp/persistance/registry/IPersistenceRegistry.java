package net.epicorp.persistance.registry;

import net.epicorp.persistance.Persistent;

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
}
