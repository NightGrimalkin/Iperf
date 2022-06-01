package UPCPolska.Iperf.security.filter;

import UPCPolska.Iperf.service.RequestService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final Bucket bucket;

    public CustomAuthorizationFilter() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (bucket.tryConsume(1)) {
            if (request.getServletPath().equals("/login") || request.getServletPath().equals("/api/token/refresh")) {
                filterChain.doFilter(request, response);
            } else {
                String authorizationHeader = request.getHeader(AUTHORIZATION);

                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    try {
                        String token = authorizationHeader.substring("Bearer ".length());
                        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                        JWTVerifier verifier = JWT.require(algorithm).build();
                        DecodedJWT decodedJWT = verifier.verify(token);
                        String username = decodedJWT.getSubject();
                        String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        stream(roles).forEach(role -> {
                            authorities.add(new SimpleGrantedAuthority(role));
                        });
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        filterChain.doFilter(request, response);
                    } catch (Exception exception) {
                        log.error("Error logging in:{}", exception.getMessage());
                        response.setHeader("error", exception.getMessage());
                        response.setStatus(FORBIDDEN.value());
                        Map<String, String> error = new HashMap<>();
                        error.put("authorization_error", exception.getMessage());
                        response.setContentType(APPLICATION_JSON_VALUE);
                        new ObjectMapper().writeValue(response.getOutputStream(), error);
                    }
                } else {
                    response.setContentType(APPLICATION_JSON_VALUE);
                    response.setStatus(FORBIDDEN.value());
                    new ObjectMapper().writeValue(response.getOutputStream(), "Authorization is missing, try logging in");
                }
            }
        } else {
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(TOO_MANY_REQUESTS.value());
            new ObjectMapper().writeValue(response.getOutputStream(), "too much requests");
        }
    }
}