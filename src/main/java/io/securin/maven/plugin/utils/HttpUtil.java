package io.securin.maven.plugin.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.securin.maven.plugin.model.Response;

public class HttpUtil {

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

	private HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(30)).build();

	public Response<Map<String, String>> getAwsConfiguration(String accessToken) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(slResultUrl + AWS_CONFIG))
				.headers(getHeaders(accessToken)).GET().build();
		TypeReference<Map<String, String>> mapTypRef = new TypeReference<Map<String, String>>() {
		};
		return getAPIResponse(request, mapTypRef);
	}
	
	public Response<Map<String, String>> getEncKeys(String accessToken) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(slResultUrl + ENC_KEYS))
				.headers(getHeaders(accessToken)).GET().build();
		TypeReference<Map<String, String>> mapTypRef = new TypeReference<Map<String, String>>() {
		};
		return getAPIResponse(request, mapTypRef);
	}
	
	public Response<String> getCliVersion(String version, String accessToken) {
		String url = slResultUrl + CLI_VERSION;
		if (null != version)
			url = url + "?version=" + version;
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(getHeaders(accessToken)).GET()
				.build();
		return getAPIResponse(request, String.class);
	}
	
	public Response<Map<String, String>> getCliVersionWithDtls(String version, String accessToken) {
		String url = slResultUrl + CLI_VERSION_DETAILS;
		if (null != version)
			url = url + "?version=" + version;
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).headers(getHeaders(accessToken)).GET()
				.build();
		TypeReference<Map<String, String>> mapTypRef = new TypeReference<Map<String, String>>() {
		};
		return getAPIResponse(request, mapTypRef);
	}

	public <T> Response<T> getAPIResponse(HttpRequest request, TypeReference<T> typeRef) {
		return getAPIResponse(request, null, typeRef);
	}
	
	public <T> Response<T> getAPIResponse(HttpRequest request, Class<T> respType) {
		return getAPIResponse(request, respType, null);
	}

	private String[] getHeaders(String authKey) {
		return new String[] { "X-ASPM-Auth-Key", authKey };
	}

	private <T> Response<T> getAPIResponse(HttpRequest request, Class<T> respType, TypeReference<T> typeRef) {
		Response<T> resp = new Response<>();
		try {
			if(logger.isDebugEnabled())
				logger.debug("Request --"+request.uri().getPath());
			HttpResponse<String> httpResp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			int statusCode = httpResp.statusCode();
			if (statusCode >= 200 && statusCode <= 399) {
				T respObj = respType != null ? convertClassType(respType, httpResp)
						: objMapper.readValue(httpResp.body(), typeRef);
				resp.setResp(respObj);
				resp.setSuccess(true);
				if(logger.isDebugEnabled())
					logger.debug("Response StatusCode -- " + statusCode);
			} else {
				if(logger.isDebugEnabled())
					logger.debug("Response StatusCode -- " + statusCode + " -- " + httpResp.body()); // NOSONAR
			}
		} catch (Exception e) { // NOSONAR
			logger.error("Exception while calling api ", e);
		}
		return resp;
	}

	@SuppressWarnings("unchecked")
	private <T> T convertClassType(Class<T> respType, HttpResponse<String> httpResp) throws JsonProcessingException {
		return respType.isAssignableFrom(String.class) ? (T) httpResp.body()
				: objMapper.readValue(httpResp.body(), respType);
	}

}
