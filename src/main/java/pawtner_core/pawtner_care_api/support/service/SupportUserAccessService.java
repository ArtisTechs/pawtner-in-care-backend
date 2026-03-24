package pawtner_core.pawtner_care_api.support.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pawtner_core.pawtner_care_api.entity.User;
import pawtner_core.pawtner_care_api.enums.UserRole;
import pawtner_core.pawtner_care_api.exception.ResourceNotFoundException;
import pawtner_core.pawtner_care_api.repository.UserRepository;
import pawtner_core.pawtner_care_api.support.enums.SupportParticipantRole;

@Service
public class SupportUserAccessService {

    private final UserRepository userRepository;

    public SupportUserAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public void assertCustomer(UUID userId) {
        SupportParticipantRole supportRole = resolveSupportRole(userId);
        if (supportRole != SupportParticipantRole.CUSTOMER) {
            throw new IllegalArgumentException("User " + userId + " is not allowed to act as a customer in support messaging");
        }
    }

    @Transactional(readOnly = true)
    public void assertAdmin(UUID userId) {
        SupportParticipantRole supportRole = resolveSupportRole(userId);
        if (supportRole != SupportParticipantRole.ADMIN) {
            throw new IllegalArgumentException("User " + userId + " is not allowed to act as an admin in support messaging");
        }
    }

    @Transactional(readOnly = true)
    public SupportParticipantRole resolveSupportRole(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " was not found"));

        if (user.getRole() == UserRole.ADMIN) {
            return SupportParticipantRole.ADMIN;
        }

        return SupportParticipantRole.CUSTOMER;
    }
}
