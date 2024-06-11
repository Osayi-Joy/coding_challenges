package org.osayijoy.url_shortener.service;



import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class UrlShortener {
    private static final String BASE_URL = "http://base.url/";
    private static final int URL_LENGTH = 8;
    private static final AtomicLong counter = new AtomicLong(0);
    private Lock lock = new ReentrantLock();

    private final Map<String, String> urlMappingByShortUrl = new HashMap<>();
    private final Map<String, String> urlMappingByLongUrl = new HashMap<>();
    private final Map<String, String> urlMappingByKeyword = new ConcurrentHashMap<>();

    public String shortenUrl(String longUrl) {
        validateUrl(longUrl);
        lock.lock();
        try {
        String existingShortUrl = urlMappingByLongUrl.get(longUrl);
        if (existingShortUrl != null) {
            return BASE_URL + existingShortUrl;
        }

        String shortUrl;
        do {
            shortUrl = generateShortUrl(longUrl);
        } while (urlMappingByShortUrl.containsKey(shortUrl));

        urlMappingByShortUrl.put(shortUrl, longUrl);
        urlMappingByLongUrl.put(longUrl, shortUrl);

        return BASE_URL + shortUrl;
        } finally {
            lock.unlock();
        }
    }




//        public String shortenUrl(String longUrl) {
//            validateUrl(longUrl);
//
//            String existingShortUrl = urlMappingByLongUrl.get(longUrl);
//            if (existingShortUrl != null) {
//                return BASE_URL + existingShortUrl;
//            }
//
//            String shortUrl = urlMappingByShortUrl
//                    .computeIfAbsent(longUrl, UrlShortener::generateShortUrl);
//
//            urlMappingByLongUrl.put(longUrl, shortUrl);
//
//            return BASE_URL + shortUrl;
//        }


        public String shortenUrl(String longUrl, String keyword) {

        validateUrl(longUrl);
        lock.lock();
        try {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }

        String existingLongUrl = urlMappingByKeyword.get(keyword);
        if (existingLongUrl != null) {
            throw new IllegalArgumentException("Keyword is already in use");
        }

        String shortUrl = BASE_URL+keyword;

            String existingKeyword = urlMappingByLongUrl.get(longUrl);
        if (existingKeyword != null && existingKeyword.equals(keyword)) {
            return shortUrl;
        }

        urlMappingByKeyword.put(keyword, longUrl);
        urlMappingByShortUrl.put(shortUrl, longUrl);
        urlMappingByLongUrl.put(longUrl, keyword);

        return shortUrl;

        } finally {
            lock.unlock();
        }
    }

    private static String generateShortUrl(String longUrl) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(longUrl.getBytes(StandardCharsets.UTF_8));
            md.update(String.valueOf(counter.getAndIncrement()).getBytes(StandardCharsets.UTF_8));
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating short URL", e);
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0, URL_LENGTH);
    }



    private void validateUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }
    }

    public String getLongUrl(String shortUrl) {
        String key = shortUrl.replace(BASE_URL, "");
        String longUrl = urlMappingByShortUrl.get(key);
        if (longUrl == null) {
            throw new IllegalArgumentException("Short URL not found");
        }
        return longUrl;
    }


    public static void main(String[] args) {
        UrlShortener urlShortener = new UrlShortener();

        String longUrl1 = "http://base.url/java-tutorial";
        String longUrl2 = "http://base.url/python-tutorial";

        String shortUrl1 = urlShortener.shortenUrl(longUrl1);
        String shortUrl2 = urlShortener.shortenUrl(longUrl2);

        System.out.println("Short URL 1: " + shortUrl1);
        System.out.println("Short URL 2: " + shortUrl2);

        System.out.println("Retrieved Long URL 1: " + urlShortener.getLongUrl(shortUrl1));
        System.out.println("Retrieved Long URL 2: " + urlShortener.getLongUrl(shortUrl2));
    }
}
