package app.bpartners.api.repository.jpa.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"monthly_transactions_summary\"")
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode
public class HMonthlyTransactionsSummary {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String idAccount;
  private String idUser;

  @Column(name = "\"year\"")
  private int year;

  @Column(name = "\"month\"")
  private int month;

  private String income;
  private String outcome;
  private String cashFlow;
  @CreationTimestamp private Instant updatedAt;
}
