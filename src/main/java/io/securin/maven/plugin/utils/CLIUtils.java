package io.securin.maven.plugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CLIUtils {

	public static String getStreamContent(InputStream inpStream) throws IOException {
		try (BufferedReader buffReader = new BufferedReader(new InputStreamReader(inpStream))) {
			return buffReader.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}
}
