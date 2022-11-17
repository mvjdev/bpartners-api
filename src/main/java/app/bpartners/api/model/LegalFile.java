package app.bpartners.api.model;

import java.time.Instant;
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
public class LegalFile {
  private String id;
  private String userId;
  private String name;
  private String fileUrl;
  private Instant approvalDatetime;

  public boolean isApproved() {
    return approvalDatetime != null;
  }
}
