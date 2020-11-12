package com.mavuno.famers.union.mavuno.configs;


import com.mavuno.famers.union.mavuno.models.ResponseModel;
import com.mavuno.famers.union.mavuno.services.UserService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.mavuno.famers.union.mavuno.configs.Utils.isTokenExpired;


@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private UserService userDetailsService;



    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = httpServletRequest.getHeader("Authorization");

        String token = null;
        String email = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);

            if (isTokenExpired(authorizationHeader)) {
                (httpServletResponse).setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getOutputStream().println(new ResponseModel("03", "Token expired").toString());

                MDC.clear();
                return;
            }


            email = jwtUtility.extractUsername(token);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);


            if (!jwtUtility.validateToken(token, userDetails)) {
                (httpServletResponse).setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getOutputStream().println(new ResponseModel("04", "Invalid request").toString());
                MDC.clear();
                return;
            }


            if (jwtUtility.validateToken(token, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }

}
