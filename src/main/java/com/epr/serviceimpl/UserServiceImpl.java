// src/main/java/com/epr/serviceimpl/UserServiceImpl.java
package com.epr.serviceimpl;

import com.epr.dto.user.UserRequestDto;
import com.epr.dto.user.UserResponseDto;
import com.epr.entity.Role;
import com.epr.entity.User;
import com.epr.repository.RoleRepository;
import com.epr.repository.UserRepository;
import com.epr.service.UserService;
import com.epr.util.DateTimeUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public List<UserResponseDto> findAllActiveUsers() {
        log.info("Fetching all active users");
        return userRepository.findAllActiveUsers()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto findById(Long id) {
        log.info("Fetching user by ID: {}", id);
        return userRepository.findActiveById(id)
                .map(this::toResponseDto)
                .orElse(null);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto dto) {
        log.info("Creating new user with email: {}", dto.getEmail());

        // 1. Validate DTO
        validateDto(dto);

        // 2. Check email uniqueness
        if (userRepository.existsByEmailIgnoreCaseAndNotId(dto.getEmail(), null)) {
            log.warn("Email already exists: {}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        // 3. Validate displayStatus length (prevent MySQL truncation)
        String displayStatus = dto.getDisplayStatus();
        if (displayStatus != null && displayStatus.length() > 20) {
            log.error("displayStatus too long: {}", displayStatus);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "display_status must be 20 characters or less");
        }

        User user = new User();
        mapRequestToEntity(user, dto);

        user.setUuid(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        user.setRegDate(dateTimeUtil.getCurrentUtcTime());
        user.setModifyDate(user.getRegDate());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        assignRoles(user, dto.getRoleIds());

        try {
            User saved = userRepository.save(user);
            log.info("User created successfully: {} (ID: {})", saved.getEmail(), saved.getId());
            return toResponseDto(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Database error while saving user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        log.info("Updating user ID: {}", id);

        validateDto(dto);

        User existing = userRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        if (userRepository.existsByEmailIgnoreCaseAndNotId(dto.getEmail(), id)) {
            log.warn("Email already in use by another user: {}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        String displayStatus = dto.getDisplayStatus();
        if (displayStatus != null && displayStatus.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "display_status too long");
        }

        mapRequestToEntity(existing, dto);
        existing.setModifyDate(dateTimeUtil.getCurrentUtcTime());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        assignRoles(existing, dto.getRoleIds());

        try {
            User updated = userRepository.save(existing);
            log.info("User updated successfully: {}", updated.getEmail());
            return toResponseDto(updated);
        } catch (Exception e) {
            log.error("Error updating user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user");
        }
    }

    @Override
    public void softDeleteUser(Long id) {
        log.info("Soft deleting user ID: {}", id);
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for deletion: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        user.setDeleteStatus(1);
        user.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        userRepository.save(user);
        log.info("User soft deleted: {}", id);
    }

    @Override
    public List<UserResponseDto> searchUsers(String keyword) {
        log.info("Searching users with keyword: {}", keyword);
        return userRepository.searchByFullNameOrEmail(keyword)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmailIgnoreCaseAndDeleteStatus(email, 2);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user details for email: {}", email);
        User user = userRepository.findByEmailIgnoreCaseAndDeleteStatus(email, 2)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getDeleteStatus() == 2, // enabled if not deleted
                true, // account non-expired
                true, // credentials non-expired
                true, // account non-locked
                authorities
        );
    }

    // Helper Methods (unchanged)
    private void validateDto(UserRequestDto dto) {
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed: {}", errors);
            throw new ConstraintViolationException("Validation failed: " + errors, violations);
        }
    }


    private void mapRequestToEntity(User user, UserRequestDto dto) {
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setProfilePicture(dto.getProfilePicture());
        user.setJobTitle(dto.getJobTitle());
        user.setAboutMe(dto.getAboutMe());
        user.setDepartment(dto.getDepartment());
        user.setDisplayStatus(dto.getDisplayStatus() != null && !dto.getDisplayStatus().trim().isEmpty()
                ? dto.getDisplayStatus().trim() : "1");
        user.setFacebook(dto.getFacebook());
        user.setLinkedin(dto.getLinkedin());
        user.setTwitter(dto.getTwitter());
        user.setSlug(dto.getSlug());
        user.setMetaTitle(dto.getMetaTitle());
        user.setMetaKeyword(dto.getMetaKeyword());
        user.setMetaDescription(dto.getMetaDescription());
    }

    private void assignRoles(User user, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                log.warn("Some role IDs not found: {}", roleIds);
            }
            user.setRoles(roles);
        } else {
            user.setRoles(List.of()); // Clear roles if none provided
        }
    }

    private UserResponseDto toResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUuid(user.getUuid());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setJobTitle(user.getJobTitle());
        dto.setAboutMe(user.getAboutMe());
        dto.setDepartment(user.getDepartment());
        dto.setDisplayStatus(user.getDisplayStatus());
        dto.setRegDate(user.getRegDate());
        dto.setModifyDate(user.getModifyDate());
        dto.setFacebook(user.getFacebook());
        dto.setLinkedin(user.getLinkedin());
        dto.setTwitter(user.getTwitter());
        dto.setSlug(user.getSlug());
        dto.setMetaTitle(user.getMetaTitle());
        dto.setMetaKeyword(user.getMetaKeyword());
        dto.setMetaDescription(user.getMetaDescription());
        dto.setRoleNames(user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList()));
        return dto;
    }
}