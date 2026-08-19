package com.andrew.hdss.services;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.MembershipDto;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.models.Membership;
import com.andrew.hdss.repositories.MembershipRepository;
import com.andrew.hdss.utils.PaginationRequest;
import com.andrew.hdss.utils.Util;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "memberships", key = "#resourceRequest.page + '-' + #resourceRequest.size")
    public BatchResponse<MembershipDto> getMemberships(ResourceRequest resourceRequest) {
        Pageable pageable = Util.getPageable(
                PaginationRequest.builder()
                        .page(resourceRequest.getPage())
                        .size(resourceRequest.getSize())
                        .build()
        );

        Page<Membership> page = membershipRepository.findAll(pageable);

        BatchResponse<MembershipDto> response = new BatchResponse<>();
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setData(page.stream().map(MembershipDto::from).toList());
        return response;
    }
}
