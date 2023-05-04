package app.bpartners.api.integration;

import app.bpartners.api.SentryConf;
import app.bpartners.api.endpoint.event.S3Conf;
import app.bpartners.api.endpoint.rest.api.PayingApi;
import app.bpartners.api.endpoint.rest.client.ApiClient;
import app.bpartners.api.endpoint.rest.client.ApiException;
import app.bpartners.api.endpoint.rest.model.PaymentInitiation;
import app.bpartners.api.endpoint.rest.model.PaymentRedirection;
import app.bpartners.api.endpoint.rest.model.PaymentStatus;
import app.bpartners.api.endpoint.rest.model.RedirectionStatusUrls;
import app.bpartners.api.endpoint.rest.security.swan.SwanComponent;
import app.bpartners.api.endpoint.rest.security.swan.SwanConf;
import app.bpartners.api.integration.conf.AbstractContextInitializer;
import app.bpartners.api.integration.conf.TestUtils;
import app.bpartners.api.manager.ProjectTokenManager;
import app.bpartners.api.repository.AccountConnectorRepository;
import app.bpartners.api.repository.LegalFileRepository;
import app.bpartners.api.repository.fintecture.FintectureConf;
import app.bpartners.api.repository.fintecture.FintecturePaymentInitiationRepository;
import app.bpartners.api.repository.jpa.PaymentRequestJpaRepository;
import app.bpartners.api.repository.jpa.model.HPaymentRequest;
import app.bpartners.api.repository.prospecting.datasource.buildingpermit.BuildingPermitConf;
import app.bpartners.api.repository.sendinblue.SendinblueConf;
import app.bpartners.api.repository.swan.AccountHolderSwanRepository;
import app.bpartners.api.repository.swan.UserSwanRepository;
import app.bpartners.api.repository.swan.model.SwanAccount;
import app.bpartners.api.service.PaymentScheduleService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static app.bpartners.api.integration.conf.TestUtils.JOE_DOE_ACCOUNT_ID;
import static app.bpartners.api.integration.conf.TestUtils.JOE_DOE_ID;
import static app.bpartners.api.integration.conf.TestUtils.SESSION1_ID;
import static app.bpartners.api.integration.conf.TestUtils.SESSION2_ID;
import static app.bpartners.api.integration.conf.TestUtils.assertThrowsApiException;
import static app.bpartners.api.integration.conf.TestUtils.joeDoeSwanAccount;
import static app.bpartners.api.integration.conf.TestUtils.setUpAccountConnectorSwanRepository;
import static app.bpartners.api.integration.conf.TestUtils.setUpAccountHolderSwanRep;
import static app.bpartners.api.integration.conf.TestUtils.setUpLegalFileRepository;
import static app.bpartners.api.integration.conf.TestUtils.setUpPaymentInitiationRep;
import static app.bpartners.api.integration.conf.TestUtils.setUpSwanComponent;
import static app.bpartners.api.integration.conf.TestUtils.setUpUserSwanRepository;
import static app.bpartners.api.integration.conf.TestUtils.toConnector;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaymentIT.ContextInitializer.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentIT {
  @MockBean
  private PaymentScheduleService paymentScheduleService;
  @MockBean
  private BuildingPermitConf buildingPermitConf;
  @Autowired
  private PaymentRequestJpaRepository requestJpaRepository;
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private SendinblueConf sendinblueConf;
  @MockBean
  private S3Conf s3Conf;
  @MockBean
  private SwanConf swanConf;
  @MockBean
  private ProjectTokenManager projectTokenManager;
  @MockBean
  private FintectureConf fintectureConf;
  @MockBean
  private AccountHolderSwanRepository accountHolderRepositoryMock;
  @MockBean
  private UserSwanRepository userSwanRepositoryMock;
  @MockBean
  private AccountConnectorRepository accountConnectorRepositoryMock;
  @MockBean
  private SwanComponent swanComponentMock;
  @MockBean
  private FintecturePaymentInitiationRepository paymentInitiationRepositoryMock;
  @MockBean
  private LegalFileRepository legalFileRepositoryMock;

  private static ApiClient anApiClient() {
    return TestUtils.anApiClient(TestUtils.JOE_DOE_TOKEN, PaymentIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() {
    setUpSwanComponent(swanComponentMock);
    setUpUserSwanRepository(userSwanRepositoryMock);
    setUpAccountConnectorSwanRepository(accountConnectorRepositoryMock);
    setUpAccountHolderSwanRep(accountHolderRepositoryMock);
    setUpPaymentInitiationRep(paymentInitiationRepositoryMock);
    setUpLegalFileRepository(legalFileRepositoryMock);
  }

  PaymentInitiation paymentInitiation1() {
    return new PaymentInitiation()
        .id(String.valueOf(randomUUID()))
        .amount(100)
        .label("Payment label 1")
        .reference("Payment reference")
        .payerName("Payer")
        .payerEmail("payer@email.com")
        .redirectionStatusUrls(
            new RedirectionStatusUrls()
                .successUrl("https://dashboard-dev.bpartners.app")
                .failureUrl("https://dashboard-dev.bpartners.app/error"));
  }

  PaymentInitiation paymentInitiation2() {
    return new PaymentInitiation()
        .id(String.valueOf(randomUUID()))
        .amount(100)
        .label("Payment label 2")
        .reference("Payment reference")
        .payerName("Payer")
        .payerEmail("payer@email.com")
        .redirectionStatusUrls(
            new RedirectionStatusUrls()
                .successUrl("https://dashboard-dev.bpartners.app")
                .failureUrl("https://dashboard-dev.bpartners.app/error"));
  }

  private static HPaymentRequest entityPaymentRequest(String sessionId, String label) {
    return HPaymentRequest.builder()
        .idInvoice(null)
        .sessionId(sessionId)
        .amount("100/1")
        .payerName("Payer")
        .payerEmail("payer@email.com")
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .label(label)
        .reference("Payment reference")
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .idUser(JOE_DOE_ID)
        .status(PaymentStatus.UNPAID)
        .build();
  }

  private static PaymentRedirection paymentRedirectionTemplate() {
    return new PaymentRedirection()
        .redirectionUrl("https://connect-v2-sbx.fintecture.com")
        .redirectionStatusUrls(new RedirectionStatusUrls()
            .successUrl("https://dashboard-dev.bpartners.app")
            .failureUrl("https://dashboard-dev.bpartners.app/error"));
  }

  @Test
  void initiate_payment_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    List<PaymentRedirection> actual = api.initiatePayments(JOE_DOE_ACCOUNT_ID,
        List.of(paymentInitiation1(), paymentInitiation2()));
    actual.forEach(paymentRedirection -> paymentRedirection.setId(null));

    assertEquals(2, actual.size());
    assertTrue(actual.contains(paymentRedirectionTemplate()));
    List<HPaymentRequest> allEntities = requestJpaRepository.findAll();
    allEntities.forEach(entity -> {
      entity.setId(null);
      entity.setCreatedDatetime(null);
    });
    assertTrue(allEntities.containsAll(
        List.of(
            entityPaymentRequest(
                SESSION1_ID,
                paymentInitiation1().getLabel()),
            entityPaymentRequest(
                SESSION2_ID,
                paymentInitiation2().getLabel()))));
  }

  @Test
  void initiate_payment_ko() {
    SwanAccount accountNoIban = joeDoeSwanAccount().toBuilder()
        .iban(null)
        .bic(null)
        .build();
    reset(accountConnectorRepositoryMock);
    when(accountConnectorRepositoryMock.findByBearer(any()))
        .thenReturn(List.of(toConnector(accountNoIban)));
    when(accountConnectorRepositoryMock.findById(any()))
        .thenReturn(toConnector(accountNoIban));

    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    assertThrowsApiException("{\"type\":\"400 BAD_REQUEST\","
            + "\"message\":\""
            + "Iban is mandatory for initiating payments. \"}",
        () -> api.initiatePayments(JOE_DOE_ACCOUNT_ID,
            List.of(paymentInitiation1())));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = TestUtils.anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
