package com.zerototen.savegame.config.Jwt;

import static com.zerototen.savegame.exception.ErrorCode.LOGIN_CHECK_FAIL;

import com.zerototen.savegame.entity.Member;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Component("userDetailsService")
@RequiredArgsConstructor
public class JwtMemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String email) {
        return memberRepository.findOneWithAuthoritiesByEmail(email)
            .map(this::createMember)
            .orElseThrow(() -> new CustomException(LOGIN_CHECK_FAIL));
    }

    private org.springframework.security.core.userdetails.User createMember(Member member) {

        List<GrantedAuthority> grantedAuthorities = member.getAuthorities().stream()
            .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(member.getEmail(),
            member.getPassword(),
            grantedAuthorities
        );
    }
}