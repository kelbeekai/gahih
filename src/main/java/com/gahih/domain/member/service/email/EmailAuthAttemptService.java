package com.gahih.domain.member.service.email;

import com.gahih.domain.member.entity.EmailAuthRequest;
import com.gahih.domain.member.repository.EmailAuthRequestRepository;
import com.gahih.global.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailAuthAttemptService {

    private final EmailAuthRequestRepository emailAuthRequestRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean increaseAttemptCountAndCheckLimitExceeded(Long emailAuthRequestId) {
        EmailAuthRequest request = emailAuthRequestRepository.findById(emailAuthRequestId)
                .orElseThrow(() -> new DomainValidationException("인증코드를 다시 요청해주세요."));

        return request.increaseAttemptCountAndCheckLimitExceeded();
    }
}