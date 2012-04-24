package com.redcell.depositbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DepositBoxActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void onAddFileClick(final View view) {
		final Intent intent = new Intent(this, AddFileActivity.class);
		startActivity(intent);
	}

	public void onSettingsClick(final View view) {
		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}