package com.zerototen.savegame.member.domain.OAuth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerototen.savegame.member.domain.controller.dto.GoogleOAuthTokenDto;
import com.zerototen.savegame.member.domain.controller.dto.GoogleUserInfoDto;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth {

  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;
  @Value("${google.login.url}")
  private String googleLoginUrl;
  @Value("${google.token.url}")
  private String GOOGLE_TOKEN_REQUEST_URL;
  @Value("${google.userinfo.url}")
  private String GOOGLE_USERINFO_REQUEST_URL;
  @Value("${google.client.id}")
  private String googleClientId;
  @Value("${google.client.secret}")
  private String googleClientSecret;
  @Value("${google.redirect.url}")
  private String googleRedirectUrl;


  public String getOauthRedirectURL() {
    String reqUrl = googleLoginUrl + "/o/oauth2/v2/auth?client_id=" + googleClientId + "&redirect_uri=" + googleRedirectUrl
        + "&response_type=code&scope=email%20profile%20openid&access_type=offline";

    return reqUrl;
  }

  public ResponseEntity<String> requestAccessToken(String code) {
    RestTemplate restTemplate = new RestTemplate();
    Map<String, Object> params = new HashMap<>();
    params.put("code", code);
    params.put("client_id", googleClientId);
    params.put("client_secret", googleClientSecret);
    params.put("redirect_uri", googleRedirectUrl);
    params.put("grant_type", "authorization_code");

    ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_REQUEST_URL,
        params, String.class);

    if (responseEntity.getStatusCode() == HttpStatus.OK) {
      return responseEntity;
    }
    return null;
  }

  public GoogleOAuthTokenDto getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
    System.out.println("response.getBody() = " + response.getBody());
      GoogleOAuthTokenDto googleOAuthTokenDto = objectMapper.readValue(response.getBody(), GoogleOAuthTokenDto.class);
    return googleOAuthTokenDto;
  }

  public ResponseEntity<String> requestUserInfo(GoogleOAuthTokenDto oAuthToken) {

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
    System.out.println("response.getBody() = " + response.getBody());
       return response;
  }

  public GoogleUserInfoDto getUserInfo(ResponseEntity<String> response) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    GoogleUserInfoDto googleUserInfoDto = objectMapper.readValue(response.getBody(), GoogleUserInfoDto.class);
    return googleUserInfoDto;
  }
}
