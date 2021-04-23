package bwlodarski.photoMap.models;

/**
 * Photo model, represents a photo from the database.
 */
public class Photo {
	private final byte[] image;
	private final String path;
	private final int id;

	/**
	 * Photo constructor
	 *
	 * @param id    ID of the photo
	 * @param image Photo byte array
	 * @param path  path to the photo stored on the device
	 */
	public Photo(int id, byte[] image, String path) {
		this.id = id;
		this.image = image;
		this.path = path;
	}

	/**
	 * Gets the ID of the photo
	 *
	 * @return photo ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the images photo bytes
	 * @return photo byte array
	 */
	public byte[] getImage() {
		return image;
	}

	/**
	 * Gets the path to the photo
	 * @return photo path
	 */
	public String getPath() {
		return path;
	}
}
