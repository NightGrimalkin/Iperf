package UPCPolska.Iperf;

import UPCPolska.Iperf.data.archetypes.Role;
import UPCPolska.Iperf.data.archetypes.User;
import UPCPolska.Iperf.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
public class IperfApplication {

    public static void main(String[] args) {
        SpringApplication.run(IperfApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }
    @Bean
    CommandLineRunner run(UserService userService){
        return args -> {
            userService.saveRole(new Role(null,"ROLE_ADMIN"));
            userService.saveRole(new Role(null,"ROLE_USER"));

            userService.saveUser(new User(null,"Jim","Jam","1234",new ArrayList<>()));
            userService.saveUser(new User(null,"Jum","1","1",new ArrayList<>()));

            userService.addRoleToUser("Jam","ROLE_USER");
            userService.addRoleToUser("1","ROLE_ADMIN");
            userService.addRoleToUser("1","ROLE_USER");
        };
    }
}
