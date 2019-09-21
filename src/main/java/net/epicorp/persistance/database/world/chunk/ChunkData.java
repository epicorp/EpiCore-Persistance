package net.epicorp.persistance.database.world.chunk;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.epicorp.persistance.Persistent;
import net.epicorp.persistance.exceptions.FileCorruptedException;
import net.epicorp.persistance.registry.IPersistenceRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import java.awt.Point;
import java.io.*;
import java.util.function.BiConsumer;

/**
 * chunk file format
 * int x // x coor of chunk
 * int y // y coor of chunk
 *
 * int len // number of entries
 *  [ // array of entries
 *      {
 *          short key
 *          persistent data
 *      }
 *  ]
 */
public class ChunkData implements IChunkData {
	protected final File file;
	protected final Short2ObjectMap<Persistent> data;
	protected final IPersistenceRegistry registry;
	protected final Point point;
	protected final World world;

	public ChunkData(World world, IPersistenceRegistry registry, Point point, File parent, Short2ObjectMap<Persistent> data) {
		this.registry = registry;
		this.file = new File(parent, String.format("[%d, %d].data", point.x, point.y));
		this.data = data;
		this.point = point;
		this.world = world;

		if(file.exists()) {
			try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
				if(dis.readInt() != point.x || dis.readInt() != point.y)
					throw new FileCorruptedException("Chunk does not belong to the correct point!");
				int len = dis.readInt();

				for(int x = 0; x < len; x++) {
					short key = dis.readShort();
					Persistent persistent = registry.newInstance(dis.readInt());
					if(persistent == null)
						throw new FileCorruptedException("Persistent with unknown id found in file, likely the result of removing a plugin that relies on EpiCore, or unregistering an object during runtime");
					persistent.load(dis);
					data.put(key, persistent);
				}
			} catch (IOException e) {
				throw new FileCorruptedException(e);
			}
		}
	}

	public ChunkData(World world, IPersistenceRegistry registry, Point point, File parent) {
		this(world, registry, point, parent, new Short2ObjectOpenHashMap<>());
	}

	@Override
	public <T extends Persistent> T getData(int x, int y, int z) {
		return (T) data.get(convert(x, y, z));
	}

	@Override
	public <T extends Persistent> void setData(T data, int x, int y, int z) {
		Persistent former = this.data.put(convert(x, y, z), data);
		if(former != null) former.close(); // release listeners, update lists, etc.
	}

	@Override
	public void forEach(BiConsumer<Location, Persistent> consumer) {
		data.forEach((s, t) -> consumer.accept(getLocation(s, world, point.x, point.y), t));
	}

	@Override
	public void save(boolean write) {
		if(file.exists()) { // if data is gon :crab:
			if(data.size() == 0 && !file.delete())
				throw new FileCorruptedException("Chunk file was unable to be deleted!");
			else {
				try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
					dos.writeInt(point.x); // x and y of chunk
					dos.writeInt(point.y);

					dos.writeInt(data.size()); // size of data

					for(Persistent entry : data.values()) { // data in file
						if(write) entry.close(); // clean entities
						dos.writeInt(registry.getIntegerKey(entry));
						entry.writeTo(dos);
					}
				} catch (IOException e) {
					throw new FileCorruptedException("Chunk file was unable to be opened : " + file);
				}
			}
		}
	}

	@Override
	public <T extends Persistent> T removeData(int x, int y, int z) {
		Persistent persistent = data.remove(convert(x, y, z));
		persistent.close();
		return (T) persistent;
	}

	/**
	 * Converts a short that represents a relative area in a chunk to a location
	 *
	 * @param convert the chunk coordinate
	 * @param world the world in which this point resides
	 * @param cx the chunk's X position
	 * @param cy the chunk's Y position
	 * @return an absolute location
	 */
	protected static Location getLocation(short convert, World world, int cx, int cy) {
		int y = (convert >> 8) & 0xFF;
		byte bottom = (byte) (convert & 0xFF);
		byte x = (byte) (bottom & 0xF);
		byte z = (byte) ((bottom & 0xFF) >> 4);
		return new Location(world, x + cx * 16, y, z + cy * 16);
	}


	/**
	 * converts relative chunk coordinates into a block key
	 * @param x [0-15]
	 * @param y [0-255]
	 * @param z [0-15]
	 * @return
	 */
	protected static short convert(int x, int y, int z) {
		byte xz = 0;
		xz |= x << 4;
		xz |= z;
		return (short) (((((byte) y) & 0xFF) << 8) | (xz & 0xFF));
	}
}
