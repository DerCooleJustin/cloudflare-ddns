import Records.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class NetUtils {
    private static HttpClient httpClient = HttpClient.newHttpClient();

    public static String getPublicV4Address() throws IOException, InterruptedException {
        // Build the curl process to force IPv4
        ProcessBuilder curlBuilder = new ProcessBuilder("curl", "-4s", "https://ifconfig.me");
        Process process = curlBuilder.start();

        // Wait for curl to finish
        if (process.waitFor() != 0) {
            return null; // Curl failed
        }

        // Read the output
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    public static String getPublicV6Address() throws IOException, InterruptedException {
        // Build the curl process to force IPv4
        ProcessBuilder curlBuilder = new ProcessBuilder("curl", "-6s", "https://ifconfig.me");
        Process process = curlBuilder.start();

        // Wait for curl to finish
        if (process.waitFor() != 0) {
            return null; // Curl failed
        }

        // Read the output
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    public static String httpGet(String uri, ArrayList<Header> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(uri)).GET();
        for (Header header : headers) requestBuilder = requestBuilder.header(header.name(), header.value());
        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String httpGet(String uri, Header header) throws IOException, InterruptedException {
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(header);
        return httpGet(uri, headers);
    }

    public static String httpPatch(String uri, ArrayList<Header> headers, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest
                .newBuilder(URI.create(uri))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body));

        for (Header header : headers) requestBuilder.header(header.name(), header.value());

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}