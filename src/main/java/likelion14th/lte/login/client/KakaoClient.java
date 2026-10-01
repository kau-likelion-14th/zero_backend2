package likelion14th.lte.login.client;


import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

@Component
public class KakaoClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    public String getAccessToken(String code){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            JsonNode response = restTemplate.postForObject(tokenUri, request, JsonNode.class);
            if(response == null || !response.has("access_token")){
                throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
            }

            return response.get("access_token").asText();
        } catch(GeneralException e){
            throw e;
        } catch (Exception e){
            throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
        }

    }

    public JsonNode getUserInfo(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if(!response.getStatusCode().is2xxSuccessful() || body == null){
                throw new GeneralException(ErrorCode.KAKAO_API_FAILED);
            }
            return body;
        } catch (GeneralException e) {
            throw e;
        }catch (Exception e){
            throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }
}
