package co.wiseweb.tests.cms;

import com.google.api.client.http.*;
import org.apache.http.HttpException;
import org.junit.After;
import org.junit.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PostElementTest extends ApiMethods {

	@After
	public void dropDown() throws IOException, URISyntaxException {
		if (elementId != null) {
			deleteElement(elementId, 204);
		}
		if (taxonomyId != null) {
			deleteTaxonomy(taxonomyId, 204);
		}
	}

	@Test
	public void checkResponseForInvalidBody() throws URISyntaxException, IOException {

		HttpRequest request = factory
				.buildPostRequest(new GenericUrl(taxonomyUrl), null)
				.setThrowExceptionOnExecuteError(false);
		request.setHeaders(new HttpHeaders().setAuthorization(apiKey));

		assertThat(request.execute().getStatusCode()).isEqualTo(400);
	}

	@Test
	public void checkNotValidAuthorizationKey() throws  IOException {
		String notValidKey = getRandomString().substring(0, 22);

		HttpRequest request = factory
				.buildPostRequest(new GenericUrl(taxonomyUrl), null)
				.setThrowExceptionOnExecuteError(false);
		request.setHeaders(new HttpHeaders().setAuthorization(notValidKey));

		assertThat(request.execute().getStatusCode()).isEqualTo(401);
	}

	@Test
	public void checkPostAndDeleteElement() throws InterruptedException, IOException,
			URISyntaxException, TimeoutException, HttpException {

		String taxonomyText = getRandomString();
		String elementText = getRandomString();

		taxonomyId = postTaxonomy(taxonomyText, 201);
		elementId = postElementToTaxonomy(elementText, taxonomyId, 201);

		assertThat(getTaxonomyPage()).contains(taxonomyText);
		assertThat(getElementPage()).contains(elementText);
	}

	@Test
	public void checkPostElementOnBusyUrl() throws InterruptedException, IOException, URISyntaxException {
		String uniqText = getRandomString();

		taxonomyId = postTaxonomy(uniqText, 201);
		elementId = postElementToTaxonomy(uniqText, taxonomyId, 201);
		postElementToTaxonomy(uniqText, taxonomyId, 400);
	}

	@Test
	public void checkUpdatingElement() throws IOException, URISyntaxException,
			InterruptedException, TimeoutException, HttpException {
		String uniqText = getRandomString();

		taxonomyId = postTaxonomy(uniqText, 201);
		elementId = postElementToTaxonomy("Content for element", taxonomyId, 201);

		updateElement(elementId, taxonomyId, uniqText, 204);
		assertThat(getElementPage()).contains(uniqText);
	}

	@Test
	public void checkDeletingNonExistentElement() throws IOException, URISyntaxException {
		String nonExistentElementId = getRandomString().substring(0, 5);
		deleteElement(nonExistentElementId, 404);
	}
}
