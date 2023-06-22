package com.zerototen.savegame.security;

import com.zerototen.savegame.domain.UserDetailsImpl;
import com.zerototen.savegame.domain.dto.TokenDto;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.RefreshToken;
import com.zerototen.savegame.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;  //30분
    private static final long REFRESH_TOKEN_EXPRIRE_TIME = 1000 * 60 * 60 * 24 * 7;  //7일

    private final Key key;

    private final RefreshTokenRepository refreshTokenRepository;


    // 암호화
    public TokenProvider(@Value("${jwt.secret}") String secretKey,
        RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public TokenDto generateTokenDto(Member member) {
        long now = (new Date().getTime());

        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
            .setSubject(member.getId().toString())
            .claim(AUTHORITIES_KEY, member.getUserRole().toString())
            .setExpiration(accessTokenExpiresIn)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        String refreshToken = Jwts.builder()
            .setExpiration(new Date(now + REFRESH_TOKEN_EXPRIRE_TIME))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        RefreshToken refreshTokenObject = RefreshToken.builder()
            .id(member.getId().toString())
            .member(member)
            .keyValue(refreshToken)
            .build();

        refreshTokenRepository.save(refreshTokenObject);

        return TokenDto.builder()
            .grantType(BEARER_PREFIX)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();

    }

    public Member getMemberFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
            isAssignableFrom(authentication.getClass())) {
            return null;
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getMember();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    @Transactional(readOnly = true)
    public RefreshToken isPresentRefreshToken(Member member) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByMember(member);
        return optionalRefreshToken.orElse(null);
    }

    @Transactional
    public boolean deleteRefreshToken(Member member) { // TODO: return의 t/f를 반대로 하는게?
        RefreshToken refreshToken = isPresentRefreshToken(member);
        if (null == refreshToken) {
            return true;
        }
        refreshTokenRepository.delete(refreshToken);
        return false;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = getAccessToken(request);
        if (token == null) {
            return null;
        } else {
            Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            User principal = new User(claims.getSubject(), "@", authorities);

            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        }
    }

    public Long getMemberIdByToken(String accessToken) {
        String token = "";
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
            token = accessToken.substring(7);
        } else {
            return null;
        }
        Claims claims;
        try {
            claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            return null;
        }

        return Long.parseLong(claims.getSubject());
    }

    private String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public String getMemberFromExpiredAccessToken(HttpServletRequest request) throws ParseException {
        String jwt = getAccessToken(request);

        Base64.Decoder decoder = Base64.getUrlDecoder();
        assert jwt != null;
        String[] parts = jwt.split("\\.");
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new String(decoder.decode(parts[1])));
        return jsonObject.get("sub").toString();
    }

    private Member validateMember(HttpServletRequest request) {
        if (!this.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return this.getMemberFromAuthentication();
    }

    public ResponseDto<?> validateCheck(HttpServletRequest request) {

        // RefreshToken 및 Authorization 유효성 검사
        if (null == request.getHeader("RefreshToken") || null == request.getHeader("Authorization")) {
            return ResponseDto.fail("로그인이 필요합니다.");
        }
        Member member = validateMember(request);
        // token 정보 유효성 검사
        if (null == member) {
            return ResponseDto.fail("Token이 유효하지 않습니다.");
        }
        return ResponseDto.success(member);
    }

}