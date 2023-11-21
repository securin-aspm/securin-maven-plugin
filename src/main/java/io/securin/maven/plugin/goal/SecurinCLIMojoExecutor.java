package io.securin.maven.plugin.goal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.securin.maven.plugin.helper.CLIServiceHelper;
import io.securin.maven.plugin.helper.ExecutableDestination;
import io.securin.maven.plugin.helper.Platform;
import io.securin.maven.plugin.model.Response;
import io.securin.maven.plugin.utils.CLIUtils;
import io.securin.maven.plugin.utils.HttpUtil;
import io.securin.maven.plugin.utils.PropertyUtil;

@Mojo(name = "securin-plugin", defaultPhase = LifecyclePhase.TEST)
public class SecurinCLIMojoExecutor extends AbstractMojo {

	@Parameter(property = "apiKey")
	private String apiKey;

	@Parameter(property = "version")
	private String version;

	@Parameter(property = "appId")
	private String appId;

	@Parameter(property = "branchName")
	private String branchName;

	@Parameter(property = "debug")
	private boolean debug;

	@Parameter(property = "args")
	private List<String> args;

	@Parameter(property = "skipBuildBreak", defaultValue = "false")
	private boolean skipBuildBreak;

	@Parameter(property = "skipScan", defaultValue = "false")
	private boolean skipScan;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		if (skipScan) {
			log.info("Securin scan skipped");
			return;
		}
		if (!validRequestParams(log)) {
			return;
		}
		Path securinExePath = getExecutablePath(log);
		if (securinExePath != null) {
			int exitValue = runProcess(securinExePath.toFile().getAbsolutePath());
			if (exitValue == 0 && skipBuildBreak) {
				log.info("Skipping build break option");
			} else if (exitValue == 2) {
				throw new MojoFailureException("Scan failed because of severity threshold exceeded");
			}
		}
	}

	public boolean validRequestParams(Log log) {
		if (!CLIUtils.isNotEmpty(apiKey)) {
			log.error("Authorization Failed - API Key is not available");
			return false;
		}
		return true;
	}

	public Path getExecutablePath(Log log) {
		Platform currentOS = Platform.currentOS();
		HttpUtil httpUtil = new HttpUtil(PropertyUtil.getProperty("SL_RESULT_API_HOST"), log);

		Response<String> cliVerResp = httpUtil.getCliVersion(version, apiKey);
		if (!cliVerResp.isSuccess() && cliVerResp.getResponseCode() == 401) {
			log.error("Authorization Failed - API key is not valid");
			return null;
		}
		String cliVersion = cliVerResp.getResp();
		if (cliVersion == null || cliVersion.length() <= 0) {
			log.error("Unable to get CLI executable version");
			return null;
		}
		Path fileDestinationPath = ExecutableDestination.getFileDestination(currentOS, System.getenv(), cliVersion);
		Path securinExeName = fileDestinationPath.resolve(currentOS.securinExecutableFileName);
		log.info("securin executable path -- " + securinExeName.toFile().getAbsolutePath());
		if (!Files.exists(securinExeName)) {
			downloadFileFromS3(fileDestinationPath, securinExeName, log, cliVersion, httpUtil);
		} else {
			validateExistingCLIVersion(fileDestinationPath, securinExeName, log, cliVersion, httpUtil);
		}
		return securinExeName;
	}

	public int runProcess(String cliExecutablePath) {
		Log log = getLog();
		List<String> parts = new ArrayList<>();
		parts.add(cliExecutablePath);
		parts.add("-api_key=" + apiKey);
		if (CLIUtils.isNotEmpty(appId)) {
			parts.add("-app_id=" + appId);
		}
		if (CLIUtils.isNotEmpty(branchName)) {
			parts.add("-branch_name=" + branchName);
		}
		if (debug) {
			parts.add("-is_debug=true");
		}
		if(skipBuildBreak) {
			parts.add("-skip_build_fail=true");
		}
		parts.add("-enable_color=true");
		parts.addAll(args);
		if (log.isDebugEnabled()) {
			log.debug("cli arguments " + parts);
		}
		ProcessBuilder pb = new ProcessBuilder(parts);
		pb.environment().put("FORCE_COLOR", "true");
		pb.directory(getProjectRootDirectory());
		Process process;
		int exitValue = 0;
		try {
			log.info("Securin maven plugin started");
			process = pb.start();
			String inpData = CLIUtils.getStreamContent(process.getInputStream());
			String errData = CLIUtils.getStreamContent(process.getErrorStream());
			process.waitFor(30, TimeUnit.MINUTES);
			exitValue = process.exitValue();
			if (errData != null && errData.length() > 0) {
				String errDt = errData.replaceAll(System.lineSeparator(), " ");
				log.error(errDt);
				return exitValue;
			}
			List<?> logs = Arrays.asList(inpData.split("\\r?\\n"));
			logs.forEach(line -> log.info(line.toString()));

		} catch (IOException | InterruptedException e) { // NOSONAR
			log.error("Exception while running cli");
			if (log.isDebugEnabled()) {
				log.debug("Error ", e);
			}
		}
		return exitValue;
	}

	private File getProjectRootDirectory() {
		if (null != getPluginContext()) {
			MavenProject project = (MavenProject) getPluginContext().get("project");
			if (project == null) {
				throw new IllegalStateException("the `project` is missing from the plugin context");
			}
			return project.getBasedir();
		}
		return null;
	}

	private void validateExistingCLIVersion(Path fileDestinationPath, Path securinExeName, Log log,
			String pluginVersion, HttpUtil httpUtil) {
		List<String> parts = new ArrayList<>();
		parts.add(securinExeName.toFile().getAbsolutePath());
		parts.add("-version");
		ProcessBuilder pb = new ProcessBuilder(parts);
		pb.environment().put("FORCE_COLOR", "true");
		pb.directory(getProjectRootDirectory());
		Process process;
		try {
			process = pb.start();
			String data = CLIUtils.getStreamContent(process.getInputStream());
			String cliVersion = data.trim();
			String pluginVer = "\"" + pluginVersion + "\"";
			if (!cliVersion.equalsIgnoreCase(pluginVer)) {
				downloadFileFromS3(fileDestinationPath, securinExeName, log, pluginVersion, httpUtil);
			}
		} catch (IOException e) { // NOSONAR
			log.error("Exception while running cli");
			if (log.isDebugEnabled()) {
				log.debug("Error ", e);
			}
		}
	}

	private void downloadFileFromS3(Path fileDestinationPath, Path secExcutblPath, Log log, String pluginVersion,
			HttpUtil httpUtil) {
		CLIServiceHelper servHelper = new CLIServiceHelper();
		try {
			Files.createDirectories(fileDestinationPath);
			servHelper.downloadFileFromS3(apiKey, secExcutblPath, pluginVersion, log, httpUtil);
		} catch (IOException e) {
			log.error("Exception while downloading from S3");
		}
	}

}
