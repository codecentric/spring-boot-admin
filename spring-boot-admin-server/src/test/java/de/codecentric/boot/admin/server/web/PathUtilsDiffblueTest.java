package de.codecentric.boot.admin.server.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PathUtilsDiffblueTest {

	/**
	 * Test {@link PathUtils#normalizePath(String)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link PathUtils#normalizePath(String)}
	 */
	@Test
	public void testNormalizePath_whenNull_thenReturnNull() {
		// Arrange, Act and Assert
		assertNull(PathUtils.normalizePath(null));
	}

	/**
	 * Test {@link PathUtils#normalizePath(String)}.
	 * <ul>
	 * <li>When {@code Path}.</li>
	 * <li>Then return {@code /Path}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link PathUtils#normalizePath(String)}
	 */
	@Test
	public void testNormalizePath_whenPath_thenReturnPath() {
		// Arrange, Act and Assert
		assertEquals("/Path", PathUtils.normalizePath("Path"));
	}

	/**
	 * Test {@link PathUtils#normalizePath(String)}.
	 * <ul>
	 * <li>When {@code ///}.</li>
	 * <li>Then return {@code /}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link PathUtils#normalizePath(String)}
	 */
	@Test
	public void testNormalizePath_whenSlashSlashSlash_thenReturnSlash() {
		// Arrange, Act and Assert
		assertEquals("/", PathUtils.normalizePath("///"));
	}

	/**
	 * Test {@link PathUtils#normalizePath(String)}.
	 * <ul>
	 * <li>When {@code /}.</li>
	 * <li>Then return empty string.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link PathUtils#normalizePath(String)}
	 */
	@Test
	public void testNormalizePath_whenSlash_thenReturnEmptyString() {
		// Arrange, Act and Assert
		assertEquals("", PathUtils.normalizePath("/"));
	}

}
