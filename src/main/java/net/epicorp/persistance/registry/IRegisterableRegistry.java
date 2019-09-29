package net.epicorp.persistance.registry;

import net.epicorp.persistance.Persistent;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public interface IRegisterableRegistry extends IPersistenceRegistry {
	/**
	 * register a persistent class with a custom instantiation function
	 *
	 * @param _class
	 * @param instantiation
	 */
	void register(Class<? extends Persistent> _class, Supplier<Persistent> instantiation);

	/**
	 * register a persistent class with the default instantiation function
	 *
	 * @param _class
	 */
	default void register(Class<? extends Persistent> _class) {
		try {
			Constructor<Persistent> constructor = (Constructor<Persistent>) _class.getConstructor();
			constructor.setAccessible(true);
			register(_class, () -> {
				try {
					return constructor.newInstance();
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
