package net.epicorp.persistance.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.epicorp.persistance.Persistent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PersistenceRegistry implements IRegisterableRegistry {

	private BiMap<Class<? extends Persistent>, Integer> ids = HashBiMap.create();
	private Map<Class<? extends Persistent>, Supplier<Persistent>> instantiators = new HashMap<>();

	protected final File file;



	public PersistenceRegistry(File file) {
		this.file = file;

		if (file != null) {
			if(file.exists()) {
				try {
					YamlConfiguration configuration = new YamlConfiguration();
					configuration.load(file);
					configuration.getKeys(false).forEach(s -> {
						try {
							this.ids.put((Class<? extends Persistent>) Class.forName(s.replace('!', '.')), configuration.getInt(s));
						} catch (ClassNotFoundException e) {
							throw new RuntimeException("A class was not found", e);
						}
					});
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
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
	@Override
	public void save() throws IOException {
		YamlConfiguration configuration = new YamlConfiguration();
		this.ids.forEach((c, i) -> configuration.set(c.getName().replace('.', '!'), i));
		configuration.save(this.file);
	}

	@Override
	public void iterate(Consumer<Integer> idIterator) {
		this.ids.forEach((c, i) -> idIterator.accept(i));
	}

	@Override
	public void register(Class<? extends Persistent> _class, Supplier<Persistent> instantiation) {
		if(!this.ids.containsKey(_class)) this.ids.put(_class, this.firstOpen());
		this.instantiators.put(_class, instantiation);
	}

	/**
	 * returns the first unused value in the ids
	 *
	 * @return
	 */
	private int firstOpen() {
		Object[] set = this.ids.values().toArray();
		for (int i = 0; i < set.length; i++)
			if (((Integer) set[i]) != i) return i;
		return set.length;
	}

	@Override
	public int getIntegerKey(Persistent persistant) {
		return this.ids.get(persistant.getClass());
	}

	@Override
	public Persistent newInstance(int id) {
		return this.instantiators.getOrDefault(this.ids.inverse().get(id), () -> null).get();
	}

	@Override
	public <T extends Persistent> T newInstance(Class<T> _class) {
		return (T) this.instantiators.get(_class).get();
	}


}
