package net.epicorp.persistance;

import java.io.*;

/**
 * represents an object that can be read, saved, and written to a stream
 * @author devan s.
 */
public interface Persistent {

	/**
	 * Reads the data from the input stream and initializes the object's contents, this is called after using the class' default constructor
	 * @param input
	 * @throws IOException
	 */
	void load(DataInputStream input) throws IOException;

	/**
	 * serialize the current object to the stream
	 * @param output
	 * @throws IOException
	 */
	void writeTo(DataOutputStream output) throws IOException;

	/**
	 * Called when the persistent data is deleted, or is being saved to the disk, here you should detach listeners, and whatnot
	 */
	void close();

	default byte[] saveAsByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		this.writeTo(new DataOutputStream(baos));
		return baos.toByteArray();
	}

	default Persistent cloneData() {
		try {
			Persistent clone = this.getClass().newInstance();
			clone.load(new DataInputStream(new ByteArrayInputStream(this.saveAsByteArray())));
			return clone;
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
