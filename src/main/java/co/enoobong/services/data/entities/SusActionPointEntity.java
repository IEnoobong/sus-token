package co.enoobong.services.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Table(name = "sus_action_point")
public class SusActionPointEntity extends AbstractAuditable {

  @Column(nullable = false, updatable = false)
  private String walletAddress;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @ToString.Exclude
  private SusActionEntity susAction;

  @Column
  @PositiveOrZero
  private int points;

  @Column
  private long nonce;
}
