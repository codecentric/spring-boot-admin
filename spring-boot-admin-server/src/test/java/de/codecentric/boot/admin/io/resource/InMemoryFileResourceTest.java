package de.codecentric.boot.admin.io.resource;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class InMemoryFileResourceTest {
	@Test
	public void testResource() throws IOException {
		InMemoryFileResource resource = new InMemoryFileResource("test.txt", "description",
				new byte[] {}, 1234L);
		assertThat(resource.getFilename(), is("test.txt"));
		assertThat(resource.getDescription(), containsString(("description")));
		assertThat(resource.lastModified(), is(1234L));
	}
}
