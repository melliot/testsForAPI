package co.wiseweb.tests.cms;
import org.junit.Test;

import java.io.IOException;

public class ActionsWithMenu extends ApiMethods{

	@Test
	public void checkPostMenu() throws IOException {
		postMenu("Menu name", 201);
	}

	@Test
	public void deleteMenu() throws IOException {
		String id = postMenu("menu name", 201);
		deleteMenu(id, 201);
	}
}
