package bifast.mock.persist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CTResponseRepository extends JpaRepository<CTResponse, Long> {
	
	Optional<CTResponse> findByEndToEndId (String endToEndId);

}
