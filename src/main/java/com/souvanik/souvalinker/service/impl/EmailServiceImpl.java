package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.config.properties.AppProperties;
import com.souvanik.souvalinker.constants.MessageConstants;
import com.souvanik.souvalinker.service.EmailService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;


/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final SesV2Client sesV2Client;

    private final AppProperties appProperties;


    @Override
    public void sendVerificationEmail(String toEmail, String token) {

        String recipientHash = hashEmail(toEmail);

        logger.info("event=email_send_attempt type=VERIFY recipientHash={}", recipientHash);

        try {
            String link = buildVerificationLink(token);
            String body = buildVerificationEmailBody(link);

            sendEmail(toEmail, MessageConstants.VERIFY_EMAIL_SUBJECT, body);

            logger.info("event=email_send_success type=VERIFY recipientHash={}", recipientHash);

        } catch (Exception ex) {
            logger.error("event=email_send_failed type=VERIFY recipientHash={}", recipientHash, ex);
        }
    }

    @Async("emailExecutor")
    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {

        String recipientHash = hashEmail(toEmail);

        logger.info("event=email_send_attempt type=RESET recipientHash={}", recipientHash);

        try {
            String link = buildResetLink(token);
            String body = buildResetEmailBody(link);

            sendEmail(toEmail, MessageConstants.RESET_PASSWORD_SUBJECT, body);

            logger.info("event=email_send_success type=RESET recipientHash={}", recipientHash);

        } catch (Exception ex) {
            logger.error("event=email_send_failed type=RESET recipientHash={}", recipientHash, ex);
        }
    }


    private void sendEmail(String toEmail, String subject, String body) {

        SendEmailRequest request = buildEmailRequest(toEmail, subject, body);

        SendEmailResponse response = sesV2Client.sendEmail(request);

        logger.debug("event=ses_response messageId={}", response.messageId());
    }




    private SendEmailRequest buildEmailRequest(String toEmail, String subject, String body) {

        Destination destination = Destination.builder()
                .toAddresses(toEmail)
                .build();

        Content subjectContent = Content.builder()
                .data(subject)
                .build();

        Content bodyContent = Content.builder()
                .data(body)
                .build();

        Body emailBody = Body.builder()
                .text(bodyContent)
                .build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(emailBody)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(message)
                .build();

        return SendEmailRequest.builder()
                .fromEmailAddress(appProperties.mail().fromEmail())
                .destination(destination)
                .content(emailContent)
                .build();
    }



    private String buildVerificationLink(String token) {
        return appProperties.shortUrl().baseUrl() + "/verify?token=" + token;
    }

    private String buildResetLink(String token) {
        return appProperties.shortUrl().baseUrl() + "/reset-password?token=" + token;
    }

    private String buildVerificationEmailBody(String link) {
        return """
                Hi,

                Please verify your email by clicking the link below:

                %s

                If you did not request this, please ignore.

                Thanks,
                Your Team
                """.formatted(link);
    }

    private String buildResetEmailBody(String link) {
        return """
                Hi,

                Click below link to reset your password:

                %s

                If you did not request this, please ignore.

                Thanks,
                Your Team
                """.formatted(link);
    }


    private String hashEmail(String email) {
        return Integer.toHexString(email.hashCode());
    }
}
