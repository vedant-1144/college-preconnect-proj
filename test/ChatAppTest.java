package com.aws.chat_app;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class ChatAppTest {

	// Change if your backend runs on different host/port
	private static final String BASE_HOST = "http://localhost";
	private static final int BASE_PORT = 8081;
	private static final String API_PREFIX = "/api";

	private static String token;
	private static String username;
	private static String email;
	private static final String PASSWORD = "TestPass#123";
	private static final String DISPLAY = "RestAssured User " + Instant.now().toEpochMilli();

	private static File avatarFile;

	@BeforeAll
	public static void beforeAll() throws Exception {
		RestAssured.baseURI = BASE_HOST;
		RestAssured.port = BASE_PORT;
		RestAssured.basePath = API_PREFIX;

		username = "rest assured user";
		email = username + "@example.test";

		// prepare a tiny PNG as avatar from base64 (1x1 transparent PNG)
		String base64Png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=";
		byte[] png = Base64.getDecoder().decode(base64Png);
		avatarFile = File.createTempFile("rs-avatar-", ".png");
		Files.write(avatarFile.toPath(), png);
		avatarFile.deleteOnExit();
	}

//	@AfterAll
//	public static void afterAll() {
//		if (avatarFile != null && avatarFile.exists()) {
//			avatarFile.delete();
//		}
//	}

	@Test
	@Order(1)
	public void t01_registerUser() {
		Map<String, Object> body = Map.of(
				"username", username,
				"email", email,
				"password", PASSWORD,
				"displayName", DISPLAY
		);

		given()
				.contentType(ContentType.JSON)
				.body(body)
				.when()
				.post("/auth/register")
				.then()
				.statusCode(anyOf(is(200), is(201)));
	}

	@Test
	@Order(2)
	public void t02_loginUser_and_getToken() {
		Map<String, Object> body = Map.of(
				"username", username,
				"password", PASSWORD
		);

		Response resp =
				given()
						.contentType(ContentType.JSON)
						.body(body)
						.when()
						.post("/auth/login")
						.then()
						.statusCode(200)
						.contentType(ContentType.JSON)
						.extract().response();

		// backend may expose token in "token" or "accessToken" or "data.token"
		String tok = null;
		if (resp.jsonPath().get("token") != null) tok = resp.jsonPath().getString("token");
		if (tok == null && resp.jsonPath().get("accessToken") != null) tok = resp.jsonPath().getString("accessToken");
		if (tok == null && resp.jsonPath().get("data.token") != null) tok = resp.jsonPath().getString("data.token");

		assertThat("JWT token must be present in login response", tok, notNullValue());
		token = tok;
	}

	@Test
	@Order(3)
	public void t03_getMyProfile() {
		Response resp =
				given()
						.headers(Map.of("Authorization", "Bearer " + token))
						.when()
						.get("/users/me")
						.then()
						.statusCode(200)
						.contentType(ContentType.JSON)
						.body("username", equalTo(username))
						.extract().response();

		// store some fields if needed later
		String profileUsername = resp.jsonPath().getString("username");
		assertThat(profileUsername, equalTo(username));
	}

	@Test
	@Order(4)
	public void t04_uploadAvatar() {
		// upload multipart form-data file field named "file"
		Response resp =
				given()
						.multiPart("file", avatarFile)
						.header("Authorization", "Bearer " + token)
						.when()
						.post("/users/me/avatar")
						.then()
						.statusCode(anyOf(is(200), is(201)))
						.contentType(ContentType.JSON)
						.body("url", notNullValue())
						.extract().response();

		String url = resp.jsonPath().getString("url");
		assertThat(url, not(emptyString()));
	}

	@Test
	@Order(5)
	public void t05_sendMessageToGlobalConversation() {
		Map<String, Object> body = Map.of("text", "Hello from RestAssured at " + Instant.now().toString());

		given()
				.contentType(ContentType.JSON)
				.header("Authorization", "Bearer " + token)
				.body(body)
				.when()
				.post("/chats/global/messages")
				.then()
				.statusCode(anyOf(is(200), is(201), is(204)));
	}

	@Test
	@Order(6)
	public void t06_getMessagesAndVerify() {
		Response resp =
				given()
						.header("Authorization", "Bearer " + token)
						.queryParam("limit", 100)
						.when()
						.get("/chats/global/messages")
						.then()
						.statusCode(200)
						.contentType(ContentType.JSON)
						.extract().response();

		// expect array (or list) response
		List<?> messages = resp.jsonPath().getList("");
		assertThat(messages, notNullValue());
		assertThat(messages.size(), greaterThanOrEqualTo(1));

		// verify at least one message has "text" field non-empty
		boolean hasText = messages.stream().anyMatch(m -> {
			if (!(m instanceof Map)) return false;
			Object txt = ((Map<?,?>)m).get("text");
			return txt != null && !txt.toString().isEmpty();
		});
		assertThat("At least one message should have non-empty text", hasText);
	}
}
