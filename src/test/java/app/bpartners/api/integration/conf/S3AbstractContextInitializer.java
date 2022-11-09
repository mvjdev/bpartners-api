package app.bpartners.api.integration.conf;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
public abstract class S3AbstractContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    String flywayTestdataPath = "classpath:/db/testdata";
    PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("it-db")
            .withUsername("sa")
            .withPassword("sa");
    postgresContainer.start();

    String bucketName = "bpartners";
    LocalStackContainer s3Container = new LocalStackContainer(DockerImageName.parse(
        "localstack/localstack:0.11.3"))
        .withServices(S3);
    s3Container.start();

    AmazonS3 s3 = AmazonS3ClientBuilder
        .standard()
        .withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(
                s3Container.getEndpointOverride(S3).toString(),
                s3Container.getRegion()
            )
        )
        .withCredentials(s3Container.getDefaultCredentialsProvider())
        .build();

    s3.createBucket(bucketName);
    s3.putObject(new PutObjectRequest(bucketName, "dev/accounts/beed1765-5c16-472a-b3f4"
        + "-5c376ce5db58/logo/logo.jpeg", new File(testFilePath())));
    s3.putObject(new PutObjectRequest(bucketName, "dev/accounts/beed1765-5c16-472a-b3f4"
        + "-5c376ce5db58/logo/test.jpeg", new File(testFilePath())));

    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
        applicationContext,
        "server.port=" + this.getServerPort(),
        "aws.cognito.userPool.id=eu-west-3_vq2jlNjq7",
        "aws.eventBridge.bus=dummy",
        "aws.sqs.mailboxUrl=dummy",
        "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
        "spring.datasource.username=" + postgresContainer.getUsername(),
        "spring.datasource.password=" + postgresContainer.getPassword(),
        "spring.flyway.locations=classpath:/db/migration," + flywayTestdataPath,
        "aws.bucket.name=" + bucketName,
        "aws.region=" + s3Container.getRegion(),
        "aws.endpoint=" + s3Container.getEndpointOverride(S3),
        "env=dev");
  }

  static String testFilePath() {
    return "src/main/resources/files/downloaded.jpeg";
  }

  public abstract int getServerPort();
}
