package com.andrew.hdss.services;

import com.andrew.hdss.dtos.RoundCreationResult;
import com.andrew.hdss.dtos.RoundDetailsDto;
import com.andrew.hdss.dtos.RoundDto;
import com.andrew.hdss.exceptions.RoundCreationInProgressException;
import com.andrew.hdss.exceptions.RoundCreationUnavailableException;
import com.andrew.hdss.exceptions.RoundNotFoundException;
import com.andrew.hdss.models.Round;
import com.andrew.hdss.models.enums.RoundStatus;
import com.andrew.hdss.repositories.RoundRepository;
import com.andrew.hdss.round.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

@Service
public class RoundService {

    private static final Logger log =
            LoggerFactory.getLogger(RoundService.class);

    private final RoundValidator roundValidator;
    private final RoundRepository roundRepository;
    private final RoundDatabaseCopier databaseCopier;
    private final RoundDatabaseNameGenerator databaseNameGenerator;
    private final RoundCreationLock creationLock;
    private final ObjectMapper objectMapper;
    private final Executor roundTaskExecutor;

    public RoundService(
            RoundValidator roundValidator,
            RoundRepository roundRepository,
            RoundDatabaseCopier databaseCopier,
            RoundDatabaseNameGenerator databaseNameGenerator,
            RoundCreationLock creationLock,
            ObjectMapper objectMapper,
            @Qualifier("roundTaskExecutor") Executor roundTaskExecutor
    ) {
        this.roundValidator = roundValidator;
        this.roundRepository = roundRepository;
        this.databaseCopier = databaseCopier;
        this.databaseNameGenerator = databaseNameGenerator;
        this.creationLock = creationLock;
        this.objectMapper = objectMapper;
        this.roundTaskExecutor = roundTaskExecutor;
    }

    /**
     * Validates the live database and starts round creation if validation passes.
     */
    public RoundCreationResult createRound(String roundName) {

        RoundCreationLock.Lease lease = creationLock.tryAcquire();

        if (lease == null) {
            throw new RoundCreationInProgressException();
        }

        boolean lockTransferred = false;
        Round round = null;

        try {
            ValidationReport report = roundValidator.validate();

            if (!report.passed()) {
                return new RoundCreationResult(report, null);
            }

            String reportJson = serializeReport(report);

            String databaseName = databaseNameGenerator.generate();

            round = new Round();
            round.setRoundName(roundName.trim());
            round.setDatabaseName(databaseName);
            round.setStatus(RoundStatus.CREATING);
            round.setValidationPassed(true);
            round.setValidationReport(reportJson);
            round.setStartedAt(LocalDateTime.now());

            round = roundRepository.saveAndFlush(round);

            Long roundId = round.getId();

            Round savedRound = round;

            try {
                roundTaskExecutor.execute(
                        () -> performCopy(
                                roundId,
                                savedRound.getDatabaseName(),
                                lease
                        )
                );

                // The asynchronous worker now owns the lock.
                lockTransferred = true;

            } catch (RuntimeException e) {
                markFailed(roundId, "Could not start the database copy");
                throw new RoundCreationUnavailableException(
                        "Round was created but the database copy could not be started",
                        e
                );
            }

            return new RoundCreationResult(
                    report,
                    RoundDto.from(round)
            );

        } finally {
            if (!lockTransferred) {
                lease.close();
            }
        }
    }

    /**
     * Performs the database copy outside a database transaction.
     */
    private void performCopy(
            Long roundId,
            String databaseName,
            RoundCreationLock.Lease lease
    ) {
        try {
            log.info(
                    "Starting database copy for round {} into {}",
                    roundId,
                    databaseName
            );

            databaseCopier.copyDatabase(databaseName);

            updateStatus(
                    roundId,
                    RoundStatus.COMPLETED,
                    null
            );

            log.info("Round {} completed successfully", roundId);

        } catch (Exception e) {
            log.error("Database copy failed for round {}", roundId, e);

            updateStatus(
                    roundId,
                    RoundStatus.FAILED,
                    errorMessage(e)
            );

        } finally {
            lease.close();
        }
    }

    private void updateStatus(
            Long roundId,
            RoundStatus status,
            String errorMessage
    ) {
        roundRepository.findById(roundId).ifPresent(round -> {
            round.setStatus(status);
            round.setCompletedAt(LocalDateTime.now());
            round.setErrorMessage(errorMessage);

            roundRepository.save(round);
        });
    }

    private void markFailed(Long roundId, String message) {
        updateStatus(roundId, RoundStatus.FAILED, message);
    }

    private String serializeReport(ValidationReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Could not serialize the validation report", e
            );
        }
    }

    private String errorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "An unexpected error occurred while copying the database";
        }

        int maxLength = 4000;

        return message.length() > maxLength
                ? message.substring(0, maxLength)
                : message;
    }

    /**
     * Returns all rounds, newest first.
     */
    public List<RoundDto> getRounds() {
        return roundRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(RoundDto::from)
                .toList();
    }

    /**
     * Returns one round, including its validation report.
     */
    public RoundDetailsDto getRound(Long id) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new RoundNotFoundException(id));

        JsonNode validationReport = null;

        if (round.getValidationReport() != null) {
            try {
                validationReport = objectMapper.readTree(
                        round.getValidationReport()
                );
            } catch (JsonProcessingException e) {
                log.error(
                        "Could not read validation report for round {}",
                        id,
                        e
                );
            }
        }

        return new RoundDetailsDto(
                round.getId(),
                round.getRoundName(),
                round.getDatabaseName(),
                round.getStatus(),
                round.isValidationPassed(),
                validationReport,
                round.getStartedAt(),
                round.getCompletedAt(),
                round.getErrorMessage(),
                round.getCreatedAt()
        );
    }

    /**
     * Dry-run validation. This does not create a round or database.
     */
    public ValidationReport validate() {
        return roundValidator.validate();
    }
}