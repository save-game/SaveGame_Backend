package com.zerototen.savegame.service;

import com.zerototen.savegame.client.KakaoClient;
import com.zerototen.savegame.domain.KakaoInfo;
import com.zerototen.savegame.domain.dto.KakaoToken;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final KakaoClient client;

    @Value("${kakao.authUrl}")
    private String kakaoAuthUrl;

    @Value("${kakao.userApiUrl}")
    private String kakaoUserApiUrl;

    @Value("${kakao.restapiKey}")
    private String restapiKey;

    @Value("${kakao.redirectUrl}")
    private String redirectUrl;

    public KakaoInfo getInfo(final String code) {
        final KakaoToken token = getToken(code);  // 로그인 과정에서 얻은 code를 이용하여 accessToken, refreshToken을 획득한다
        log.debug("token = {}", token);
        try {
            // 위에서 얻은 accessToken으로 카카오 API(유저 정보 획득) 호출
            return client.getInfo(new URI(kakaoUserApiUrl), token.getTokenType() + " " + token.getAccessToken());
        } catch (Exception e) {
            log.error("something error..", e);
            return KakaoInfo.fail();
        }
    }

    private KakaoToken getToken(final String code) {
        try {
            return client.getToken(new URI(kakaoAuthUrl), restapiKey, redirectUrl, code, "authorization_code");
        } catch (Exception e) {
            log.error("Something error..", e);
            return KakaoToken.fail();
        }
    }
}