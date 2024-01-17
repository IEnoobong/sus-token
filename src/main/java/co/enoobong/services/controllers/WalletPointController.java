package co.enoobong.services.controllers;

import co.enoobong.services.data.projections.WalletPoints;
import co.enoobong.services.services.SusActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RequestMapping("/api/wallets/points")
@RestController
@RequiredArgsConstructor
public class WalletPointController {

  private final SusActionService susActionService;

  @GetMapping("{walletAddress}")
  public WalletPoints getCurrentWalletPoints(@PathVariable String walletAddress,
      @RequestParam(required = false, defaultValue = "0") long lastKnownNonce) {
    return susActionService.getCurrentWalletPoints(walletAddress, lastKnownNonce);
  }
}
