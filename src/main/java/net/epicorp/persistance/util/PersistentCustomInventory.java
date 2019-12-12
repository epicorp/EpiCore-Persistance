package net.epicorp.persistance.util;

import net.epicorp.persistance.Persistent;
import net.epicorp.persistance.registry.IPersistenceRegistry;
import net.epicorp.utilities.inventories.CustomInventory;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.*;

public class PersistentCustomInventory extends CustomInventory implements Persistent {
	@Override
	public void load(DataInputStream stream) throws IOException {
		this.name = stream.readUTF();
		this.stackSize = stream.readInt();

		ItemStack[] stacks = new ItemStack[stream.readInt()];

		BukkitObjectInputStream bois = new BukkitObjectInputStream(stream);
		for (int i = 0; i < stacks.length; i++) {
			try {
				stacks[i] = (ItemStack) bois.readObject();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		this.contents = stacks;
	}

	@Override
	public void writeTo(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.name);
		stream.writeInt(this.stackSize);
		stream.writeInt(this.contents.length);
		BukkitObjectOutputStream boos = new BukkitObjectOutputStream(stream);
		for (ItemStack content : contents)
			boos.writeObject(content);
	}

	@Override
	public void close() {

	}


}
