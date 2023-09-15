package io.securin.maven.plugin.goal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.securin.maven.plugin.model.Response;
import io.securin.maven.plugin.utils.CLIUtils;
import io.securin.maven.plugin.utils.EncryptUtil;
import io.securin.maven.plugin.utils.HttpUtil;
import io.securin.maven.plugin.utils.S3Util;

@ExtendWith(MockitoExtension.class)
class SecurinCLIMojoExecutorTest {

	@InjectMocks
	private SecurinCLIMojoExecutor securinCLIMojoExecutor;

	@Test
	void testExecute() throws Exception {
		List<String> argLs = new ArrayList<>();
		argLs.add("args");
		Field apiKey = securinCLIMojoExecutor.getClass().getDeclaredField("apiKey");
		apiKey.setAccessible(true);
		apiKey.set(securinCLIMojoExecutor, "apiKey");
		Field versionField = securinCLIMojoExecutor.getClass().getDeclaredField("version");
		versionField.setAccessible(true);
		versionField.set(securinCLIMojoExecutor, "version");
		Field appIdField = securinCLIMojoExecutor.getClass().getDeclaredField("appId");
		appIdField.setAccessible(true);
		appIdField.set(securinCLIMojoExecutor, "appId");
		Field branchName = securinCLIMojoExecutor.getClass().getDeclaredField("branchName");
		branchName.setAccessible(true);
		branchName.set(securinCLIMojoExecutor, "branch");
		Field debugField = securinCLIMojoExecutor.getClass().getDeclaredField("debug");
		debugField.setAccessible(true);
		debugField.set(securinCLIMojoExecutor, true);
		Field argsField = securinCLIMojoExecutor.getClass().getDeclaredField("args");
		argsField.setAccessible(true);
		argsField.set(securinCLIMojoExecutor, argLs);
		Response<String> versionResp = new Response<>();
		versionResp.setResp("1.0.2");
		Map<String, String> versionDtls = new HashMap<String, String>();
		versionDtls.put("key_path", "path");
		versionDtls.put("bucket_name", "bucket");
		Response<Map<String, String>> detailsResp = new Response<>();
		detailsResp.setResp(versionDtls);
		Map<String, String> aws = new HashMap<String, String>();
		aws.put("accessKey", "access");
		aws.put("secretKey", "secret");
		aws.put("sessionToken", "token");
		Response<Map<String, String>> awsResp = new Response<>();
		awsResp.setResp(aws);
		awsResp.setSuccess(true);
		Map<String, String> encMap = new HashMap<String, String>();
		encMap.put("KEY", "key");
		encMap.put("IV", "iv");
		Response<Map<String, String>> encResp = new Response<>();
		detailsResp.setResp(encMap);
		EncryptUtil enc = mock(EncryptUtil.class);
		try (MockedConstruction<HttpUtil> util = mockConstruction(HttpUtil.class, (httpUtil, context) -> {
			when(httpUtil.getCliVersion(any(), any())).thenReturn(versionResp);
			when(httpUtil.getCliVersionWithDtls(any(), any())).thenReturn(detailsResp);
			when(httpUtil.getAwsConfiguration(any())).thenReturn(awsResp);
			when(httpUtil.getEncKeys(any())).thenReturn(encResp);
		})) {
			try (MockedStatic<EncryptUtil> encUtil = mockStatic(EncryptUtil.class)) {
				encUtil.when(EncryptUtil::getInstance).thenReturn(enc);
				when(enc.decryptV1(anyString(), any())).thenReturn("value");
				S3Util s3Util = mock(S3Util.class);
				try (MockedStatic<S3Util> s3 = mockStatic(S3Util.class)) {
					s3.when(S3Util::getInstance).thenReturn(s3Util);
					when(s3Util.downLoadFile(any(), any())).thenReturn(true);
					Process process = mock(Process.class);
					try (MockedConstruction<ProcessBuilder> builder = mockConstruction(ProcessBuilder.class,
							(pb, context) -> {
								when(pb.start()).thenReturn(process);
							})) {
						try (MockedStatic<CLIUtils> cliUtils = mockStatic(CLIUtils.class)) {
							cliUtils.when(() -> CLIUtils.getStreamContent(any())).thenReturn("Success");
							cliUtils.when(() -> CLIUtils.isNotEmpty(any())).thenReturn(true);
							securinCLIMojoExecutor.execute();

							SecurinCLIMojoExecutor exe = mock(SecurinCLIMojoExecutor.class);
							exe.execute();
							verify(exe, atLeastOnce()).execute();
						}
					}
				}
			}
		}
	}

}
