package likelion14th.lte.youtube.client;

import tools.jackson.databind.JsonNode;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YouTubeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api-key}")
    private String apiKey;

    @Value("${youtube.api-base}")
    private String apiBase;

    public JsonNode searchVideoRaw(String query, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));

        String url = UriComponentsBuilder
                .fromUriString(apiBase + "/search")
                .queryParam("part", "snippet")
                .queryParam("q", query)
                .queryParam("maxResults", safeLimit)
                .queryParam("key", apiKey)
                .build()
                .toUriString();

        return request(url);
    }

    public JsonNode getVideoRaw(String videoId) {
        String url = UriComponentsBuilder
                .fromUriString(apiBase + "/videos")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("id", videoId)
                .queryParam("key", apiKey)
                .build()
                .toUriString();

        return request(url);
    }

    private JsonNode request(String url) {
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    JsonNode.class
            );

            if(!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new GeneralException(ErrorCode.YOUTUBE_API_FAILED);
            }

            return response.getBody();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.YOUTUBE_API_FAILED);
        }


    }

}