package UPCPolska.Iperf.data.repository;

import UPCPolska.Iperf.data.archetypes.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepo extends JpaRepository<Role,Long> {
    Role findByName(String name);
}
