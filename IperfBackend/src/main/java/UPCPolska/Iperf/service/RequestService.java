package UPCPolska.Iperf.service;

import UPCPolska.Iperf.data.archetypes.Log;
import UPCPolska.Iperf.data.repository.LogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Service
public class RequestService {
    @Autowired
    LogRepo logRepository;


    //Zapytanie bedzie budowane w frontendzie w reactcie a tutaj bedzie tylko dodoany regEx zeby sprawdzic poprawność



    public String requestExecute(String query, HttpServletRequest request) throws SocketException {
        Log log = new Log(); //przygotowanie logu do zapisania
        String s; //zczytywanie lini z odpowiedzi konsoli
        String toDisplay = ""; //budowanie stringa do wyswietlenia
        Process p;
        try {
            p = Runtime.getRuntime().exec(query);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                toDisplay += s + "\n";
            p.waitFor();
            System.out.println("exit: " + p.exitValue());
            p.destroy();
            log.setCommand(query);
            log.setIpAddress(getRequestIp(request));
        } catch (Exception e) {
            log.setCommand(query);
            log.setError(e.toString());
        }
        log.setResponse(toDisplay);
        log.setCreated(LocalDateTime.now());
        logRepository.save(log);
        return toDisplay;
    }

    private static String getRequestIp(HttpServletRequest request) {
        String remoteAddress = "";
        if (request != null) {
            remoteAddress = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddress == null || "".equals(remoteAddress)) {
                remoteAddress = request.getRemoteAddr();
            }
        }
        return remoteAddress;
    }
}
