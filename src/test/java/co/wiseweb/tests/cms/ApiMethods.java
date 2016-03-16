package co.wiseweb.tests.cms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApiMethods {

	static long TIMEOUT = 60000;
	static long DELAY_FOR_REQUEST = 5000;

	String apiKey = ConfigProperties.getProperty("wl_api_key");
	String apiUrl = ConfigProperties.getProperty("wl_api_url");
	String wlGui = ConfigProperties.getProperty("wl_gui_url");

	String taxonomyUrl = apiUrl + "taxonomies/taxonomy";
	String elementUrl = apiUrl + "elements/element";
	String menuUrl = apiUrl + "menus/menu";

	String newTaxUrl = "taxonomy" + UUID.randomUUID().toString().substring(0, 3);
	String newElemUrl = "element" + UUID.randomUUID().toString().substring(0, 3);

	protected String elementId;
	protected String taxonomyId;

	protected HttpRequestFactory factory = new ApacheHttpTransport().createRequestFactory();
	protected HttpClient client = ApacheHttpTransport.newDefaultHttpClient();


	static String CONTENT_TYPE = ContentType.APPLICATION_JSON.toString();

	/**
	 * Create new taxonomy via API.
	 *
	 * @return String entity id
	 */
	protected String postTaxonomy(String name, int statusCode) throws IOException {

		JSONObject body = new JSONObject()
				.put("url", newTaxUrl)
				.put("name", name)
				.put("meta_info", new JSONObject().put("title", "testTitle"))
				.put("content", "Content for taxonomy")
				.put("custom_fields", new JSONObject().put("check", "taxonomy custom fields"));

		HttpResponse response = factory
				.buildPostRequest(
						new GenericUrl(taxonomyUrl), ByteArrayContent.fromString(CONTENT_TYPE, body.toString()))
				.setHeaders(new HttpHeaders().setAuthorization(apiKey))
				.execute();

		assertThat(response.getStatusCode()).isEqualTo(statusCode);
		JsonNode responseAsMap = new ObjectMapper().readTree(response.getContent());

		return responseAsMap.findValue("id").asText();
	}

	/**
	 * Create new element via API.
	 *
	 * @return String entity id
	 */
	protected String postElementToTaxonomy(String content, String taxonomyId, int statusCode) throws IOException {

		JSONObject body = new JSONObject()
				.put("url", newElemUrl)
				.put("name", "Test API name")
				.put("content", content)
				.put("meta_info", new JSONObject().put("title", "ElementTitle"))
				.put("keywords", new String[]{"testing", "API"})
				.put("custom_fields", new JSONObject().put("check", "element custom field"))
				.put("taxonomies", taxonomyId);

		HttpResponse response = factory
				.buildPostRequest(
						new GenericUrl(elementUrl), ByteArrayContent.fromString(CONTENT_TYPE, body.toString())
				)
				.setThrowExceptionOnExecuteError(false)
				.setHeaders(new HttpHeaders().setAuthorization(apiKey))
				.execute();

		assertThat(response.getStatusCode()).isEqualTo(statusCode);
		JsonNode responseAsMap = new ObjectMapper().readTree(response.getContent());

		if (response.getStatusCode() == 201) {
			return responseAsMap.findValue("id").asText();
		}

		return null;
	}

	/**
	 * Sending request to delete element by id.
	 */
	protected void deleteElement(String id, int statusCode) throws URISyntaxException, IOException {

		HttpDelete request = new HttpDelete();
		request.setHeader("Authorization", apiKey);
		request.setURI(new URI(elementUrl + "/" + id));

		assertThat(client.execute(request).getStatusLine().getStatusCode()).isEqualTo(statusCode);

		elementId = null;
	}

	/**
	 * Sending request to delete taxonomy by id.
	 */
	protected void deleteTaxonomy(String id, int statusCode) throws URISyntaxException, IOException {

		HttpDelete request = new HttpDelete();
		request.setHeader("Authorization", apiKey);
		request.setURI(new URI(taxonomyUrl + "/" + id));

		assertThat(client.execute(request).getStatusLine().getStatusCode()).isEqualTo(statusCode);
		taxonomyId = null;
	}

	/**
	 * Return content from taxonomy page.
	 */
	protected String getTaxonomyPage() throws TimeoutException, IOException, InterruptedException, HttpException {

		HttpRequest request = factory
				.buildGetRequest(new GenericUrl(wlGui + newTaxUrl))
				.setThrowExceptionOnExecuteError(false);
		HttpResponse response = sendRequestWithDelay(request, DELAY_FOR_REQUEST);

		return getContent(response.getContent());
	}

	/**
	 * Return content from element page.
	 */
	protected String getElementPage() throws IOException, TimeoutException, InterruptedException, HttpException {

		HttpRequest request = factory
				.buildGetRequest(new GenericUrl(wlGui + newTaxUrl + "/" + newElemUrl))
				.setThrowExceptionOnExecuteError(false);

		HttpResponse response = sendRequestWithDelay(request, DELAY_FOR_REQUEST);

		return getContent(response.getContent());
	}

	/**
	 * Update taxonomy by id.
	 */
	protected void updateTaxonomy(String id, String name, int statusCode) throws IOException {

		JSONObject body = new JSONObject()
				.put("url", newTaxUrl)
				.put("name", name)
				.put("content", "content for taxonomy")
				.put("meta_info", new JSONObject().put("title", "newTitle"));

		HttpResponse response = factory
				.buildPutRequest(
						new GenericUrl(taxonomyUrl + "/" + id),
						ByteArrayContent.fromString(CONTENT_TYPE, body.toString())
				)
				.setHeaders(new HttpHeaders().setAuthorization(apiKey))
				.setThrowExceptionOnExecuteError(false)
				.execute();

		assertThat(response.getStatusCode()).isEqualTo(statusCode);
	}

	/**
	 * Update element by id.
	 */
	protected void updateElement(String elementId, String taxId, String content, int statusCode) throws IOException {
		JSONObject body = new JSONObject()
				.put("url", newElemUrl)
				.put("name", "name for element")
				.put("content", content)
				.put("meta_info", new JSONObject().put("title", "newTitle"))
				.put("taxonomies", taxId);

		HttpResponse response = factory
				.buildPutRequest(
						new GenericUrl(elementUrl + "/" + elementId),
						ByteArrayContent.fromString(CONTENT_TYPE, body.toString())
				)
				.setHeaders(new HttpHeaders().setAuthorization(apiKey))
				.setThrowExceptionOnExecuteError(false)
				.execute();

		assertThat(response.getStatusCode()).isEqualTo(statusCode);
	}

	/**
	 * Return random string.
	 */
	protected String getRandomString() {

		return UUID.randomUUID().toString();
	}

	/**
	 * Sending request with some delay before take ok response.
	 */
	protected HttpResponse sendRequestWithDelay(HttpRequest request, long delay) throws IOException,
			InterruptedException, HttpException, TimeoutException {

		long startTime = System.currentTimeMillis();
		HttpResponse response;

		do {
			if (startTime + TIMEOUT < System.currentTimeMillis()) {

				throw new TimeoutException("Element was not loaded before time is out!");
			}

			response = request.execute();

			if (response.getStatusCode() == 404) {
				Thread.sleep(delay);
			} else if (response.getStatusCode() != 200) {

				throw new HttpException(
						"Response code is: " + response.getStatusCode()
								+ " message is: " + response.getStatusMessage()
				);
			}
		} while (response.getStatusCode() != 200);

		return response;
	}

	/**
	 * Post menu via API.
	 */
	protected String postMenu(String name, int statusCode) throws IOException {

		JSONObject body = new JSONObject()
				.put("location", "left")
				.put("name", name);

		HttpRequest request = factory
				.buildPostRequest(new GenericUrl(menuUrl), ByteArrayContent.fromString(CONTENT_TYPE, body.toString()))
				.setHeaders(new HttpHeaders().setAuthorization(apiKey));

		HttpResponse response = request.execute();
		assertThat(response.getStatusCode()).isEqualTo(statusCode);

		JsonNode responseAsMap = new ObjectMapper().readTree(response.getContent());

		if (response.getStatusCode() == 201) {

			return responseAsMap.findValue("id").asText();
		} else
			return null;
	}

	/**
	 * Delete menu by id.
	 */
	protected void deleteMenu(String id, int statusCode) throws IOException {

		HttpResponse response = factory.buildDeleteRequest(new GenericUrl(menuUrl + "/" + id)).execute();
		assertThat(response.getStatusCode()).isEqualTo(statusCode);
	}

	/**
	 * Read response and return it.
	 */
	private String getContent(InputStream response) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(response));
		String content;

		while ((content = reader.readLine()) != null) {
			stringBuilder.append(content);
		}
		reader.close();

		return stringBuilder.toString();
	}
}
