package com.be.OAuth2WithJWT.service;

import com.be.OAuth2WithJWT.domain.Member;
import com.be.OAuth2WithJWT.dto.*;
import com.be.OAuth2WithJWT.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        Member existData = memberRepository.findByUsername(username);
        if (existData == null) {
            Member member = Member.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .role("ROLE_USER")
                    .build();
            memberRepository.save(member);

            UserDTO userDTO = new UserDTO();
            userDTO.setName(oAuth2Response.getName());
            userDTO.setUsername(username);
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else {
            existData.update(oAuth2Response.getEmail(), oAuth2Response.getName());

            memberRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setName(existData.getName());
            userDTO.setUsername(existData.getUsername());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        }




    }
}
