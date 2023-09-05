package io.securin.maven.plugin.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyUtil {

	private static Properties prop = getAppProps();

	private static Properties getAppProps() {
		Properties prop = new Properties();
		try (InputStream resourceAsStream = PropertyUtil.class.getClassLoader()
				.getResourceAsStream("config.properties")) {
			prop.load(resourceAsStream);
		} catch (IOException e) {
			throw new RuntimeException("Exception while loading plugin"); //NOSONAR
		}
		return prop;
	}

	public static String getProperty(String propName) {
		return prop.getProperty(propName);
	}

	public static synchronized void setProperty(String propName, String propValue) {
		prop.setProperty(propName, propValue);
	}

}
