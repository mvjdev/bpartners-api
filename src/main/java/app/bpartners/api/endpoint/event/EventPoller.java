package app.bpartners.api.endpoint.event;

import app.bpartners.api.endpoint.event.EventConsumer.AcknowledgeableTypedEvent;
import app.bpartners.api.endpoint.event.model.TypedEvent;
import app.bpartners.api.endpoint.event.model.TypedFileUploaded;
import app.bpartners.api.endpoint.event.model.TypedMailSent;
import app.bpartners.api.endpoint.event.model.gen.FileUploaded;
import app.bpartners.api.endpoint.event.model.gen.MailSent;
import app.bpartners.api.model.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@Slf4j
public class EventPoller {

  private final String queueUrl;
  private final SqsClient sqsClient;
  private final ObjectMapper om;
  private final EventConsumer eventConsumer;

  private static final Duration WAIT_TIME = Duration.ofSeconds(20); // MUST be <= 20s
  private static final int MAX_NUMBER_OF_MESSAGES = 10;

  public EventPoller(
      @Value("${aws.sqs.queueUrl}") String queueUrl,
      SqsClient sqsClient,
      ObjectMapper om,
      EventConsumer eventConsumer) {
    this.queueUrl = queueUrl;
    this.sqsClient = sqsClient;
    this.om = om;
    this.eventConsumer = eventConsumer;
  }

  @Scheduled(cron = "0/10 * * * * *")
  public void poll() {
    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .waitTimeSeconds(WAIT_TIME.toSecondsPart())
        .maxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
        .build();

    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
    if (!messages.isEmpty()) {
      log.info("Events received: {}", messages);
      var ackEvents = toAcknowledgeableTypedEvents(messages);
      eventConsumer.accept(ackEvents);
    }
  }

  private List<AcknowledgeableTypedEvent> toAcknowledgeableTypedEvents(List<Message> messages) {
    List<AcknowledgeableTypedEvent> res = new ArrayList<>();

    for (Message message : messages) {
      TypedEvent typedEvent;
      try {
        typedEvent = toTypedEvent(message);
      } catch (Exception e) {
        log.error("Message could not be unmarshalled, message={}", message, e);
        continue;
      }

      res.add(new AcknowledgeableTypedEvent(
          typedEvent,
          () -> sqsClient.deleteMessage(DeleteMessageRequest.builder()
              .queueUrl(queueUrl)
              .receiptHandle(message.receiptHandle())
              .build())));
    }

    return res;
  }

  private TypedEvent toTypedEvent(Message message) throws JsonProcessingException {
    TypedEvent typedEvent;

    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
    };
    Map<String, Object> body = om.readValue(message.body(), typeRef);
    String typeName = body.get("detail-type").toString();
    if (FileUploaded.class.getTypeName().equals(typeName)) {
      FileUploaded fileUploaded = om.convertValue(body.get("detail"), FileUploaded.class);
      typedEvent = new TypedFileUploaded(fileUploaded);
    } else if (MailSent.class.getTypeName().equals(typeName)) {
      MailSent mailSent = om.convertValue(body.get("detail"), MailSent.class);
      typedEvent = new TypedMailSent(mailSent);
    } else {
      throw new BadRequestException("Unexpected message type for message=" + message);
    }

    return typedEvent;
  }
}
