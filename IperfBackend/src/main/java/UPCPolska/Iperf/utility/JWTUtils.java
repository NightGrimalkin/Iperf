package UPCPolska.Iperf.utility;

import UPCPolska.Iperf.data.archetypes.Role;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.net.HttpCookie;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JWTUtils {

    public static String createJwtToken(HttpServletRequest request, User user, boolean isAccess) {
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        String token;
        if (isAccess) {
            token = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .withIssuer(request.getRequestURL().toString())
                    .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .sign(algorithm);
        } else {
            token = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 31))
                    .withIssuer(request.getRequestURL().toString())
                    .sign(algorithm);
        }
        return token;
    }

    public static String createJwtTokenUserArchetype(HttpServletRequest request, UPCPolska.Iperf.data.archetypes.User user) {
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .sign(algorithm);
        return access_token;
    }

    public static Optional<String> readCookie(HttpServletRequest request, String key) {
        return Arrays.stream(request.getCookies())
                .filter(c -> key.equals(c.getName()))
                .map(Cookie::getValue)
                .findAny();
    }

    public static Cookie createRefreshCookie(String refresh_token) {
        Cookie jwtRefreshCookie = new Cookie("refresh_token", refresh_token);
        jwtRefreshCookie.setMaxAge( 60 * 60 * 24 * 31);
        jwtRefreshCookie.setHttpOnly(true);
        jwtRefreshCookie.setSecure(true);
        jwtRefreshCookie.setPath("/");
        return jwtRefreshCookie;
    }

}
