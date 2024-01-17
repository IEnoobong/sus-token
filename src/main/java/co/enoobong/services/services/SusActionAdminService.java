package co.enoobong.services.services;

import co.enoobong.services.data.ActionStatus;
import co.enoobong.services.data.entities.SusActionEntity;
import co.enoobong.services.data.entities.SusActionPointEntity;
import co.enoobong.services.data.repositories.SusActionPointRepository;
import co.enoobong.services.data.repositories.SusActionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SusActionAdminService {

  private static final int DEFAULT_POINTS = 1000;

  private final SusActionRepository susActionRepository;
  private final SusActionPointRepository susActionPointRepository;

  public Page<SusActionEntity> getSusActions(Specification<SusActionEntity> filters,
      Pageable pageable) {
    return susActionRepository.findAll(filters, pageable);
  }

  public Optional<SusActionEntity> getSusAction(UUID id) {
    return susActionRepository.findById(id);
  }

  @Transactional
  public SusActionEntity validateSusAction(SusActionEntity susAction) {
    //validate status transition
    susAction = susActionRepository.save(susAction);
    if (susAction.getStatus() == ActionStatus.APPROVED) {
      var walletAddress = susAction.getWalletAddress();

      var susActionPointEntity = SusActionPointEntity.builder()
          .susAction(susAction)
          .walletAddress(walletAddress)
          .points(DEFAULT_POINTS)
          .nonce(System.currentTimeMillis())
          .build();

      susActionPointEntity = susActionPointRepository.save(susActionPointEntity);

      log.info("{} allocated {} points for sus action {}", walletAddress,
          susActionPointEntity.getPoints(), susAction.getId());
    }

    return susAction;
  }

}
