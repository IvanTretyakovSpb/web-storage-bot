package com.tretyakov.service.impl;

import com.tretyakov.dao.AppUserDAO;
import com.tretyakov.dao.RawDataDAO;
import com.tretyakov.entity.AppUser;
import com.tretyakov.entity.RawData;
import com.tretyakov.entity.enums.UserState;
import com.tretyakov.service.MainService;
import com.tretyakov.service.ProducerService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.tretyakov.entity.enums.UserState.BASIC_STATE;
import static com.tretyakov.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static com.tretyakov.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getState();
        String text = update.getMessage().getText();
        var output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO добавить обработку email
        } else {
            log.error("Unknown user state: " + userState);
            output = "Unknown error! Enter /cancel and try again";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранение документа
        String answer = "Document has loaded successfully. Reference for downloading: " +
                "http://test.com/get-document/33";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранение фото
        String answer = "Photo has loaded successfully. Reference for downloading: " +
                "http://test.com/get-photo/77";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getState();
        if (!appUser.getIsActive()) {
            String error = "Register or activate your account to download content.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String error = "Cancel the current command with /cancel to send files.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //TODO добавить регистрацию
            return "Temporarily not available!";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Hello! For watch list of available commands enter /help";
        } else {
            return "Unknown command! For watch list of available commands enter /help";
        }
    }

    private String help() {
        return "List of available commands:\n"
                + "/cancel - cancel execution of the current command\n"
                + "/registration - registration of the new user";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Command is canceled!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistantAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistantAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistantAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
