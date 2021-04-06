package bwlodarski.courseworkapplication.Models;

public class Photo {
	int id;
	String image;

	public Photo(int id, String image) {
		this.id = id;
		this.image = image;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
}
