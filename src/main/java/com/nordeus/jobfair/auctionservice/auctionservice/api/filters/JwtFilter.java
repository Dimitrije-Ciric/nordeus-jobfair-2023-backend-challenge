package com.nordeus.jobfair.auctionservice.auctionservice.api.filters;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.HashMapConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final HashMapConverter hashMapConverter;

    private final AuctionsUserService auctionsUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String authSchema = authHeader.split(" ", 2)[0],
                jwtToken = authHeader.split(" ", 2)[1];

        if (!authSchema.equals("Bearer")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Long userId;
        Map<String, Object> userDetails;
        try {
            Map<String, Object> jwtPayload = this.parseJwtPayload(jwtToken);

            userId = Long.valueOf(jwtPayload.get("user_id").toString());
            userDetails = (Map<String, Object>) jwtPayload.get("user_details");
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        request.setAttribute("auctions_user", this.auctionsUserService.updateUser(userId, userDetails));

        filterChain.doFilter(request, response);
    }

    private Map<String, Object> parseJwtPayload(String token) {
        Map<String, Object> parsedJwtToken = this.hashMapConverter.convertToEntityAttribute(token);
        return (Map<String, Object>) parsedJwtToken.get("payload");
    }
}
