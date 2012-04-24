package com.redcell.depositbox.utils;

public class SettingsUtil {

	private static SettingsUtil instance;

	public static SettingsUtil getInstance() {
		if (instance == null) {
			instance = new SettingsUtil();
		}
		return instance;
	}

	private boolean encrypt;
	private boolean compress;
	private boolean obfuscate;

	private SettingsUtil() {
		this.setEncrypt(false);
		this.setCompress(false);
		this.setObfuscate(false);
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(final boolean encrypt) {
		this.encrypt = encrypt;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(final boolean compress) {
		this.compress = compress;
	}

	public boolean isObfuscate() {
		return obfuscate;
	}

	public void setObfuscate(final boolean obfuscate) {
		this.obfuscate = obfuscate;
	}
}
