package app.bpartners.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
  //TODO: map with Fintecture responses
  private String id;
  private String sessionId;
  private String invoiceId;
  private String accountId;
  private String label;
  private String reference;
  private String payerName;
  private String payerEmail;
  private Fraction amount;
}
