package com.kyawhsan.notiva.security;

import com.kyawhsan.notiva.entity.User;
import com.kyawhsan.notiva.auth.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuthService googleOAuthService;
    private final JwtService jwtService;

    @Value("${app.oauth2.frontend-callback-url}")
    private String frontendCallbackUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String googleId = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String displayName = oauthUser.getAttribute("name");
        String avatarUrl = oauthUser.getAttribute("picture");

        User user = googleOAuthService.processGoogleUser(googleId, email, displayName, avatarUrl);

        if (!user.isEnabled()) {
            redirectToFailure(request, response, "Account is disabled");
            return;
        }

        String accessToken = jwtService.generateToken(new CustomUserDetails(user));

        invalidateOAuthSession(request);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                .fragment("access_token={token}").buildAndExpand(accessToken).toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void redirectToFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            String message) throws IOException {

        invalidateOAuthSession(request);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                .queryParam("error", message).build().encode().toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void invalidateOAuthSession(
            HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }
}