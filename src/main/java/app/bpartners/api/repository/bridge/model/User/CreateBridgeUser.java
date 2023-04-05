package app.bpartners.api.repository.bridge.model.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Data
@ToString
public class CreateBridgeUser {
  @JsonProperty("email")
  private String email;
  @JsonProperty("password")
  private String password;
}
