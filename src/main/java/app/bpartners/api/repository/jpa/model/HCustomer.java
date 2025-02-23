package app.bpartners.api.repository.jpa.model;

import static app.bpartners.api.endpoint.rest.model.CustomerType.PROFESSIONAL;
import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import app.bpartners.api.endpoint.rest.model.CustomerStatus;
import app.bpartners.api.endpoint.rest.model.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "\"customer\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class HCustomer {
  public static final String UPDATED_AT_PROPERTY = "updatedAt";
  @Id private String id;
  private String idUser;
  private String name;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String website;
  private String address;
  private Integer zipCode;
  private String city;
  private String country;
  private String comment;
  private Double latitude;
  private Double longitude;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private CustomerType customerType;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private CustomerStatus status;

  @Transient private boolean recentlyAdded;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  private Instant updatedAt;
  private String latestFullAddress;

  @Column(updatable = false)
  private boolean isConverted;

  @JoinTable(
      name = "has_customer",
      joinColumns = @JoinColumn(name = "id_customer", insertable = false, updatable = false),
      inverseJoinColumns = @JoinColumn(name = "id_prospect", insertable = false, updatable = false))
  @OneToOne
  private HProspect prospect;

  public boolean isConverted() {
    return isConverted;
  }

  public boolean isProfessional() {
    return PROFESSIONAL == customerType;
  }

  public String getFullAddress() {
    return address + " " + zipCode + " " + city + " " + country;
  }
}
