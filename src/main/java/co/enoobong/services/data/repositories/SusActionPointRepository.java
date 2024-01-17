package co.enoobong.services.data.repositories;

import co.enoobong.services.data.entities.SusActionPointEntity;
import co.enoobong.services.data.projections.WalletPoints;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SusActionPointRepository extends JpaRepository<SusActionPointEntity, UUID> {

  @Query("""
      SELECT new co.enoobong.services.data.projections.WalletPoints(sap.walletAddress, COALESCE(SUM(sap.points),0), COALESCE(MAX(sap.nonce),0))
      FROM SusActionPointEntity sap
      WHERE sap.walletAddress = :walletAddress
      AND sap.nonce > :nonce
      GROUP BY sap.walletAddress
      """)
  WalletPoints getWalletPointsSinceNonce(String walletAddress, long nonce);
}
