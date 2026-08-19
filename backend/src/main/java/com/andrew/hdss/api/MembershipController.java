package com.andrew.hdss.api;

import com.andrew.hdss.api.swaggerinterfaces.MembershipApi;
import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.MembershipDto;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.services.MembershipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
@Tag(name = "Membership Data Retrieval")
public class MembershipController implements MembershipApi {

    private final MembershipService membershipService;

    @GetMapping
    public BatchResponse<MembershipDto> getMemberships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return membershipService.getMemberships(new ResourceRequest(page, size));
    }
}