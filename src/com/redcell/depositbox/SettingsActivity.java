package com.redcell.depositbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.redcell.depositbox.utils.SettingsUtil;

public class SettingsActivity extends Activity {

	private final String ENCRYPT = "ENCRYPT";
	private final String COMPRESS = "COMPRESS";
	private final String OBFUSCATE = "OBFUSCATE";
	private final SettingsUtil settings = SettingsUtil.getInstance();

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		final ToggleButton encrypt = (ToggleButton) findViewById(R.id.encrypt);
		if (!getPreferences(MODE_MULTI_PROCESS).contains(ENCRYPT)) {
			getPreferences(MODE_MULTI_PROCESS).edit().putBoolean(ENCRYPT, false).commit();
		}

		final boolean encryptValue = getPreferences(MODE_MULTI_PROCESS).getBoolean(ENCRYPT, false);
		encrypt.setChecked(encryptValue);
		settings.setEncrypt(encryptValue);

		final ToggleButton compress = (ToggleButton) findViewById(R.id.compress);
		if (!getPreferences(MODE_MULTI_PROCESS).contains(COMPRESS)) {
			getPreferences(MODE_MULTI_PROCESS).edit().putBoolean(COMPRESS, false).commit();
		}

		final boolean compressValue = getPreferences(MODE_MULTI_PROCESS).getBoolean(COMPRESS, false);
		compress.setChecked(compressValue);
		settings.setCompress(compressValue);

		final ToggleButton obfuscate = (ToggleButton) findViewById(R.id.obfuscate);
		if (!getPreferences(MODE_MULTI_PROCESS).contains(OBFUSCATE)) {
			getPreferences(MODE_MULTI_PROCESS).edit().putBoolean(OBFUSCATE, false).commit();
		}

		final boolean obfuscateValue = getPreferences(MODE_MULTI_PROCESS).getBoolean(OBFUSCATE, false);
		obfuscate.setChecked(obfuscateValue);
		settings.setObfuscate(obfuscateValue);

	}

	public void onEncryptClick(final View view) {
		final boolean encryptChecked = ((ToggleButton) findViewById(R.id.encrypt)).isChecked();
		getPreferences(MODE_MULTI_PROCESS).edit().putBoolean(ENCRYPT, encryptChecked).commit();
		settings.setEncrypt(encryptChecked);
	}

	public void onCompressClick(final View view) {
		final boolean compressChecked = ((ToggleButton) findViewById(R.id.compress)).isChecked();
		getPreferences(MODE_MULTI_PROCESS).edit().putBoolean(COMPRESS, compressChecked).commit();
		settings.setCompress(compressChecked);
	}

	public void onObfuscateClick(final View view) {
		final boolean obfuscateChecked = ((ToggleButton) findViewById(R.id.obfuscate)).isChecked();
		getPreferences(MODE_MULTI_PROCESS).edit().putBoolean(OBFUSCATE, obfuscateChecked).commit();
		settings.setObfuscate(obfuscateChecked);
	}
}
