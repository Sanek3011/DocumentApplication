package com.hh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hh.dto.DocumentDto;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GeneratorService {

    private static final String WEATHER_API_URL = "http://localhost:8080/api/v1/documents/save/batch";
    private CloseableHttpClient client = HttpClients.createDefault();
    private ObjectMapper mapper = new ObjectMapper();

    public void generateNDocuments(int n) throws URISyntaxException, IOException {
        CloseableHttpResponse execute = client.execute(createWeatherRequest(n));
    }

    private HttpPost createWeatherRequest(int n) throws URISyntaxException, JsonProcessingException {
        String url = new URIBuilder(WEATHER_API_URL)
                .build().toString();
        HttpPost request = new HttpPost(url);
        String s = mapper.writeValueAsString(generateNDtos(n));
        request.addHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setEntity(new StringEntity(s));
        return request;
    }

    private List<DocumentDto> generateNDtos(int n) {
        List<DocumentDto> documentDtos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            documentDtos.add(new DocumentDto(null, "Author:"+i, "Title:"+i, null));
        }
        return documentDtos;
    }

}
