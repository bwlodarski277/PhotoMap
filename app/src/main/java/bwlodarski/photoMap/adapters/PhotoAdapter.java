package bwlodarski.photoMap.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.activities.PhotoDetailsActivity;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;
import bwlodarski.photoMap.models.Photo;

/**
 * Custom RecyclerView for displaying the photo grid in the app.
 *
 * @see ViewHolder
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

	private static final String TAG = "PhotoAdapter";

	Context context;
	ArrayList<Photo> photos;
	SQLiteDatabase db;
	DatabaseHandler handler;

	/**
	 * PhotoAdapter (custom RecyclerView) constructor
	 *
	 * @param context Context to create the PhotoAdapter in
	 * @param photos  List of photos to display in the grid
	 * @param db      SQLite Database object
	 * @param handler DatabaseHandler object
	 */
	public PhotoAdapter(Context context, ArrayList<Photo> photos,
	                    SQLiteDatabase db, DatabaseHandler handler) {
		this.context = context;
		this.photos = photos;
		this.db = db;
		this.handler = handler;
	}

	/**
	 * Inflates the layout with the grid photo fragment.
	 *
	 * @param parent   parent to attach the items to
	 * @param viewType unused, but required in the Override
	 * @return inflated ViewHolder
	 */
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.fragment_grid_photo,
				parent, false);
		return new ViewHolder(v);
	}

	/**
	 * Creates grid item and sets the behaviour of each individual item in the photo grid.
	 * Creates an onClick listener to open the photo in full screen.
	 *
	 * @param holder   custom ViewHolder to create
	 * @param position position of the item being created in the grid
	 */
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.image.setImageBitmap(ImageHandler.bytesToBitmap(photos.get(position).getImage()));
		// Pressing the image once will open a full-screen view of the photo.
		holder.image.setOnClickListener(v -> {
			Intent intent = new Intent(context, PhotoDetailsActivity.class);
			intent.putExtra("photo", photos.get(position).getId());
			context.startActivity(intent);
		});
		// Pressing and holding on a photo will allow you to delete it.
		holder.image.setOnLongClickListener(v -> {
			new AlertDialog.Builder(context)
					.setTitle("Delete photo")
					.setMessage("Are you sure you want to delete this photo?")
					.setPositiveButton("Yes", (dialog, which) -> {
						Photo photo = photos.get(position);
						int photoId = photo.getId();
						String path = photo.getPath();
						// Deleting the photo from the link table, then the photo table
						handler.deleteEntry(DatabaseHandler.UserPhotos.TABLE, photoId);
						handler.deleteEntry(DatabaseHandler.Photos.TABLE, photoId);
						// Deleting file from internal storage
						File photoFile = new File(path);
						boolean deleted = photoFile.delete();
						if (!deleted) Log.e(TAG, "File not deleted from storage.");
						// Removing photo from photos list
						photos.remove(position);
						// Notifying RecyclerView that data has changed
						notifyDataSetChanged();
					})
					.setNegativeButton("No", null)
					.show();
			return true;
		});
	}

	/**
	 * Gets the number of items in the PhotoAdapter.
	 *
	 * @return number of items
	 */
	@Override
	public int getItemCount() {
		return photos.size();
	}

	/**
	 * Custom RecyclerView ViewHolder
	 * Used for displaying a square image preview in the photo grid.
	 *
	 * @see PhotoAdapter
	 */
	static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView image;

		/**
		 * Custom ViewHolder constructor
		 *
		 * @see PhotoAdapter
		 */
		public ViewHolder(View itemView) {
			super(itemView);
			image = itemView.findViewById(R.id.grid_photo);
		}
	}
}
