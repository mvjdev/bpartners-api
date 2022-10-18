package app.bpartners.api.integration;

import app.bpartners.api.SentryConf;
import app.bpartners.api.endpoint.rest.api.PayingApi;
import app.bpartners.api.endpoint.rest.client.ApiClient;
import app.bpartners.api.endpoint.rest.client.ApiException;
import app.bpartners.api.endpoint.rest.model.CrupdateInvoice;
import app.bpartners.api.endpoint.rest.model.Invoice;
import app.bpartners.api.endpoint.rest.model.InvoiceStatus;
import app.bpartners.api.endpoint.rest.security.swan.SwanComponent;
import app.bpartners.api.endpoint.rest.security.swan.SwanConf;
import app.bpartners.api.integration.conf.S3AbstractContextInitializer;
import app.bpartners.api.integration.conf.TestUtils;
import app.bpartners.api.manager.ProjectTokenManager;
import app.bpartners.api.repository.fintecture.FintectureConf;
import app.bpartners.api.repository.fintecture.FintecturePaymentInitiationRepository;
import app.bpartners.api.repository.sendinblue.SendinblueConf;
import app.bpartners.api.repository.swan.AccountHolderSwanRepository;
import app.bpartners.api.repository.swan.AccountSwanRepository;
import app.bpartners.api.repository.swan.UserSwanRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static app.bpartners.api.endpoint.rest.model.InvoiceStatus.CONFIRMED;
import static app.bpartners.api.endpoint.rest.model.InvoiceStatus.DRAFT;
import static app.bpartners.api.endpoint.rest.model.InvoiceStatus.PROPOSAL;
import static app.bpartners.api.integration.conf.TestUtils.INVOICE1_FILE_ID;
import static app.bpartners.api.integration.conf.TestUtils.INVOICE1_ID;
import static app.bpartners.api.integration.conf.TestUtils.INVOICE2_ID;
import static app.bpartners.api.integration.conf.TestUtils.INVOICE3_ID;
import static app.bpartners.api.integration.conf.TestUtils.INVOICE4_ID;
import static app.bpartners.api.integration.conf.TestUtils.JOE_DOE_ACCOUNT_ID;
import static app.bpartners.api.integration.conf.TestUtils.assertThrowsApiException;
import static app.bpartners.api.integration.conf.TestUtils.assertThrowsForbiddenException;
import static app.bpartners.api.integration.conf.TestUtils.customer1;
import static app.bpartners.api.integration.conf.TestUtils.customer2;
import static app.bpartners.api.integration.conf.TestUtils.product3;
import static app.bpartners.api.integration.conf.TestUtils.product4;
import static app.bpartners.api.integration.conf.TestUtils.product5;
import static app.bpartners.api.integration.conf.TestUtils.setUpAccountHolderSwanRep;
import static app.bpartners.api.integration.conf.TestUtils.setUpAccountSwanRepository;
import static app.bpartners.api.integration.conf.TestUtils.setUpPaymentInitiationRep;
import static app.bpartners.api.integration.conf.TestUtils.setUpSwanComponent;
import static app.bpartners.api.integration.conf.TestUtils.setUpUserSwanRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = InvoiceIT.ContextInitializer.class)
@AutoConfigureMockMvc
class InvoiceIT {
  public static final String OTHER_ACCOUNT_ID = "other_account_id";
  public static final int MAX_PAGE_SIZE = 500;
  private static final String NEW_INVOICE_ID = "invoice_uuid";

  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private SendinblueConf sendinblueConf;
  @MockBean
  private SwanConf swanConf;
  @MockBean
  private FintectureConf fintectureConf;
  @MockBean
  private ProjectTokenManager projectTokenManager;
  @MockBean
  private UserSwanRepository userSwanRepositoryMock;
  @MockBean
  private AccountSwanRepository accountSwanRepositoryMock;
  @MockBean
  private SwanComponent swanComponentMock;
  @MockBean
  private AccountHolderSwanRepository accountHolderRepositoryMock;
  @MockBean
  private FintecturePaymentInitiationRepository paymentInitiationRepositoryMock;

  private static ApiClient anApiClient() {
    return TestUtils.anApiClient(TestUtils.JOE_DOE_TOKEN, InvoiceIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() {
    setUpSwanComponent(swanComponentMock);
    setUpUserSwanRepository(userSwanRepositoryMock);
    setUpAccountSwanRepository(accountSwanRepositoryMock);
    setUpAccountHolderSwanRep(accountHolderRepositoryMock);
    setUpPaymentInitiationRep(paymentInitiationRepositoryMock);
  }

  CrupdateInvoice validInvoice() {
    return new CrupdateInvoice()
        .ref("BP003")
        .title("Facture sans produit")
        .customer(customer1())
        .products(List.of(product4(), product5()))
        .status(CONFIRMED)
        .sendingDate(LocalDate.of(2022, 9, 10))
        .toPayAt(LocalDate.of(2022, 9, 11));
  }

  CrupdateInvoice proposalInvoice() {
    return new CrupdateInvoice()
        .ref("BP004")
        .title("Facture sans produit")
        .customer(customer1())
        .products(List.of(product4(), product5()))
        .status(PROPOSAL)
        .sendingDate(LocalDate.of(2022, 10, 12))
        .toPayAt(LocalDate.of(2022, 10, 13));
  }

  CrupdateInvoice draftInvoice() {
    return new CrupdateInvoice()
        .ref("BP005")
        .title("Facture sans produit")
        .customer(customer1())
        .products(List.of(product4(), product5()))
        .status(DRAFT)
        .sendingDate(LocalDate.of(2022, 10, 12))
        .toPayAt(LocalDate.of(2022, 10, 13));
  }

  CrupdateInvoice confirmedInvoice() {
    return new CrupdateInvoice()
        .ref("BP006")
        .title("Facture sans produit")
        .customer(customer1())
        .products(List.of(product4(), product5()))
        .status(CONFIRMED)
        .sendingDate(LocalDate.of(2022, 10, 12))
        .toPayAt(LocalDate.of(2022, 10, 13));
  }

  CrupdateInvoice confirmedInvoice2() {
    return new CrupdateInvoice()
        .ref("BP007")
        .title("Facture sans produit")
        .customer(customer1())
        .products(List.of(product4(), product5()))
        .status(CONFIRMED)
        .sendingDate(LocalDate.of(2022, 10, 12))
        .toPayAt(LocalDate.of(2022, 10, 13));
  }

  Invoice invoice1() {
    return new Invoice()
        .id(INVOICE1_ID)
        .fileId(INVOICE1_FILE_ID)
        .title("Facture tableau")
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .customer(customer1()).ref("BP001")
        .sendingDate(LocalDate.of(2022, 9, 1))
        .toPayAt(LocalDate.of(2022, 10, 1))
        .status(CONFIRMED)
        .products(List.of(product3(), product4())).totalPriceWithVat(8800).totalVat(800)
        .totalPriceWithoutVat(8000);
  }

  Invoice invoice2() {
    return new Invoice()
        .id(INVOICE2_ID)
        .title("Facture plomberie")
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .fileId("BP002.pdf")
        .customer(customer2().address("Nouvelle adresse"))
        .ref("BP002")
        .sendingDate(LocalDate.of(2022, 9, 10))
        .toPayAt(LocalDate.of(2022, 10, 10))
        .status(CONFIRMED).products(List.of(product5()))
        .totalPriceWithVat(1100)
        .totalVat(100).totalPriceWithoutVat(1000);
  }

  Invoice createdInvoice() {
    return new Invoice()
        .id(NEW_INVOICE_ID)
        .fileId("BP003.pdf")
        .ref(validInvoice().getRef())
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .title("Facture sans produit")
        .customer(validInvoice().getCustomer())
        .status(InvoiceStatus.CONFIRMED)
        .sendingDate(validInvoice().getSendingDate())
        .products(List.of(product4(), product5()))
        .toPayAt(validInvoice().getToPayAt())
        .totalPriceWithVat(3300)
        .totalVat(300)
        .totalPriceWithoutVat(3000);
  }

  Invoice updatedInvoice1() {
    return new Invoice()
        .id(INVOICE3_ID)
        .fileId("BP006.pdf")
        .ref(confirmedInvoice().getRef())
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .title("Facture sans produit")
        .customer(confirmedInvoice().getCustomer())
        .status(CONFIRMED)
        .sendingDate(confirmedInvoice().getSendingDate())
        .products(List.of(product4(), product5()))
        .toPayAt(confirmedInvoice().getToPayAt())
        .totalPriceWithVat(3300)
        .totalVat(300)
        .totalPriceWithoutVat(3000);
  }

  Invoice updatedInvoice2() {
    return new Invoice()
        .id(INVOICE3_ID)
        .fileId("BP004 TEMP.pdf")
        .ref(proposalInvoice().getRef() + " TEMP")
        .title("Facture sans produit")
        .customer(proposalInvoice().getCustomer())
        .status(PROPOSAL)
        .sendingDate(proposalInvoice().getSendingDate())
        .products(List.of(product4(), product5()))
        .toPayAt(proposalInvoice().getToPayAt())
        .totalPriceWithVat(3300)
        .totalVat(300)
        .totalPriceWithoutVat(3000);
  }

  Invoice updatedInvoice3() {
    return new Invoice()
        .id(INVOICE4_ID)
        .fileId("BP007.pdf")
        .ref(confirmedInvoice2().getRef())
        .paymentUrl("https://connect-v2-sbx.fintecture.com")
        .title("Facture sans produit")
        .customer(confirmedInvoice2().getCustomer())
        .status(CONFIRMED)
        .sendingDate(confirmedInvoice2().getSendingDate())
        .products(List.of(product4(), product5()))
        .toPayAt(confirmedInvoice2().getToPayAt())
        .totalPriceWithVat(3300)
        .totalVat(300)
        .totalPriceWithoutVat(3000);
  }

  @Test
  void read_invoice_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    Invoice actual1 = api.getInvoiceById(JOE_DOE_ACCOUNT_ID, INVOICE1_ID);
    Invoice actual2 = api.getInvoiceById(JOE_DOE_ACCOUNT_ID, INVOICE2_ID);
    List<Invoice> actual = api.getInvoices(JOE_DOE_ACCOUNT_ID, 1, 10);

    assertEquals(invoice1(), actual1);
    assertEquals(invoice2(), actual2);
    assertTrue(actual.containsAll(List.of(actual1, actual2)));
  }

  @Test
  void read_invoice_ko() {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    assertThrowsForbiddenException(() -> api.getInvoices(OTHER_ACCOUNT_ID, 1, 10));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page must be >=1\"}",
        () -> api.getInvoices(JOE_DOE_ACCOUNT_ID, -1, 10));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size must be >=1\"}",
        () -> api.getInvoices(JOE_DOE_ACCOUNT_ID, 1, -10));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size must be <" + MAX_PAGE_SIZE
            + "\"}",
        () -> api.getInvoices(JOE_DOE_ACCOUNT_ID, 1, MAX_PAGE_SIZE + 1));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Required request parameter 'page' for method"
            + " parameter type PageFromOne is not present"
            + "\"}",
        () -> api.getInvoices(JOE_DOE_ACCOUNT_ID, null, 10));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Required request parameter 'pageSize' for "
            + "method parameter type BoundedPageSize is not present\"}",
        () -> api.getInvoices(JOE_DOE_ACCOUNT_ID, 1, null));
  }

  @Test
  void crupdate_invoice_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    Invoice actual = api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, NEW_INVOICE_ID, validInvoice());

    assertEquals(createdInvoice(), actual);
  }

  @Test
  void crupdate_invoice_draft_to_proposal_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    Invoice actual = api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, INVOICE3_ID, proposalInvoice());

    assertEquals(updatedInvoice2(), actual);
    assertTrue(actual.getRef().contains("TEMP"));
  }

  @Test
  void crupdate_invoice_draft_to_confirmed_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    Invoice actual = api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, INVOICE3_ID, confirmedInvoice());

    assertEquals(updatedInvoice1(), actual);
    assertFalse(actual.getRef().contains("TEMP"));
  }

  @Test
  void crupdate_invoice_proposal_to_confirmed_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    Invoice actual = api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, INVOICE4_ID, confirmedInvoice2());

    assertNotNull(actual.getPaymentUrl());
    assertEquals(updatedInvoice3(), actual);
    assertFalse(actual.getRef().contains("TEMP"));
  }

  @Test
  void crupdate_invoice_proposal_to_draft_ko() {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    assertThrows(ApiException.class,
        () -> api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, INVOICE4_ID, draftInvoice()));
  }

  @Test
  void crupdate_invoice_confirmed_to_draft_ko() {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    assertThrows(ApiException.class,
        () -> api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, INVOICE1_ID, draftInvoice()));
  }

  @Test
  void crupdate_invoice_confirmed_to_proposal_ko() {
    ApiClient joeDoeClient = anApiClient();
    PayingApi api = new PayingApi(joeDoeClient);

    assertThrows(ApiException.class,
        () -> api.crupdateInvoice(JOE_DOE_ACCOUNT_ID, INVOICE1_ID, proposalInvoice()));
  }
  /* /!\ Use for unit test only
  @Test
  void generate_invoice_pdf_ok() throws IOException {
    byte[] data = invoiceService.generateInvoicePdf(INVOICE1_ID);
    File generatedFile = new File("test.pdf");
    OutputStream os = new FileOutputStream(generatedFile);
    os.write(data);
    os.close();
  }
*/

  static class ContextInitializer extends S3AbstractContextInitializer {
    public static final int SERVER_PORT = TestUtils.anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
