package org.hive2hive.mobile.files;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;

import java.util.List;

/**
 * @author Nico
 *         inspired by http://custom-android-dn.blogspot.in/2013/01/create-simple-file-explore-in-android.html
 */
public class FileArrayAdapter extends ArrayAdapter<AndroidFile> {

	private static final int LAYOUT_ID = R.layout.fragment_files;

	private final H2HApplication context;
	private final AndroidFileComparator comparator;

	public FileArrayAdapter(H2HApplication context, List<AndroidFile> items) {
		super(context, LAYOUT_ID, items);
		this.context = context;
		this.comparator = new AndroidFileComparator();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(LAYOUT_ID, null);
		}

		final AndroidFile node = getItem(position);
		if (node != null) {
			TextView nameText = (TextView) view.findViewById(R.id.file_list_name);
			nameText.setText(node.getFile().getName());

			TextView detailsText = (TextView) view.findViewById(R.id.file_list_details);
			detailsText.setText(getDetailsText(node));
			node.setDetailsView(detailsText);

			// TextView dateText = (TextView) v.findViewById(R.id.file_list_date);
			// dateText.setText(fileTaste.getDate());

			// register the image view at the file state holder in order to automatically get updates
			ImageView fileImage = (ImageView) view.findViewById(R.id.file_list_icon);
			node.setImageView(fileImage);

			Drawable icon = context.getResources().getDrawable(node.getState().getIconId(node.isFile()));
			fileImage.setImageDrawable(icon);

			// set the progress bar
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.file_list_progress);
			node.setProgressBar(progress);
		}

		return view;
	}

	@Override
	public void add(AndroidFile file) {
		if(getPosition(file) > 0) {
			// don't add it because it is already in the list
		} else {
			super.add(file);
		}
	}

	private String getDetailsText(AndroidFile node) {
		if (node.isFolder()) {
			// show item count
			int items = node.getChildren().size();
			if (items == 0) {
				return context.getString(R.string.folder_empty);
			} else if (items == 1) {
				return context.getString(R.string.folder_single_item);
			} else {
				return context.getString(R.string.folder_items, items);
			}
		} else {
			return context.getString(node.getState().getMessageId());
		}
	}

	public void updateView(boolean sort) {
		if (sort) {
			super.sort(comparator);
		} else {
			notifyDataSetChanged();
		}
	}
}
