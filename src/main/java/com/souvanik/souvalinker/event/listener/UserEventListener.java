package com.souvanik.souvalinker.event.listener;

import com.souvanik.souvalinker.event.PasswordChangedEvent;
import com.souvanik.souvalinker.event.PasswordResetEvent;
import com.souvanik.souvalinker.event.UserRegisteredEvent;
import com.souvanik.souvalinker.service.EmailService;
import com.souvanik.souvalinker.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);
    private final EmailService emailService;

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {

        logger.info("event=user_registered_listener_triggered email={}", event.email());

        emailService.sendVerificationEmail(event.email(), event.token());
    }

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordReset(PasswordResetEvent event) {

        logger.info("event=password_reset_listener_triggered emailHash={}",
                Integer.toHexString(event.email().hashCode()));

        emailService.sendPasswordResetEmail(
                event.email(),
                event.token()
        );
    }

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(PasswordChangedEvent event) {

        emailService.sendPasswordChangedEmail(event.email());
    }
    
}
