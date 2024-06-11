package org.osayijoy.url_shortener.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.*;


@ExtendWith(MockitoExtension.class)
class UrlShortenerTest {

    private final UrlShortener urlShortener = new UrlShortener();

    @Test
    void testShortenUrl_shouldGenerateShortUrl() {
        String longUrl = "http://example.com";
        String shortUrl = urlShortener.shortenUrl(longUrl);

        assertNotNull(shortUrl);
        assertTrue(shortUrl.startsWith("http://base.url/"));
    }

    @Test
    void testShortenUrl_shouldThrowExceptionForInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> {
            urlShortener.shortenUrl("invalid-url");
        });
    }
    @Test
    void testShortenUrl_shouldThrowExceptionForNullUrl(){
        assertThrows(IllegalArgumentException.class, () ->
                urlShortener.shortenUrl(null));
    }

    @Test
    void testRetrieveUrl_shouldReturnOriginalUrl() {
        String longUrl = "http://example.com";
        String shortUrl = urlShortener.shortenUrl(longUrl);

        String retrievedUrl = urlShortener.getLongUrl(shortUrl);

        assertEquals(longUrl, retrievedUrl);
    }

    @Test
    void testRetrieveUrl_shouldThrowExceptionForNonExistingShortUrl() {
        String shortUrl = "http://base.url/non-existing";

        assertThrows(IllegalArgumentException.class, () -> {
            urlShortener.getLongUrl(shortUrl);
        });
    }


    @Test
    void testShortenUrl_shouldHandleDuplicateUrls() {
        String longUrl = "http://example.com";

        urlShortener.shortenUrl(longUrl);

        String shortUrl1 = urlShortener.shortenUrl(longUrl);
        String shortUrl2 = urlShortener.shortenUrl(longUrl);

        assertEquals(shortUrl1, shortUrl2);
    }

    @Test
    void testShortenUrl_shouldGenerateUniqueShortUrls() {
        String longUrl1 = "http://example1.com";
        String longUrl2 = "http://example2.com";

        String shortUrl1 = urlShortener.shortenUrl(longUrl1);
        String shortUrl2 = urlShortener.shortenUrl(longUrl2);

        assertNotEquals(shortUrl1, shortUrl2);
    }

    @Test
    void testShortenUrl_shouldHandleExtremelyLongUrl() {
        String longUrl = "http://example.com/" + "a".repeat(10000);

        assertDoesNotThrow(() -> urlShortener.shortenUrl(longUrl));
    }

    @Test
    void testShortenUrl_shouldHandleSpecialCharacters() {
        String longUrl = "http://example.com/?q=special%20characters";

        assertDoesNotThrow(() -> urlShortener.shortenUrl(longUrl));
    }

    @Test
    void testShortenUrl_shouldHandleConcurrentAccess() throws InterruptedException, ExecutionException {
        String longUrl = "http://example.com";

        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            Future<String> future = executor.submit(() -> urlShortener.shortenUrl(longUrl));
            results.add(future.get());
        }
        executor.shutdown();

        assertEquals(1, results.size());
    }


    @Test
    void testShortenUrl_shouldHandleConcurrentAccessWithoutGeneratingDuplicates() throws InterruptedException, ExecutionException {
        String[] urls = {"https://www.google.com/", "https://www.google.com",
                "http://www.yahoo.com", "https://www.yahoo.com/", "https://www.amazon.com",
                "https://www.amazon.com/page1.php", "https://www.amazon.com/page2.php",
                "https://www.flipkart.in", "https://www.rediff.com", "https://www.techmeme.com",
                "https://www.techcrunch.com", "https://www.lifehacker.com", "https://www.icicibank.com"};

        ExecutorService executorService = Executors.newFixedThreadPool(urls.length);

        Set<String> shortUrls = new HashSet<>();
        for (String url : urls) {
            Future<String> future = executorService.submit(() -> urlShortener.shortenUrl(url));
            shortUrls.add(future.get());
        }

        executorService.shutdown();

        assertEquals(urls.length, shortUrls.size());
    }

    @Test
    void testShortenUrl_shouldHandleConcurrentAccessWithoutDuplicate() throws InterruptedException, ExecutionException {
        String longUrl = "http://example.com";

        ExecutorService executor = Executors.newFixedThreadPool(1000);
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Future<String> future = executor.submit(() -> urlShortener.shortenUrl(longUrl));
            results.add(future.get());
        }
        executor.shutdown();

        assertEquals(1, results.size());
    }

    @Test
    void testShortenUrl_shouldHandleConcurrentAccess1() throws InterruptedException, ExecutionException {
        String longUrl = "http://example.com";

        Set<String> results = new HashSet<>();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        int taskCount = 1000000;
        for (int i = 0; i < taskCount; i++) {
            completionService.submit(() -> urlShortener.shortenUrl(longUrl));
        }

        for (int i = 0; i < taskCount; i++) {
            Future<String> future = completionService.take();
            String shortUrl = future.get();
            results.add(shortUrl);
        }


        executor.shutdown();

        assertEquals(1, results.size());
    }


    /**
     * Tests for shortening urls using keyword
     */

    @Test
    void testShortenUrl_shouldThrowExceptionForNullKeyword() {
        String longUrl = "http://example.com";
        assertThrows(IllegalArgumentException.class, () -> {
            urlShortener.shortenUrl(longUrl, null);
        });
    }

    @Test
    void testShortenUrl_shouldThrowExceptionForEmptyKeyword() {
        String longUrl = "http://example.com";
        assertThrows(IllegalArgumentException.class, () -> {
            urlShortener.shortenUrl(longUrl, "");
        });
    }

    @Test
    void testShortenUrl_shouldThrowExceptionForDuplicateKeyword() {
        String longUrl = "http://example.com";
        String keyword = "test";
        urlShortener.shortenUrl(longUrl, keyword);

        assertThrows(IllegalArgumentException.class, () -> {
            urlShortener.shortenUrl("http://another-url.com", keyword);
        });
    }

    @Test
    void testShortenUrl_shouldGenerateShortUrlWithKeyword() {
        String longUrl = "http://example.com";
        String keyword = "test";

        String shortUrl = urlShortener.shortenUrl(longUrl, keyword);

        assertEquals("http://base.url/test", shortUrl);
    }



}
