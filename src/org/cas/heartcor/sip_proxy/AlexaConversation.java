package org.cas.heartcor.sip_proxy;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AlexaConversation {

	private static final String CLIENT_SECRET = "d3aecce5ea9312757ee07304466cfc5791b84d6224a2aabe95698c48b7ac1624";

	private static final String CLIENT_ID = "amzn1.application-oa2-client.47850456e7b243a4a8448045da900cd8";

	private static final String ENDPOINT = "https://access-alexa-na.amazon.com/v1/avs/speechrecognizer/recognize";

	private static final String AUDIO_CONTENT_TYPE = "audio/L16";// FIXME tbd

	private final String token;
	private final CloseableHttpClient client = HttpClients.createDefault();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, URISyntaxException {
		System.out.println("NOW TESTING: WHAT HAPPENS WHEN I SEND stream.wav to ALEXA?");
		Scanner scanner = new Scanner(System.in);
		System.out.println("Do you have an access token already? (y/n)");
		String accessToken;
		if(scanner.nextLine().contains("y")) {
			System.out.println("Give it to me then!");
			accessToken = scanner.nextLine();
		} else {
			System.out.println("Let's get one for you then...");
			System.out.println("Login at " + getOAuthUrl());
			System.out.println("Then get the request token using your hacker skills and put it down below:");
			String requestToken = scanner.nextLine();
			accessToken = getAccessToken(requestToken);
			System.out.println("Write this down somewhere: " + accessToken);
		}
		AlexaConversation conversation = AlexaConversation.connect(accessToken);
		InputStream stream = Files.newInputStream(Paths.get("stream.wav"));
		conversation.say(stream);
	}

	private AlexaConversation(String token) {
		// non-constructible, see #connect()
		this.token = token;
	}

	public static AlexaConversation connect(String token) {
		AlexaConversation conversation = new AlexaConversation(token);
		// do some connection-related stuff later?
		return conversation;
	}

	public static String getOAuthUrl() {
		String productId = "heartcor_product";
		int deviceSerialNumber = 1;
		String redirectURI = "https://localhost/response";
		String scope = "alexa:all";
		String responseType = "code";
		String scopeData = format(
				"{\"%s\": {\"productID\": \"%s\", \"productInstanceAttributes\": {\"deviceSerialNumber\": \"%d\"}}}", //
				scope, productId, deviceSerialNumber);
		return format( //
				"https://www.amazon.com/ap/oa?client_id=%s&scope=%s&scope_data=%s&response_type=%s&redirect_uri=%s", //
				URLEncoder.encode(CLIENT_ID, Charset.defaultCharset()), //
				URLEncoder.encode(scope, Charset.defaultCharset()), //
				URLEncoder.encode(scopeData, Charset.defaultCharset()), //
				URLEncoder.encode(responseType, Charset.defaultCharset()), //
				URLEncoder.encode(redirectURI, Charset.defaultCharset()));
	}
	
	public static String getAccessToken(String requestToken) throws IOException {
		HttpPost request = new HttpPost("https://api.amazon.com/auth/o2/token");
		String body = format("grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s", //
				requestToken, CLIENT_ID, CLIENT_SECRET, "https://localhost/response");
		request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		request.setEntity(new ByteArrayEntity(body.getBytes(), ContentType.APPLICATION_FORM_URLENCODED));
		CloseableHttpResponse response = HttpClients.createDefault().execute(request);
		String responseBody = new String(response.getEntity().getContent().readAllBytes());
		if(response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed to retrieve access token:");
			System.out.println(response.toString());
			System.out.println(responseBody);
			throw new IOException("Failed to retrieve access token.");
		}
		System.out.println("Got OAuth2 access token successfully!");
		JsonObject json = new JsonParser().parse(responseBody).getAsJsonObject();
		return json.get("access_token").getAsString();
	}

	public void say(InputStream something) throws IOException, URISyntaxException {
		HttpPost request = new HttpPost(getAddressToAlexa());
		request.setHeader("Authorization", "Bearer " + token);

		MultipartEntityBuilder multiparts = MultipartEntityBuilder.create();
		String requestJson = getRequestJson();
		System.out.println("==> Dispatching following request: " + requestJson);
		multiparts.addTextBody("request", requestJson, ContentType.APPLICATION_JSON);
		multiparts.addBinaryBody("audio", something, ContentType.create(AUDIO_CONTENT_TYPE), "recording");

		request.setEntity(multiparts.build());
		CloseableHttpResponse response = client.execute(request);
		System.out.println("<== Got the following back: " + response);
		HttpEntity entity = response.getEntity();
	}

	private String getRequestJson() {
		JsonObject request = new JsonObject();
		JsonObject messageHeader = new JsonObject();
		JsonArray deviceContexts = new JsonArray(1);
		JsonObject deviceContext = new JsonObject();
		deviceContext.addProperty("name", "some-name"); // FIXME
		deviceContext.addProperty("namespace", "some-namespace"); // FIXME
		JsonObject deviceContextPayload = new JsonObject();
		deviceContextPayload.addProperty("streamId", "some-stream-id"); // FIXME
		deviceContextPayload.addProperty("offsetInMilliseconds", "0"); // FIXME
		deviceContextPayload.addProperty("playerActivity", "some-activity"); // FIXME
		deviceContext.add("payload", deviceContextPayload);
		deviceContexts.add(deviceContext);
		messageHeader.add("deviceContext", deviceContexts);
		request.add("messageHeader", messageHeader);
		JsonObject messageBody = new JsonObject();
		messageBody.addProperty("profile", "alexa-close-talk");
		messageBody.addProperty("locale", "de-de");
		messageBody.addProperty("format", AUDIO_CONTENT_TYPE);
		request.add("messageBody", messageBody);
		return new GsonBuilder().create().toJson(request);
	}

	private static URI getAddressToAlexa() throws URISyntaxException {
		return new URI(ENDPOINT);
	}

}
