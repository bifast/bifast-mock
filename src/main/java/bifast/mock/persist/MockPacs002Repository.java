package bifast.mock.persist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockPacs002Repository extends JpaRepository<MockPacs002, String> {
    
	Optional<MockPacs002> findByTrxTypeAndOrgnlEndToEndId (String trxType, String endToEndId);
}
