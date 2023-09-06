package io.securin.maven.plugin.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import io.securin.maven.plugin.model.Response;
import io.securin.maven.plugin.utils.EncryptUtil;
import io.securin.maven.plugin.utils.HttpUtil;
import io.securin.maven.plugin.utils.S3Util;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

public class CLIServiceHelper {

	public void populateAwsToken(HttpUtil httpUtil, String accessKey) {
		Response<Map<String, String>> awsConfig = httpUtil.getAwsConfiguration(accessKey);
		if (awsConfig.isSuccess()) {
			Response<Map<String, String>> encKyResp = httpUtil.getEncKeys(accessKey);
			Map<String, String> awsResp = awsConfig.getResp();
			EncryptUtil encUtil = EncryptUtil.getInstance();
			S3Util.init(AwsSessionCredentials.create(encUtil.decryptV1(awsResp.get("accessKey"), encKyResp.getResp()),
					encUtil.decryptV1(awsResp.get("secretKey"), encKyResp.getResp()),
					encUtil.decryptV1(awsResp.get("sessionToken"), encKyResp.getResp())));
		}
	}

	public void downloadFileFromS3(String authKey, Path secExcutblPath, String version, Log log, HttpUtil httpUtil)
			throws IOException {
		Map<String, String> versionDtls = httpUtil.getCliVersionWithDtls(version, authKey).getResp();
		populateAwsToken(httpUtil, authKey);
		S3Util s3Util = S3Util.getInstance();
		try {
			long startTime = System.nanoTime();
			log.info("Started downloading file. This process may take some time. Please wait...");
			s3Util.downLoadFile(secExcutblPath, versionDtls);
			BigDecimal bd = BigDecimal.valueOf((System.nanoTime() - startTime) / 1_000_000_000.0).setScale(2,
					RoundingMode.UP);
			log.info("File downloaded successfully in " + bd.toString() + "s");
		} finally {
			s3Util.closeS3Client();
		}
	}

}
