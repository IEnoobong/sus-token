package co.enoobong.services.data.entities;

import co.enoobong.services.data.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "application_user")
public class User extends AbstractEntity {

  @Column(unique = true)
  private String username;
  private String name;
  @JsonIgnore
  private String hashedPassword;
  @Enumerated(EnumType.STRING)
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<Role> roles;
  @Lob
  @Column(length = 1000000)
  private byte[] profilePicture;
}
