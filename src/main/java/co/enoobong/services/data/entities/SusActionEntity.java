package co.enoobong.services.data.entities;

import co.enoobong.services.data.ActionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@Builder
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Table(name = "sus_action")
public class SusActionEntity extends VersionedEntity {

  @NotBlank
  @Pattern(regexp = "^0x[a-fA-F0-9]{40}$")
  @Column(nullable = false, updatable = false)
  private String walletAddress;

  @NotBlank
  @Column(nullable = false)
  private String description;

  @Column
  private String proofData;

  @Column
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ActionStatus status = ActionStatus.SUBMITTED;

  @Column
  private String statusNote;
}
