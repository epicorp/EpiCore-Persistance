package net.epicorp.persistance.util;

import net.epicorp.persistance.Persistent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BukkitPersistents {
	private static final NamespacedKey CRINGE = new NamespacedKey("cringe_ass_nae_nae_maybe", "i_am_lazy_depreciate_my_ass_lmao");
	public static void write(Persistent persistent, ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		try {
			container.set(CRINGE, PersistentDataType.BYTE_ARRAY, persistent.saveAsByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		stack.setItemMeta(meta);
	}

	public static final byte[] EMPTY = new byte[1024];
	public static void read(Persistent init, ItemStack stack) {
		try {
			init.load(new DataInputStream(new ByteArrayInputStream(stack.getItemMeta().getPersistentDataContainer().getOrDefault(CRINGE, PersistentDataType.BYTE_ARRAY, EMPTY))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void write(Persistent persistent, Entity entity) {
		PersistentDataContainer container = entity.getPersistentDataContainer();
		try {
			container.set(CRINGE, PersistentDataType.BYTE_ARRAY, persistent.saveAsByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static void read(Persistent init, Entity entity) {
		try {
			init.load(new DataInputStream(new ByteArrayInputStream(entity.getPersistentDataContainer().get(CRINGE, PersistentDataType.BYTE_ARRAY))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
