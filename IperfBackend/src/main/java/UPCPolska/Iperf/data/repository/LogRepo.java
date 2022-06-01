package UPCPolska.Iperf.data.repository;

import UPCPolska.Iperf.data.archetypes.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepo extends JpaRepository<Log, Long> {
}
