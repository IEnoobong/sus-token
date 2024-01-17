package co.enoobong.services.services;

import co.enoobong.services.data.ActionStatus;
import co.enoobong.services.data.entities.SusActionEntity;
import co.enoobong.services.data.projections.WalletPoints;
import co.enoobong.services.data.repositories.SusActionPointRepository;
import co.enoobong.services.data.repositories.SusActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SusActionService {

  private final SusActionRepository susActionRepository;
  private final SusActionPointRepository susActionPointRepository;

  public SusActionEntity createSusAction(SusActionEntity susActionEntity) {
    susActionEntity.setStatus(ActionStatus.SUBMITTED);

    susActionEntity.setStatusNote("Initial submission.");

    return susActionRepository.save(susActionEntity);
  }

  public Page<SusActionEntity> getAddressSusActions(String walletAddress, Pageable pageable) {
    return susActionRepository.findAllByWalletAddress(walletAddress, pageable);
  }

  public WalletPoints getCurrentWalletPoints(String walletAddress, long lastKnownNonce) {
    return susActionPointRepository.getWalletPointsSinceNonce(walletAddress, lastKnownNonce);
  }
}
