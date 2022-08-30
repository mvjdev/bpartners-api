package app.bpartners.api.endpoint.rest.controller;

import app.bpartners.api.endpoint.rest.mapper.RedirectionMapper;
import app.bpartners.api.endpoint.rest.model.OnboardingInitiation;
import app.bpartners.api.endpoint.rest.model.Redirection;
import app.bpartners.api.service.OnboardingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class OnboardingController {
  private final OnboardingService onboardingService;
  private final RedirectionMapper mapper;

  @PostMapping(value = "/onboardingInitiation")
  public Redirection generateOnboarding(@RequestBody OnboardingInitiation params) {
    return mapper.toRest(
        onboardingService.generateOnboarding(params.getRedirectionStatusUrls().getSuccessUrl(),
            params.getRedirectionStatusUrls().getFailureUrl()));
  }
}
