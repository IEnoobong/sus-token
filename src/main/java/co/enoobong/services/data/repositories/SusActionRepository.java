package co.enoobong.services.data.repositories;

import co.enoobong.services.data.entities.SusActionEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SusActionRepository extends JpaRepository<SusActionEntity, UUID>,
    JpaSpecificationExecutor<SusActionEntity> {

  Page<SusActionEntity> findAllByWalletAddress(String walletAddress, Pageable pageable);
}
