package com.tretyakov.service.impl;

import com.tretyakov.dao.AppUserDAO;
import com.tretyakov.entity.AppUser;
import com.tretyakov.service.UserActivationService;
import com.tretyakov.utils.CryptoTool;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        Long userId = cryptoTool.idOf(cryptoUserId);
        Optional<AppUser> optionalAppUser = appUserDAO.findById(userId);
        if (optionalAppUser.isPresent()) {
            AppUser user = optionalAppUser.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
}
