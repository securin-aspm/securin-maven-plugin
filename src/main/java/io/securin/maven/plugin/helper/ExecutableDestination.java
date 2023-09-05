package io.securin.maven.plugin.helper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutableDestination {

	public static Path getFileDestination(Platform platform, Map<String, String> env, String version) {
		return getFileDirectory(platform, env, version);
	}

	private static Path getFileDirectory(Platform platform, Map<String, String> env, String version) {
		if (Platform.WINDOWS.equals(platform)) {
			return Optional.ofNullable(env.get("APPDATA")).map(Paths::get)
					.map(appData -> appData.resolve("Securin" + File.separator + version))
					.orElseThrow(() -> new RuntimeException("Windows needs AppData directory."));
		}
		throw new RuntimeException("securin doesnt support - " + platform);
	}

}
