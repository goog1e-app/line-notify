package me.line.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author P-C Lin (a.k.a 高科技黑手)
 */
public class LineNotifyAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(LineNotifyAPI.class);

	private static final String ENDPOINT_SEND_NOTIFICATION = "https://notify-api.line.me/api/notify";

	private static SentResponseBody send(final String accessToken, final UrlEncodedFormEntity httpEntity) {
		SentResponseBody responseBody = new SentResponseBody();

		try ( CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault()) {
			httpClient.start();

			HttpPost httpRequest = new HttpPost(ENDPOINT_SEND_NOTIFICATION);
			httpRequest.setEntity(httpEntity);
			httpRequest.setHeader(
				"Content-Type",
				"application/x-www-form-urlencoded"
			);
			httpRequest.setHeader(
				"Authorization",
				String.format(
					"Bearer %s",
					accessToken
				)
			);
			Future<HttpResponse> future = httpClient.execute(
				httpRequest,
				null
			);

			boolean isParsable = true;
			final HttpResponse httpResponse = future.get();
			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			switch (statusCode) {
				case 200:
					LOGGER.debug("Success");
					break;
				case 400:
					LOGGER.info("Bad request");
					break;
				case 401:
					LOGGER.info("Invalid access token");
					break;
				case 500:
					LOGGER.info("Failure due to server error");
					break;
				default:
					isParsable = false;
					LOGGER.info("Processed over time or stopped");
			}
			if (isParsable) {
				responseBody = JSON_MAPPER.readValue(
					httpResponse.getEntity().getContent(),
					SentResponseBody.class
				);
			}

			httpClient.close();
		} catch (IOException | ExecutionException | CancellationException | InterruptedException exception) {
			LOGGER.info(
				"\n{}",
				exception
			);
		}

		return responseBody;
	}

	/**
	 * JSON-format specific ObjectMapper implementation.
	 */
	public static final ObjectMapper JSON_MAPPER = new JsonMapper();

	/**
	 * ObjectWriter that will serialize objects using the default pretty
	 * printer for indentation
	 */
	public static final ObjectWriter OBJECT_WRITER_WITH_PRETTY_PRINTER = new ObjectMapper().writerWithDefaultPrettyPrinter();

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).
	 *
	 * Connected services will also delete the connection information.
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @return The response body is a JSON object type
	 */
	public static SentResponseBody send(final String accessToken, final String message) {
		return send(
			accessToken,
			message,
			false
		);
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).
	 *
	 * Connected services will also delete the connection information.
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param notificationDisabled true: The user doesn't receive a push
	 * notification when the message is sent; false: The user receives a
	 * push notification when the message is sent (unless they have disabled
	 * push notification in LINE and/or their device).
	 * @return The response body is a JSON object type
	 */
	@SuppressWarnings("UnusedAssignment")
	public static SentResponseBody send(final String accessToken, final String message, final boolean notificationDisabled) {
		SentResponseBody responseBody = new SentResponseBody();

		List<NameValuePair> pairs = new ArrayList<>();
		pairs.add(
			new BasicNameValuePair(
				"message",
				new String(
					message.getBytes(StandardCharsets.UTF_8),
					StandardCharsets.UTF_8
				)
			)
		);
		pairs.add(
			new BasicNameValuePair(
				"notificationDisabled",
				Boolean.toString(notificationDisabled)
			)
		);

		responseBody = send(
			accessToken,
			new UrlEncodedFormEntity(
				pairs,
				StandardCharsets.UTF_8
			)
		);

		return responseBody;
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).Connected services will also delete the connection
	 * information.
	 *
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param stickerPackageId Package ID
	 * @param stickerId Sticker ID
	 * @return The response body is a JSON object type
	 */
	public static SentResponseBody send(final String accessToken, final String message, final int stickerPackageId, final int stickerId) {
		return send(
			accessToken,
			message,
			stickerPackageId,
			stickerId,
			false
		);
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).Connected services will also delete the connection
	 * information.
	 *
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param stickerPackageId Package ID
	 * @param stickerId Sticker ID
	 * @param notificationDisabled true: The user doesn't receive a push
	 * notification when the message is sent; false: The user receives a
	 * push notification when the message is sent (unless they have disabled
	 * push notification in LINE and/or their device).
	 * @return The response body is a JSON object type
	 */
	@SuppressWarnings("UnusedAssignment")
	public static SentResponseBody send(final String accessToken, final String message, final int stickerPackageId, final int stickerId, final boolean notificationDisabled) {
		SentResponseBody responseBody = new SentResponseBody();

		List<NameValuePair> pairs = new ArrayList<>();
		pairs.add(
			new BasicNameValuePair(
				"message",
				message
			)
		);
		pairs.add(
			new BasicNameValuePair(
				"stickerPackageId",
				Integer.toString(stickerPackageId)
			)
		);
		pairs.add(
			new BasicNameValuePair(
				"stickerId",
				Integer.toString(stickerId)
			)
		);
		pairs.add(
			new BasicNameValuePair(
				"notificationDisabled",
				Boolean.toString(notificationDisabled)
			)
		);

		responseBody = send(
			accessToken,
			new UrlEncodedFormEntity(
				pairs,
				StandardCharsets.UTF_8
			)
		);

		return responseBody;
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).Connected services will also delete the connection
	 * information.
	 *
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param imageFullsize Maximum size of 2048×2048px JPEG
	 * @return The response body is a JSON object type
	 */
	public static SentResponseBody send(final String accessToken, final String message, final String imageFullsize) {
		return send(
			accessToken,
			message,
			imageFullsize,
			false
		);
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).Connected services will also delete the connection
	 * information.
	 *
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param imageFullsize Maximum size of 2048×2048px JPEG
	 * @param notificationDisabled true: The user doesn't receive a push
	 * notification when the message is sent; false: The user receives a
	 * push notification when the message is sent (unless they have disabled
	 * push notification in LINE and/or their device).
	 * @return The response body is a JSON object type
	 */
	@SuppressWarnings("UnusedAssignment")
	public static SentResponseBody send(final String accessToken, final String message, final String imageFullsize, final boolean notificationDisabled) {
		SentResponseBody responseBody = new SentResponseBody();

		List<NameValuePair> pairs = new ArrayList<>();
		pairs.add(
			new BasicNameValuePair(
				"message",
				message
			)
		);
		pairs.add(
			new BasicNameValuePair(
				"imageFullsize",
				imageFullsize
			)
		);
		pairs.add(
			new BasicNameValuePair(
				"notificationDisabled",
				Boolean.toString(notificationDisabled)
			)
		);

		responseBody = send(
			accessToken,
			new UrlEncodedFormEntity(
				pairs,
				StandardCharsets.UTF_8
			)
		);

		return responseBody;
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).Connected services will also delete the connection
	 * information.
	 *
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param imageFile Upload a image file to the LINE server. Supported
	 * image format is png and jpeg. There is a limit that you can upload to
	 * within one hour.
	 * @return The response body is a JSON object type
	 */
	public static SentResponseBody send(final String accessToken, final String message, final File imageFile) {
		return send(
			accessToken,
			message,
			imageFile,
			false
		);
	}

	/**
	 * Sends notifications to users or groups that are related to an access
	 * token.If this API receives a status code 401 when called, the access
	 * token will be deactivated on LINE Notify (disabled by the user in
	 * most cases).Connected services will also delete the connection
	 * information.
	 *
	 *
	 * @param accessToken
	 * @param message 1000 characters max
	 * @param imageFile Upload a image file to the LINE server. Supported
	 * image format is png and jpeg. There is a limit that you can upload to
	 * within one hour.
	 * @param notificationDisabled true: The user doesn't receive a push
	 * notification when the message is sent; false: The user receives a
	 * push notification when the message is sent (unless they have disabled
	 * push notification in LINE and/or their device).
	 * @return The response body is a JSON object type
	 */
	public static SentResponseBody send(final String accessToken, final String message, final File imageFile, final boolean notificationDisabled) {
		SentResponseBody responseBody = new SentResponseBody();

		try ( CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault()) {
			httpClient.start();

			HttpPost httpRequest = new HttpPost(ENDPOINT_SEND_NOTIFICATION);
			httpRequest.setEntity(
				MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE).
					addPart(
						"message",
						new StringBody(
							message,
							ContentType.MULTIPART_FORM_DATA
						)
					).
					addPart(
						"imageFile",
						new FileBody(
							imageFile,
							ContentType.DEFAULT_BINARY
						)
					).
					addPart(
						"notificationDisabled",
						new StringBody(
							Boolean.toString(notificationDisabled),
							ContentType.MULTIPART_FORM_DATA
						)
					).
					build()
			);
			httpRequest.setHeader(
				"Content-Type",
				"multipart/form-data"
			);
			httpRequest.setHeader(
				"Authorization",
				String.format(
					"Bearer %s",
					accessToken
				)
			);
			Future<HttpResponse> future = httpClient.execute(
				httpRequest,
				null
			);

			boolean isParsable = true;
			final HttpResponse httpResponse = future.get();
			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			switch (statusCode) {
				case 200:
					LOGGER.debug("Success");
					break;
				case 400:
					LOGGER.info("Bad request");
					break;
				case 401:
					LOGGER.info("Invalid access token");
					break;
				case 500:
					LOGGER.info("Failure due to server error");
					break;
				default:
					isParsable = false;
					LOGGER.info("Processed over time or stopped");
			}
			if (isParsable) {
				responseBody = JSON_MAPPER.readValue(
					httpResponse.getEntity().getContent(),
					SentResponseBody.class
				);
			}

			httpClient.close();
		} catch (IOException | ExecutionException | CancellationException | InterruptedException exception) {
			LOGGER.info(
				"\n{}",
				exception
			);
		}

		return responseBody;
	}
}
