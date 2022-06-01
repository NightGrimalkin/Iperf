package UPCPolska.Iperf.controller;

import UPCPolska.Iperf.data.archetypes.Role;
import UPCPolska.Iperf.data.archetypes.User;
import UPCPolska.Iperf.service.UserService;
import UPCPolska.Iperf.utility.JWTUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(
        origins = {"http://localhost:3000/"},
        allowCredentials = "true",
        maxAge = 3600,
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST,
                RequestMethod.DELETE, RequestMethod.PUT,
                RequestMethod.PATCH, RequestMethod.OPTIONS,
                RequestMethod.HEAD, RequestMethod.TRACE}
)
@RequestMapping("/api")
@Slf4j

public class UserResource {
    private final UserService userService;
    private final Bucket bucket;

    public UserResource(UserService userService) {
        Bandwidth limit = Bandwidth.classic(8, Refill.greedy(8, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
        this.userService=userService;
    }


    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/save")
    public ResponseEntity<User> saveUsers(@RequestBody User user) {
        if(bucket.tryConsume(1)){
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        if(bucket.tryConsume(1)) {
            URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
            return ResponseEntity.created(uri).body(userService.saveRole(role));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @PostMapping("/role/addToUser")
    public ResponseEntity<?> addToUser(@RequestBody RoleToUserForm form) {
        if(bucket.tryConsume(1)) {
            userService.addRoleToUser(form.getUsername(), form.getRoleName());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    public static Optional<String> readCookie(HttpServletRequest request, String key) {
        return Arrays.stream(request.getCookies())
                .filter(c -> key.equals(c.getName()))
                .map(Cookie::getValue)
                .findAny();
    }


    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(bucket.tryConsume(1)) {
            try {
                String refresh_token = readCookie(request, "refresh_token").get();
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);
                String access_token = JWTUtils.createJwtTokenUserArchetype(request, user);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), access_token);
            } catch (NullPointerException exception) {
                log.error("Access denied in:{}", exception.getMessage());
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }else{
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(TOO_MANY_REQUESTS.value());
            new ObjectMapper().writeValue(response.getOutputStream(), "too much user requests");
        }

    }
}

@Data
class RoleToUserForm {
    private String username;
    private String roleName;
}