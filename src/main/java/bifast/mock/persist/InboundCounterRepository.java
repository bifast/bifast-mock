package bifast.mock.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboundCounterRepository extends JpaRepository<InboundCounter, Integer> {

}
