package io.securin.maven.plugin.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3Util {

	private S3Client amazonS3Client;

	private static S3Util instance;

	private S3Util(S3Client amazonS3Client) {
		this.amazonS3Client = amazonS3Client;
	}

	public static void init(AwsSessionCredentials basicSessionCredentials) {
		S3Client amazonS3Client = S3Client.builder().region(Region.US_WEST_2)
				.credentialsProvider(StaticCredentialsProvider.create(basicSessionCredentials)).build();
		instance = new S3Util(amazonS3Client);
	}

	public boolean downLoadFile(Path secExcutblPath, Map<String, String> versionDtls) throws IOException {
		GetObjectRequest objectRequest = GetObjectRequest.builder()
				.key(versionDtls.get("key_path") + "/" + secExcutblPath.toFile().getName())
				.bucket(versionDtls.get("bucket_name")).build();
		ResponseBytes<GetObjectResponse> responseBytes = amazonS3Client.getObjectAsBytes(objectRequest);
		Files.write(secExcutblPath, responseBytes.asByteArray());
		return true;
	}

	public void closeS3Client() {
		amazonS3Client.close();
	}

	public static S3Util getInstance() {
		return instance;
	}

}
