package com.tretyakov.service.impl;

import com.tretyakov.dao.AppUserDAO;
import com.tretyakov.dto.MailParams;
import com.tretyakov.entity.AppUser;
import com.tretyakov.entity.enums.UserState;
import com.tretyakov.service.AppUserService;
import com.tretyakov.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;

@Log4j
@Service
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "You are already registered!";
        } else if (appUser.getEmail() != null) {
            return "A letter has already been sent to your email. " +
                    "Follow the link in the letter to confirm your registration.";
        }
        appUser.setState(UserState.WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser);
        return "Please, enter your email address:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException e) {
            return "Please, enter a valid email address. To cancel the command, enter /cancel";
        }
        Optional<AppUser> optionalAppUser = appUserDAO.findByEmail(email);
        if (optionalAppUser.isEmpty()) {
            appUser.setEmail(email);
            appUser.setState(UserState.BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            String cryptoUserId = cryptoTool.hashOf(appUser.getId());
            ResponseEntity<String> response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                String message = String.format("Sending email to mail %s failed", email);
                log.error(message);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return message;
            }
            return "An email has been sent to your email address. " +
                    "Follow the link in the letter to confirm your registration.";
        } else {
            return "This email address is already in use! Please, enter a valid email address. " +
                    "To cancel the command, enter /cancel";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MailParams mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        HttpEntity<MailParams> request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
