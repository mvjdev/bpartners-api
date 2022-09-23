package app.bpartners.api.unit.validator;

import app.bpartners.api.endpoint.rest.model.OnboardingInitiation;
import app.bpartners.api.endpoint.rest.model.RedirectionStatusUrls;
import app.bpartners.api.endpoint.rest.validator.OnboardingInitiationValidator;
import org.junit.jupiter.api.Test;
import static app.bpartners.api.integration.conf.TestUtils.assertThrowsBadRequestException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OnboardingInitiationValidatorTest {
  private final OnboardingInitiationValidator validator = new OnboardingInitiationValidator();

  @Test
  void validator_validate_onboardingInitiation_ok() {
    assertDoesNotThrow(
        () -> validator.accept(
            new OnboardingInitiation()
                .redirectionStatusUrls(
                    new RedirectionStatusUrls()
                        .failureUrl("failure")
                        .successUrl("success")
                )
        ));
  }

  @Test
  void validator_validate_invalid_onboarding_ko() {
    assertThrowsBadRequestException("redirectionStatusUrls.successUrl is mandatory. "
            + "redirectionStatusUrls.failureUrl is mandatory. ",
        () -> validator.accept(
            new OnboardingInitiation()
                .redirectionStatusUrls(
                    new RedirectionStatusUrls()
                        .failureUrl(null)
                        .successUrl(null)
                )
        ));
    assertThrowsBadRequestException("redirectionStatusUrls is mandatory. ", () -> validator.accept(
        new OnboardingInitiation()
            .redirectionStatusUrls(
                null
            )
    ));
  }
}
