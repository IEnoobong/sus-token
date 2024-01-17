package co.enoobong.services.data.entities;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@MappedSuperclass
public abstract class VersionedEntity extends AbstractAuditable {

  @Version
  private short version;
}
