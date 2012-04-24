package com.redcell.depositbox.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropBoxUtils {

	final static private String APP_KEY = "3ap8oflrk1t3ewq";
	final static private String APP_SECRET = "pptm1lijg8tmryt";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	final static private String KEY = "key";
	final static private String SECRET = "secret";
	private final SharedPreferences datastore;

	private final DropboxAPI<AndroidAuthSession> mDBApi;

	private static DropBoxUtils instance = null;

	public static DropBoxUtils getInstance(final SharedPreferences datastore) {
		if (instance == null) {
			instance = new DropBoxUtils(datastore);
		}
		return instance;
	}

	private DropBoxUtils(final SharedPreferences datastore) {
		this.datastore = datastore;
		final AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		final AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);

		final AccessTokenPair accessToken = getKeys();

		mDBApi = new DropboxAPI<AndroidAuthSession>(session);

		if (accessToken != null) {
			mDBApi.getSession().setAccessTokenPair(accessToken);
		}
	}

	public boolean hasAuthenticated() {
		return mDBApi.getSession().getAccessTokenPair() != null || mDBApi.getSession().authenticationSuccessful();
	}

	public void authenticate(final Context caller) {
		mDBApi.getSession().startAuthentication(caller);
	}

	public void preserveSession() {
		if (mDBApi.getSession().authenticationSuccessful()) {
			try {
				// MANDATORY call to complete auth.
				// Sets the access token on the session
				mDBApi.getSession().finishAuthentication();

				final AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

				// Provide your own storeKeys to persist the access token pair
				// A typical way to store tokens is using SharedPreferences
				storeKeys(tokens.key, tokens.secret);
			} catch (final IllegalStateException e) {
				Log.i("DbAuthLog", "Error authenticating", e);
			}
		}
	}

	private void storeKeys(final String key, final String secret) {
		datastore.edit().putString(KEY, key).putString(SECRET, secret).commit();
	}

	private AccessTokenPair getKeys() {
		final String key = datastore.getString(KEY, "");
		final String secret = datastore.getString(SECRET, "");

		if (key.equals("") || secret.equals("")) {
			return null;
		}

		final AccessTokenPair token = new AccessTokenPair(key, secret);
		return token;
	}

	@SuppressWarnings("unchecked")
	public void uploadFiles(final List<String> selectionList) {

		final Uploader uploader = new Uploader();
		uploader.execute(selectionList);
	}

	@SuppressWarnings("unchecked")
	public void downloadFiles(final List<String> selectionList) {

		final Downloader downloader = new Downloader();
		downloader.execute(selectionList);
	}

	private class Uploader extends AsyncTask<List<String>, Integer, Long> {
		private long fileSize = 0;

		@Override
		protected Long doInBackground(final List<String>... params) {
			final List<String> fileList = params[0];

			for (final String fileName : fileList) {
				final File file = new File(fileName);
				try {
					final InputStream input = getInputStream(file);
					final Entry newEntry = mDBApi.putFile(getFileName(file), input, this.fileSize, null, null);
					Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
				} catch (final DropboxUnlinkedException e) {
					// User has unlinked, ask them to link again here.
					Log.e("DbExampleLog", "User has unlinked.");
				} catch (final DropboxException e) {
					Log.e("DbExampleLog", "Something went wrong while uploading.");
				} catch (final IOException e) {
					Log.wtf("DropBoxUtils", "Reading from the file broke");
				}
			}

			return 1L;
		}

		private InputStream getInputStream(final File file) throws IOException {
			this.fileSize = file.length();
			final InputStream fileStream = new FileInputStream(file);

			if (compress()) {
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				final OutputStream compressedData = new GZIPOutputStream(output);

				final byte bytes[] = new byte[2048];
				int i = fileStream.read(bytes, 0, bytes.length);
				while (i != -1) {
					compressedData.write(bytes, 0, i);
					i = fileStream.read(bytes, 0, bytes.length);
				}
				compressedData.flush();
				compressedData.close();
				final byte[] compressedFile = output.toByteArray();
				this.fileSize = compressedFile.length;
				return encrypt(compressedFile);

			} else {
				return fileStream;
			}
		}

		private String getFileName(final File file) {

			String filename = file.getName();

			if (obfuscate()) {
				MessageDigest hash = null;
				try {
					hash = MessageDigest.getInstance("SHA-512");
				} catch (final NoSuchAlgorithmException e) {
					try {
						hash = MessageDigest.getInstance("MD5");
					} catch (final NoSuchAlgorithmException e1) {
						Log.wtf("DropBoxUtils", e1);
					}
				}
				if (hash != null) {
					final byte[] bytes = hash.digest(file.getAbsolutePath().getBytes());
					final StringBuffer sb = new StringBuffer();
					for (int i = 0; i < bytes.length; i++) {
						sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
					}
					filename = sb.toString();
				}
			}

			if (compress()) {
				filename += ".gz";
			}

			return filename;
		}

		private InputStream encrypt(final byte[] bytes) {
			if (encrypt()) {
				return new ByteArrayInputStream(bytes);
			} else {
				return new ByteArrayInputStream(bytes);
			}
		}

		private boolean encrypt() {
			return SettingsUtil.getInstance().isEncrypt();
		}

		private boolean compress() {
			return SettingsUtil.getInstance().isCompress();
		}

		private boolean obfuscate() {
			return SettingsUtil.getInstance().isObfuscate();
		}

	}

	private class Downloader extends AsyncTask<List<String>, Integer, Long> {

		@Override
		protected Long doInBackground(final List<String>... params) {
			long size = 0;
			final List<String> fileList = params[0];

			for (final String fileName : fileList) {
				final File file = new File(fileName);
				try {
					final DropboxFileInfo newEntry = mDBApi.getFile(fileName, null, new FileOutputStream(file), null);
					size += newEntry.getFileSize();
				} catch (final DropboxUnlinkedException e) {
					// User has unlinked, ask them to link again here.
					Log.e("DbExampleLog", "User has unlinked.");
				} catch (final DropboxException e) {
					Log.e("DbExampleLog", "Something went wrong while downloading.");
				} catch (final FileNotFoundException e) {
					Log.wtf("DropBoxUtils", "Reading from the file broke");
				}
			}

			return size;
		}
	}

}
