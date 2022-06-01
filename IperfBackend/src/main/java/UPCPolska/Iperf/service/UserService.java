package UPCPolska.Iperf.service;

import UPCPolska.Iperf.data.archetypes.Role;
import UPCPolska.Iperf.data.archetypes.User;
import UPCPolska.Iperf.data.repository.RoleRepo;
import UPCPolska.Iperf.data.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements  UserDetailsService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            log.error("User not found");
            throw new UsernameNotFoundException("User not found");
        } else {
            log.error("User found");
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    
    public User saveUser(User user) {
        log.info("Saving new user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    
    public Role saveRole(Role role) {
        log.info("Saving new role");
        return roleRepo.save(role);
    }

    
    public void addRoleToUser(String username, String roleName) {
        log.info("adding role to user");
        User user = userRepo.findByUsername(username);
        Role role = roleRepo.findByName(roleName);
        user.getRoles().add(role);
    }

    
    public User getUser(String username) {
        log.info("Getting user");
        return userRepo.findByUsername(username);
    }

    
    public List<User> getUsers() {
        log.info("Getting all users");
        return userRepo.findAll();
    }


}
