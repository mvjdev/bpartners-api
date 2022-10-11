package app.bpartners.api.endpoint.rest.security.swan;

import app.bpartners.api.endpoint.rest.model.Token;
import app.bpartners.api.model.exception.BadRequestException;
import app.bpartners.api.repository.mapper.SwanMapper;
import app.bpartners.api.repository.swan.model.SwanUser;
import app.bpartners.api.repository.swan.response.TokenResponse;
import app.bpartners.api.repository.swan.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.http.Header;

import static app.bpartners.api.endpoint.rest.security.swan.SwanConf.BEARER_PREFIX;

@Component
@AllArgsConstructor
public class SwanComponent {
  private final SwanMapper swanMapper;
  private final SwanConf swanConf;


  public String getSwanUserIdByToken(String accessToken) {
    try {
      return getSwanUserByToken(accessToken) != null ? getSwanUserByToken(accessToken).getId() :
          null;
    } catch (URISyntaxException | IOException | InterruptedException e) {
      return null;
    }
  }

  public Token getTokenByCode(String code, String redirectUrl) {
    try {
      HttpClient httpClient = HttpClient.newBuilder().build();
      String data =
          "client_id=" + swanConf.getClientId()
              + "&client_secret=" + swanConf.getClientSecret()
              + "&redirect_uri=" + redirectUrl
              + "&grant_type=authorization_code"
              + "&code=" + code;
      byte[] postData = data.getBytes(StandardCharsets.UTF_8);
      HttpRequest request = HttpRequest.newBuilder()
          .uri(new URI(swanConf.getTokenProviderUrl()))
          .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofByteArray(postData))
          .build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      TokenResponse tokenResponse = new ObjectMapper().readValue(response.body(),
          TokenResponse.class);
      return swanMapper.toRest(tokenResponse);
    } catch (IOException | InterruptedException | URISyntaxException e) {
      throw new BadRequestException("Code is invalid, expired, revoked or the redirectUrl "
          + redirectUrl + " does not match in the authorization request");
    }
  }

  public SwanUser getSwanUserByToken(String accessToken)
      throws URISyntaxException, IOException, InterruptedException {
    HttpClient httpClient = HttpClient.newBuilder().build();
    String message = "{\"query\":\"query ProfilePage "
        + "{user { id firstName lastName mobilePhoneNumber identificationStatus idVerified "
        + "birthDate "
        + "nationalityCCA3}}\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(swanConf.getApiUrl()))
        .header("Content-Type", "application/json")
        .header("Authorization", BEARER_PREFIX + accessToken)
        .POST(HttpRequest.BodyPublishers.ofString(message))
        .build();
    HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    UserResponse userResponse = new ObjectMapper()
        .findAndRegisterModules() //Load DateTime Module
        .readValue(response.body(), UserResponse.class);
    return userResponse.getData().getUser();
  }
}
