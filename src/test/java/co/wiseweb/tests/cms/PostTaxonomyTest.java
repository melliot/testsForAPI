package co.wiseweb.tests.cms;

import org.apache.http.HttpException;
import org.junit.After;
import org.junit.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class PostTaxonomyTest extends ApiMethods{

	@After
	public void dropDown() throws IOException, URISyntaxException, InterruptedException {
		if (taxonomyId != null) {
			deleteTaxonomy(taxonomyId, 204);
		}
	}

	@Test
	public void checkCreatingAndUpdatingTaxonomy() throws IOException, InterruptedException,
			TimeoutException, HttpException {

		String uniqName = getRandomString();
		taxonomyId = postTaxonomy("old name", 201);

		updateTaxonomy(taxonomyId, uniqName, 204);
		assertThat(getTaxonomyPage()).contains(uniqName);
	}

	@Test
	public void tryToUpdateNonExistentTaxonomy() throws TimeoutException, IOException {
		String uniqName = getRandomString();
		String nonExistentId = getRandomString().substring(0, 5);

		updateTaxonomy(nonExistentId, uniqName, 404);
	}

	@Test
	public void tryToDeleteNonExistentTaxonomy() throws IOException, URISyntaxException {
		String nonExistentId = getRandomString().substring(0, 5);
		deleteTaxonomy(nonExistentId, 404);
	}
}
