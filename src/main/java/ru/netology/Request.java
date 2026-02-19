package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private Map<String, List<String>> queryParams;
    private final Map<String, String> headers;
    private final String body;
    private Map<String, List<String>> postParams;

    public Request(String method, String path, String query, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;

        try {
            if (query != null && !query.isEmpty()) {
                this.queryParams = URLEncodedUtils.parse(query, StandardCharsets.UTF_8).stream()
                        .collect(Collectors.groupingBy(NameValuePair::getName,
                                Collectors.mapping(NameValuePair::getValue, Collectors.toList())
                        ));
            } else {
                this.queryParams = new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.queryParams = new HashMap<>();
        }

        this.headers = headers;
        this.body = body;

        this.postParams = new HashMap<>();
        if (body != null && headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
            String[] params = body.split("&");

            for (String param : params) {
                String[] parts = param.split("=",2);
                String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

                postParams.computeIfAbsent(name, v -> new ArrayList<>()).add(value);
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        List<String> queryParam = queryParams.get(name);
        return (queryParam != null && !queryParam.isEmpty()) ? queryParam.get(0) : null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    public List<String> getPostParam(String name) {
        return postParams.get(name);
    }
}
