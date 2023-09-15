package io.securin.maven.plugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.securin.maven.plugin.model.Response;

public class HttpUtil {

	private static final String X_ASPM_AUTH_KEY = "X-ASPM-Auth-Key";
	private static final String AWS_CONFIG = "/cli/aws/accesstoken";
	private static final String ENC_KEYS = "/enc/keys";
	private static final String CLI_VERSION = "/cli/version";
	private static final String CLI_VERSION_DETAILS = "/cli/version/s3/details";

	private String slResultUrl;
	private Log logger;

	public HttpUtil(String slResultUrl, Log log) {
		this.slResultUrl = slResultUrl;
		this.logger = log;
	}

	ObjectMapper objMapper = new ObjectMapper();

	public Response<Map<String, String>> getAwsConfiguration(String accessToken) {

		TypeReference<Map<String, String>> mapTypRef = new TypeReference<Map<String, String>>() {
		};
		return getAPIResponse(slResultUrl + AWS_CONFIG, accessToken, null, mapTypRef);
	}

	public Response<Map<String, String>> getEncKeys(String accessToken) {
		TypeReference<Map<String, String>> mapTypRef = new TypeReference<Map<String, String>>() {
		};
		return getAPIResponse(slResultUrl + ENC_KEYS, accessToken, null, mapTypRef);
	}

	public Response<String> getCliVersion(String version, String accessToken) {
		String url = slResultUrl + CLI_VERSION;
		if (null != version)
			url = url + "?version=" + version;
		return getAPIResponse(url, accessToken, String.class, null);
	}

	public Response<Map<String, String>> getCliVersionWithDtls(String version, String accessToken) {
		String url = slResultUrl + CLI_VERSION_DETAILS;
		if (null != version)
			url = url + "?version=" + version;
		TypeReference<Map<String, String>> mapTypRef = new TypeReference<Map<String, String>>() {
		};
		return getAPIResponse(url, accessToken, null, mapTypRef);
	}

	private <T> Response<T> getAPIResponse(String reqUrl, String accessToken, Class<T> respType,
			TypeReference<T> typeRef) {
		Response<T> resp = new Response<>();
		try {
			URL obj = new URL(reqUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(X_ASPM_AUTH_KEY, accessToken);
			int statusCode = con.getResponseCode();
			resp.setResponseCode(statusCode);
			if (statusCode >= 200 && statusCode <= 399) {
				String response = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
						.collect(Collectors.joining());
				T respObj = respType != null ? convertClassType(respType, response)
						: objMapper.readValue(response, typeRef);
				resp.setResp(respObj);
				resp.setSuccess(true);
				if (logger.isDebugEnabled())
					logger.debug("Response StatusCode -- " + statusCode);
			} else {
				InputStream errStrm = null;
				if (con.getInputStream() != null) {
					errStrm = con.getInputStream();
				} else if (con.getErrorStream() != null) {
					errStrm = con.getErrorStream();
				}
				if (logger.isDebugEnabled() && errStrm != null) {
					String errResp = new BufferedReader(new InputStreamReader(errStrm)).lines()
							.collect(Collectors.joining());
					logger.debug("Response StatusCode -- " + statusCode + " -- " + errResp); // NOSONAR
				}

			}
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.error("Exception while calling api ", e);
			}
		}

		return resp;
	}

	@SuppressWarnings("unchecked")
	private <T> T convertClassType(Class<T> respType, String httpResp) throws JsonProcessingException {
		return respType.isAssignableFrom(String.class) ? (T) httpResp : objMapper.readValue(httpResp, respType);
	}

}
