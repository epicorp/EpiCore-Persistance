package net.epicorp.persistance.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.epicorp.persistance.Persistent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PersistenceRegistry implements IPersistenceRegistry {

	private BiMap<Class<? extends Persistent>, Integer> ids = HashBiMap.create();
	private Map<Class<? extends Persistent>, Supplier<Persistent>> instantiators = new HashMap<>();

	protected final File file;



	public PersistenceRegistry(File file) throws IOException, InvalidConfigurationException {
		this.file = file;
		if (file != null) {
			if(file.exists()) {
				YamlConfiguration configuration = new YamlConfiguration();
				configuration.load(file);
				configuration.getKeys(false).forEach(s -> {
					try {
						ids.put((Class<? extends Persistent>) Class.forName(s), configuration.getInt(s));
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("A class was not found", e);
					}
				});
			} else
				System.out.println("Persistence Registry was initialized with empty file, this should ideally only happen the first time you launch the server");
		} else
			throw new IllegalArgumentException("File is null");

	}

	/**
	 * saves the registry to a file
	 *
	 * @throws IOException
	 */
	public void save() throws IOException {
		YamlConfiguration configuration = new YamlConfiguration();
		ids.forEach((c, i) -> configuration.set(c.getName(), i));
		configuration.save(file);
	}

	/**
	 * register a persistent class with a custom instantiation function
	 *
	 * @param _class
	 * @param instantiation
	 */
	public void register(Class<? extends Persistent> _class, Supplier<Persistent> instantiation) {
		ids.forcePut(_class, firstOpen());
		instantiators.put(_class, instantiation);
	}

	/**
	 * register a persistent class with the default instantiation function
	 *
	 * @param _class
	 */
	public void register(Class<? extends Persistent> _class) {
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

	/**
	 * returns the first unused value in the ids
	 *
	 * @return
	 */
	private int firstOpen() {
		Object[] set = ids.values().toArray();
		for (int i = 0; i < set.length; i++)
			if (((Integer) set[i]) != i) return i;
		return set.length;
	}

	@Override
	public int getIntegerKey(Persistent persistant) {
		return ids.get(persistant.getClass());
	}

	@Override
	public Persistent newInstance(int id) {
		return instantiators.get(ids.inverse().get(id)).get();
	}

	@Override
	public <T extends Persistent> T newInstance(Class<T> _class) {
		return (T) instantiators.get(_class).get();
	}


}
