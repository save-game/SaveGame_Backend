package com.zerototen.savegame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerototen.savegame.domain.UserDetailsImpl;
import com.zerototen.savegame.domain.dto.SignupInfoDto;
import com.zerototen.savegame.domain.dto.TokenDto;
import com.zerototen.savegame.domain.dto.kakao.KakaoMemberInfoDto;
import com.zerototen.savegame.domain.dto.kakao.OauthLoginResponseDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.type.Authority;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.security.TokenProvider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class KakaoOauthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @Value("${kakao.rest-api-key}")
    String API_KEY;

    @Transactional
    public ResponseDto<?> kakaoLogin(String code, HttpServletResponse response, HttpServletRequest request)
        throws JsonProcessingException {

        log.info(request.getRequestURI());
        log.info(String.valueOf(request.getRequestURL()));

        // 1. 로그인 성공 후 획득한 인가 코드로 accessToken을 요청
        String accessToken = getAccessToken(code, "login");

        // 2. accessToken을 이용해 카카오 API 호출하여 response 받기(사용자 정보 json받아서 id, email, nickname 빼기)
        KakaoMemberInfoDto kakaoMemberInfo = getkakaoMemberInfo(accessToken);

        // 3. 기존에 가입된 이메일인지 확인 후, 가입되지 않은 이메일이면 회원 등록
        Member member = registerKakaoUserIfNeeded(kakaoMemberInfo);

        // 4. 강제 로그인 처리
        forceLogin(member, response);

        return ResponseDto.success(OauthLoginResponseDto.builder()
            .email(member.getEmail())
            .nickname(member.getNickname())
            .profileImageUrl(member.getProfileImageUrl())
            .build());
    }

    // 카카오 로그인 연동 해제
    @Transactional
    public ResponseDto<?> kakaoLogout(String code) throws JsonProcessingException {
        // 1. 받은 code와 state로 accesstoken 받기
        String accessToken = getAccessToken(code, "logout");
        // 2. 로그인연동 해제
        return ResponseDto.success(doLogout(accessToken));
    }

    // 연동 해제 요청 실행
    private String doLogout(String accessToken) throws JsonProcessingException {
        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.add("Content-type", "application/x-www-form-urlencoded");
        logoutHeaders.add("Authorization", "Bearer " + accessToken);

        MultiValueMap<String, String> logoutRequestParam = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, String>> logoutRequest = new HttpEntity<>(logoutRequestParam, logoutHeaders);
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<String> logoutResponse = rt.exchange(
            "https://kapi.kakao.com/v1/user/unlink",
            HttpMethod.POST,
            logoutRequest,
            String.class
        );
        String responseBody = logoutResponse.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("id").asText();
    }

    private String getAccessToken(String code, String mode) throws JsonProcessingException {

        String redirectUrl;
        if (mode.equals("login")) {
//            redirectUrl = "http://localhost:8080/api/auth/kakaologin";
            redirectUrl = "http://13.124.58.137/auth/kakaologin";  // 백엔드 서버
        }
        else {
//            redirectUrl = "http://localhost:8080/api/auth/kakaologout";
            redirectUrl = "http://13.124.58.137/auth/kakaologout";  // 백엔드 서버

        }

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", API_KEY);
        body.add("redirect_uri", redirectUrl);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
            new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            kakaoTokenRequest,
            String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    private KakaoMemberInfoDto getkakaoMemberInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoMemberInfoRequest = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            kakaoMemberInfoRequest,
            String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties").get("nickname").asText();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String imgUrl = null;
        try {
            imgUrl = jsonNode.get("kakao_account").get("profile").get("profile_image_url").asText();
        } catch (Exception ignored) {
        }
        return KakaoMemberInfoDto.builder()
            .id(id)
            .nickname(nickname)
            .email(email)
            .imageUrl(imgUrl)
            .build();
    }

    private Member registerKakaoUserIfNeeded(KakaoMemberInfoDto kakaoMemberInfo) {
        Member member = memberRepository.findByEmail(kakaoMemberInfo.getEmail())
            .orElse(null);

        // 해당 이메일로 가입한 정보가 있는 경우 예외 발생
        if (member != null) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        // 해당 이메일로 가입한 정보가 없는 경우 회원등록
        String nickname = kakaoMemberInfo.getNickname();
        String email = kakaoMemberInfo.getEmail();
        String imageUrl = kakaoMemberInfo.getImageUrl();

        member = new Member(
            SignupInfoDto.builder()
                .email(email)
                .nickname(nickname)
                .imgUrl(imageUrl)
                .role(Authority.ROLE_MEMBER)
                .build());

        return memberRepository.save(member);
    }

    private void forceLogin(Member kakaoUser, HttpServletResponse response) {
        // response header에 token 추가
        TokenDto token = tokenProvider.generateTokenDto(kakaoUser);
        response.addHeader("Authorization", "Bearer " + token.getAccessToken());
        response.addHeader("RefreshToken", token.getRefreshToken());

        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, token.getAccessToken(),
            userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}