package bwlodarski.photoMap.models;

public class Photo {
	private final byte[] image;
	private final String path;
	private final int id;

	public Photo(int id, byte[] image, String path) {
		this.id = id;
		this.image = image;
		this.path = path;
	}

	public int getId() {
		return id;
	}

	public byte[] getImage() {
		return image;
	}

	public String getPath() {
		return path;
	}
}
