package de.codecentric.boot.admin.io.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

public class InMemoryFileResourceTest {
	@Test
	public void testResource() throws IOException {
		InMemoryFileResource resource = new InMemoryFileResource("test.txt", "description",
				new byte[] {}, 1234L);
		assertThat(resource.getFilename()).isEqualTo("test.txt");
		assertThat(resource.getDescription()).contains("description");
		assertThat(resource.lastModified()).isEqualTo(1234L);
	}
}
