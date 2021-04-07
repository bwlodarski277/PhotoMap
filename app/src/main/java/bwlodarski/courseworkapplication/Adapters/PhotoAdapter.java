package bwlodarski.courseworkapplication.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import bwlodarski.courseworkapplication.Activities.PhotoDetailsActivity;
import bwlodarski.courseworkapplication.Helpers.DatabaseHandler;
import bwlodarski.courseworkapplication.Helpers.ImageHandler;
import bwlodarski.courseworkapplication.Models.Photo;
import bwlodarski.courseworkapplication.R;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

	Context context;
	ArrayList<Photo> photos;
	SQLiteDatabase db;
	DatabaseHandler handler;

	public PhotoAdapter(Context context, ArrayList<Photo> photos,
	                    SQLiteDatabase db, DatabaseHandler handler) {
		this.context = context;
		this.photos = photos;
		this.db = db;
		this.handler = handler;
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView image;

		public ViewHolder(View itemView) {
			super(itemView);
			image = itemView.findViewById(R.id.grid_photo);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.grid_photo, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.image.setImageBitmap(ImageHandler.getBitmap(photos.get(position).getImage()));
		holder.image.setOnClickListener(v -> {
			Intent intent = new Intent(context, PhotoDetailsActivity.class);
			intent.putExtra("photo", photos.get(position).getImage());
			context.startActivity(intent);
		});
		holder.image.setOnLongClickListener(v -> {
			new AlertDialog.Builder(context)
					.setTitle("Delete photo")
					.setMessage("Are you sure you want to delete this photo?")
					.setPositiveButton("Yes", (dialog, which) -> {
						handler.deleteEntry(DatabaseHandler.Photos.TABLE,
								photos.get(position).getId());
						photos.remove(position);
						notifyDataSetChanged();
					})
					.setNegativeButton("No", null)
					.show();
			return true;
		});
	}

	@Override
	public int getItemCount() {
		return photos.size();
	}
}
