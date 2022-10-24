package app.bpartners.api.service;

import app.bpartners.api.endpoint.rest.model.FileType;
import app.bpartners.api.endpoint.rest.model.InvoiceStatus;
import app.bpartners.api.endpoint.rest.security.model.Principal;
import app.bpartners.api.endpoint.rest.security.principal.PrincipalProvider;
import app.bpartners.api.model.Account;
import app.bpartners.api.model.AccountHolder;
import app.bpartners.api.model.BoundedPageSize;
import app.bpartners.api.model.Invoice;
import app.bpartners.api.model.PageFromOne;
import app.bpartners.api.model.PaymentRedirection;
import app.bpartners.api.model.Product;
import app.bpartners.api.model.exception.ApiException;
import app.bpartners.api.model.validator.InvoiceValidator;
import app.bpartners.api.repository.InvoiceRepository;
import app.bpartners.api.repository.ProductRepository;
import app.bpartners.api.service.aws.SesService;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import static app.bpartners.api.endpoint.rest.model.InvoiceStatus.CONFIRMED;
import static app.bpartners.api.endpoint.rest.model.InvoiceStatus.DRAFT;
import static app.bpartners.api.endpoint.rest.model.InvoiceStatus.PROPOSAL;
import static app.bpartners.api.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static app.bpartners.api.service.utils.FileInfoUtils.JPG_FORMAT_NAME;
import static app.bpartners.api.service.utils.FileInfoUtils.PDF_EXTENSION;
import static com.google.zxing.BarcodeFormat.QR_CODE;

@Service
@AllArgsConstructor
@Slf4j
public class InvoiceService {
  private final InvoiceRepository repository;
  private final ProductRepository productRepository;
  private final PaymentInitiationService pis;
  private final FileService fileService;
  private final AccountHolderService holderService;
  private final PrincipalProvider auth;
  private final InvoiceValidator validator;
  private final SesService mailService;

  public List<Invoice> getInvoices(String accountId, PageFromOne page, BoundedPageSize pageSize,
                                   InvoiceStatus status) {
    int pageValue = page.getValue() - 1;
    int pageSizeValue = pageSize.getValue();
    List<Invoice> invoices = repository.findAllByAccountId(accountId, pageValue, pageSizeValue);
    if (status != null) {
      invoices = repository.findAllByAccountIdAndStatus(accountId, status,
          pageValue,
          pageSizeValue);
    }
    return invoices.stream()
        .map(this::refreshValues)
        .collect(Collectors.toUnmodifiableList());
  }

  public Invoice getById(String invoiceId) {
    return refreshValues(repository.getById(invoiceId));
  }

  public Invoice crupdateInvoice(Invoice toCrupdate) {
    validator.accept(toCrupdate);
    Invoice refreshedInvoice = refreshValues(repository.crupdate(toCrupdate));
    byte[] pdfAsBytes;
    if (toCrupdate.getStatus().equals(CONFIRMED)) {
      pdfAsBytes = generateInvoicePdf(refreshedInvoice);
    } else {
      pdfAsBytes = generateDraftPdf(refreshedInvoice);
    }
    fileService.uploadFile(FileType.INVOICE, refreshedInvoice.getAccount().getId(),
        refreshedInvoice.getFileId(), pdfAsBytes);
    /*/!\ Only use for local test because localstack seems to not permit download after upload
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(refreshedInvoice.getFileId());
        fos.write(pdfAsBytes);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        if (fos != null) {
          try {
            fos.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }*/
    if (!sendInvoiceMail(refreshedInvoice, pdfAsBytes)
        && !refreshedInvoice.getStatus().equals(DRAFT)) {
      throw new ApiException(SERVER_EXCEPTION, "Mail not send");
    }
    return refreshedInvoice;
  }

  private Invoice refreshValues(Invoice invoice) {
    List<Product> products = invoice.getProducts();
    if (products.isEmpty()) {
      products =
          productRepository.findByIdInvoice(invoice.getId());
    }
    Invoice initializedInvoice = Invoice.builder()
        .id(invoice.getId())
        .comment(invoice.getComment())
        .updatedAt(invoice.getUpdatedAt())
        .title(invoice.getTitle())
        .invoiceCustomer(invoice.getInvoiceCustomer())
        .account(invoice.getAccount())
        .status(invoice.getStatus())
        .totalVat(computeTotalVat(products))
        .totalPriceWithoutVat(computeTotalPriceWithoutVat(products))
        .totalPriceWithVat(computeTotalPriceWithVat(products))
        .products(products)
        .toPayAt(invoice.getToPayAt())
        .sendingDate(invoice.getSendingDate())
        .build();
    if (!invoice.getStatus().equals(CONFIRMED)) {
      initializedInvoice.setPaymentUrl(null);
      initializedInvoice.setRef(invoice.getRef() + "-TMP");
    } else {
      PaymentRedirection paymentRedirection = pis.initiateInvoicePayment(initializedInvoice);
      initializedInvoice.setPaymentUrl(paymentRedirection.getRedirectUrl());
      initializedInvoice.setRef(invoice.getRef());
    }
    return initializedInvoice;
  }

  private int computeTotalVat(List<Product> products) {
    if (products == null) {
      return 0;
    }
    return products.stream()
        .mapToInt(Product::getTotalVat)
        .sum();
  }

  private int computeTotalPriceWithoutVat(List<Product> products) {
    if (products == null) {
      return 0;
    }
    return products.stream()
        .mapToInt(Product::getTotalWithoutVat)
        .sum();
  }

  private int computeTotalPriceWithVat(List<Product> products) {
    if (products == null) {
      return 0;
    }
    return products.stream()
        .mapToInt(Product::getTotalPriceWithVat)
        .sum();
  }

  private String parseInvoiceTemplateToString(Invoice invoice, String template) {
    TemplateEngine templateEngine = configureTemplate();
    Context context = configureContext(invoice);
    return templateEngine.process(template, context);
  }

  public byte[] generateInvoicePdf(Invoice invoice) {
    ITextRenderer renderer = new ITextRenderer();
    loadStyle(renderer, invoice, "invoice");
    renderer.layout();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      renderer.createPDF(outputStream);
    } catch (DocumentException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
    return outputStream.toByteArray();
  }

  public byte[] generateDraftPdf(Invoice invoice) {
    ITextRenderer renderer = new ITextRenderer();
    loadStyle(renderer, invoice, "draft");
    renderer.layout();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      renderer.createPDF(outputStream);
    } catch (DocumentException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
    return outputStream.toByteArray();
  }

  private void loadStyle(ITextRenderer renderer, Invoice invoice, String template) {
    String baseUrl = FileSystems.getDefault()
        .getPath("src/main", "resources", "templates")
        .toUri()
        .toString();
    renderer.setDocumentFromString(parseInvoiceTemplateToString(invoice, template), baseUrl);
  }

  private Context configureContext(Invoice invoice) {
    Context context = new Context();
    Account account = invoice.getAccount();
    AccountHolder accountHolder = holderService.getAccountHolderByAccountId(account.getId());
    accountHolder.setTvaNumber("FR 32 123456789");  //TODO: make this persisted and then remove
    accountHolder.setMobilePhoneNumber(
        "+33 6 11 22 33 44"); //TODO: make this persisted and remove
    accountHolder.setSocialCapital("40 000 €"); //TODO: make this persisted and remove
    accountHolder.setEmail("numer@hei.school"); //TODO: make this persisted and remove
    byte[] logoBytes =
        fileService.downloadFile(FileType.LOGO, account.getId(), userLogoFileId());
    context.setVariable("invoice", invoice);
    context.setVariable("logo", base64Image(logoBytes));
    context.setVariable("account", account);
    context.setVariable("accountHolder", accountHolder);
    if (invoice.getPaymentUrl() != null) {
      byte[] qrCodeBytes = generateQrCode(invoice.getPaymentUrl());
      context.setVariable("qrcode", base64Image(qrCodeBytes));
    }
    return context;
  }

  private TemplateEngine configureTemplate() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode(TemplateMode.HTML);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);
    return templateEngine;
  }

  private byte[] generateQrCode(String link) {
    int width = 1000; //TODO: make this size parameterizable
    int height = 1000; //TODO: make this size parameterizable
    try {
      QRCodeWriter writer = new QRCodeWriter();
      BitMatrix bitMatrix = writer.encode(link, QR_CODE, width, height);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), JPG_FORMAT_NAME, os);
      return os.toByteArray();
    } catch (WriterException | IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private String userLogoFileId() {
    return ((Principal) auth.getAuthentication().getPrincipal()).getUser().getLogoFileId();
  }

  private String base64Image(byte[] image) {
    return Base64.getEncoder().encodeToString(image);
  }

  private boolean sendInvoiceMail(Invoice refreshedInvoice, byte[] pdf) {
    if (refreshedInvoice.getStatus().equals(DRAFT)) {
      return false;
    }
    String type = "";
    if (refreshedInvoice.getStatus().equals(PROPOSAL)) {
      type = "Devis";
    }
    if (refreshedInvoice.getStatus().equals(CONFIRMED)) {
      type = "Facture";
    }
    if (!type.isBlank()) {
      String subject = type + " " + refreshedInvoice.getRef();
      try {
        mailService.sendEmail(
            refreshedInvoice.getInvoiceCustomer().getEmail(),
            subject,
            emailBody(type.toLowerCase()),
            subject + PDF_EXTENSION,
            pdf
        );
      } catch (IOException | MessagingException e) {
        log.info("response={}", e);
        return false;
      }
    }
    return true;
  }

  //TODO: set it again in template resolver when the load style baseUrl is set
  private String emailBody(String type) {
    return "<html xmlns:th=\"http://www.thymeleaf.org\">\n"
        + "    <body style=\"font-family: 'Gill Sans'\">\n"
        + "        <h2 style=color:#8d2158;>Nom de l'artisan</h2>\n"
        + "        <h3 style=color:#424141;>\n"
        + "            Slogan de l'artisan\n"
        + "        </h3>\n"
        + "        <p>Bonjour,</p>\n"
        + "        <p>\n"
        + "            Retrouvez-ci joint votre " + type + " enregistré à la référence XXX\n"
        + "        </p>\n"
        + "        <p>Bien à vous et merci pour votre confiance.</p>\n"
        + "    </body>\n"
        + "</html>";
  }
}