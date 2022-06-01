package UPCPolska.Iperf.controller;

import UPCPolska.Iperf.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;


import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
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
@RequestMapping("/iperf")
public class QueryResource {
    private final RequestService requestService;
    private final Bucket bucket;


    public QueryResource(RequestService requestService) {
        this.requestService = requestService;
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

    @PostMapping("/ping")
    public void formPage(@RequestBody String query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(bucket.tryConsume(1)) {
            String queryResponse = requestService.requestExecute(query, request);
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), queryResponse);
        }else {
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(TOO_MANY_REQUESTS.value());
            new ObjectMapper().writeValue(response.getOutputStream(), "too much ping requests");
        }
    }
}
