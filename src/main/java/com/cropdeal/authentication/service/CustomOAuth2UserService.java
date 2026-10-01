package com.cropdeal.authentication.service;

import com.cropdeal.authentication.model.User;
import com.cropdeal.authentication.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email).orElseGet(() -> {
                User user = new User();
                user.setEmail(email);
                user.setPassword("oauth2-user");
                user.setRole("DEALER");
                return userRepository.save(user);
            });
        }

        return oauth2User;
    }
}
