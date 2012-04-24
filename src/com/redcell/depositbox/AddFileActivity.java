package com.redcell.depositbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.redcell.depositbox.utils.DropBoxUtils;

public class AddFileActivity extends ListActivity {

	private List<String> selectionList;
	private List<File> fileList;
	private final String ROOT = "/";
	private DropBoxUtils dropBoxUtils = null;
	private boolean wantingToUpload = false;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);
		selectionList = new ArrayList<String>();
		loadFileList("/");
	}

	private void loadFileList(final String path) {
		final File file = new File(path);

		if (file.isDirectory() && file.canRead()) {

			fileList = new ArrayList<File>();
			final List<String> displayList = new ArrayList<String>();

			try {
				if (!file.getCanonicalPath().equals(ROOT)) {
					displayList.add("../");
					fileList.add(file.getParentFile());
				}
			} catch (final IOException e) {
				Log.e("AddFileActivity", "Could not resolve canonical path for " + file.getPath(), e);
			}

			for (final File subFile : file.listFiles()) {
				fileList.add(subFile);

				if (subFile.isDirectory()) {
					displayList.add(subFile.getName() + "/");
				} else if (subFile.isFile()) {
					displayList.add(subFile.getName());
				}

			}
			final ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.row, displayList);
			setListAdapter(adapter);
		}

	}

	@Override
	public void onListItemClick(final ListView list, final View view, final int position, final long id) {
		final File file = fileList.get(position);

		if (file.isDirectory()) {
			loadFileList(file.getPath());
			selectionList = new ArrayList<String>();
		} else {
			if (selectionList.contains(file.getAbsolutePath())) {
				selectionList.remove(file.getAbsolutePath());
			} else {
				selectionList.add(file.getAbsolutePath());
			}
		}
	}

	public void onUploadFilesClick(final View v) {
		if (selectionList.isEmpty()) {
			// do nothing
		} else {
			if (dropBoxUtils == null) {
				final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				dropBoxUtils = DropBoxUtils.getInstance(prefs);
			}
			if (!dropBoxUtils.hasAuthenticated()) {
				dropBoxUtils.authenticate(this);
				this.wantingToUpload = true;
			} else {
				dropBoxUtils.uploadFiles(selectionList);
				selectionList = new ArrayList<String>();
			}

		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (dropBoxUtils != null && dropBoxUtils.hasAuthenticated()) {
			dropBoxUtils.preserveSession();
			if (wantingToUpload) {
				dropBoxUtils.uploadFiles(selectionList);
				selectionList = new ArrayList<String>();
				wantingToUpload = false;
			}
		}
	}
}