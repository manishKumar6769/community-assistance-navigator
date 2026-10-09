package com.navigator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.navigator.controller.GlobalExceptionHandler;
import com.navigator.controller.ReferralController;
import com.navigator.model.Referral;
import com.navigator.model.Resource;
import com.navigator.repository.ReferralRepository;
import com.navigator.repository.ResourceRepository;
import com.navigator.service.ReferralService;

class ReferralControllerTest {

    private final ReferralRepository referralRepository = mock(ReferralRepository.class);
    private final ResourceRepository resourceRepository = mock(ResourceRepository.class);
    private final Map<String, Referral> referrals = new HashMap<>();
    private final Map<String, Resource> resources = new HashMap<>();
    private final MockMvc mockMvc;

    ReferralControllerTest() {
        when(referralRepository.save(any(Referral.class))).thenAnswer(invocation -> {
            Referral referral = invocation.getArgument(0);
            referrals.put(referral.getReferralId(), referral);
            return referral;
        });
        when(referralRepository.findById(any(String.class))).thenAnswer(invocation ->
                referrals.get(invocation.getArgument(0)));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            resources.put(resource.getResourceId(), resource);
            return resource;
        });
        when(resourceRepository.findById(any(String.class))).thenAnswer(invocation ->
                resources.get(invocation.getArgument(0)));

        ReferralService service = new ReferralService(referralRepository, resourceRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReferralController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void referralLifecycleUpdatesStatusAndReliabilityThroughApi() throws Exception {
        Resource firstResource = resource("resource-1", 80);
        Resource secondResource = resource("resource-2", 80);
        resources.put(firstResource.getResourceId(), firstResource);
        resources.put(secondResource.getResourceId(), secondResource);

        createReferral("user-1", "resource-1", "assessment-1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();
        String referralId = referralIdForUser("user-1");

        mockMvc.perform(put("/api/referrals/{id}/status", referralId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONTACTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTACTED"));

        mockMvc.perform(put("/api/referrals/{id}/status", referralId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RECEIVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        createReferral("user-2", "resource-2", "assessment-2")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();
        String secondReferralId = referralIdForUser("user-2");

        mockMvc.perform(put("/api/referrals/{id}/status", secondReferralId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOT_RECEIVED\",\"failureReason\":\"INCORRECT_INFO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_RECEIVED"))
                .andExpect(jsonPath("$.failureReason").value("INCORRECT_INFO"));

        assertEquals(75, secondResource.getReliabilityScore());
    }

    @Test
    void notReceivedWithoutValidFailureReasonReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/referrals/{id}/status", "ref-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOT_RECEIVED\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/referrals/{id}/status", "ref-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOT_RECEIVED\",\"failureReason\":\"BAD_REASON\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listWithoutUserIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/referrals"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Required query parameter is missing"));
    }

    private org.springframework.test.web.servlet.ResultActions createReferral(String userId,
                                                                             String resourceId,
                                                                             String assessmentId)
            throws Exception {
        return mockMvc.perform(post("/api/referrals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": "%s",
                          "resourceId": "%s",
                          "assessmentId": "%s"
                        }
                        """.formatted(userId, resourceId, assessmentId)));
    }

    private Resource resource(String resourceId, int reliabilityScore) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        resource.setReliabilityScore(reliabilityScore);
        return resource;
    }

    private String referralIdForUser(String userId) {
        return referrals.values()
                .stream()
                .filter(referral -> userId.equals(referral.getUserId()))
                .findFirst()
                .map(Referral::getReferralId)
                .orElseThrow();
    }
}
