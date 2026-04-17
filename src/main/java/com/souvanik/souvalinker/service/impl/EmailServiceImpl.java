package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.config.AppProperties;
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

    @Async("emailExecutor")
    @Override
    public void sendVerificationEmail(String toEmail, String token) {

        logger.info("Sending verification email to {}", toEmail);

        String body = buildVerificationEmailBody(token);

        sendEmail(toEmail, MessageConstants.VERIFY_EMAIL_SUBJECT, body);
    }


    @Async("emailExecutor")
    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {

        logger.info("Sending password reset email to {}", toEmail);

        String body = buildResetEmailBody(token);

        sendEmail(toEmail, MessageConstants.RESET_PASSWORD_SUBJECT, body);
    }



    private void sendEmail(String toEmail, String subject, String body) {

        try {

            SendEmailRequest request = buildEmailRequest(toEmail, subject, body);


            SendEmailResponse response = sesV2Client.sendEmail(request);


            logger.info("Email sent successfully. messageId={}", response.messageId());

        } catch (SesV2Exception ex) {

            logger.error("SES email send failed for {}", toEmail, ex);
        }
    }



    private SendEmailRequest buildEmailRequest(String toEmail, String subject, String body) {

        Destination destination = Destination.builder().toAddresses(toEmail).build();
        Content subjectContent = Content.builder().data(subject).build();
        Content bodyContent = Content.builder().data(body).build();

        Body emailBody = Body.builder().text(bodyContent).build();


        Message message = Message.builder().subject(subjectContent).body(emailBody).build();


        EmailContent emailContent = EmailContent.builder().simple(message).build();


        return SendEmailRequest.builder()
                .fromEmailAddress(
                        appProperties
                                .mail()
                                .fromEmail()
                )
                .destination(destination)
                .content(emailContent)
                .build();
    }



    private String buildVerificationEmailBody(
            String token) {

        return """
               Please verify your email.

               Verification token:
               %s
               """.formatted(
                token
        );
    }



    private String buildResetEmailBody(
            String token) {

        return """
               Reset your password.

               Reset token:
               %s
               """.formatted(
                token
        );
    }
}
