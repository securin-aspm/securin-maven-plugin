package io.securin.maven.plugin.helper;

import java.util.Locale;

public enum Platform {
	WINDOWS("securin-cli-win.exe"), MAC("securin-cli-mac"), LINUX("securin-cli-linux");

	public final String securinExecutableFileName;

	Platform(String securinExecutableFileName) {
		this.securinExecutableFileName = securinExecutableFileName;
	}

	public static Platform currentOS() {
		return getOSFile(System.getProperty("os.name"));
	}

	protected static Platform getOSFile(String osName) {
		String osNameLower = osName.toLowerCase(Locale.ENGLISH);
		if (osNameLower.contains("windows")) {
			return WINDOWS;
		} else if (osNameLower.contains("mac os x") || osNameLower.contains("darwin") || osNameLower.contains("osx")) {
			return MAC;
		} else if(osNameLower.contains("linux")) {
			return LINUX;
		}
		throw new IllegalArgumentException(osNameLower + " is not supported.");
	}
}
