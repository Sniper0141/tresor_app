package ch.bbw.pr.tresorbackend.script;

import ch.bbw.pr.tresorbackend.controller.UserController;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class OneTimeEncryptionScript {
    private final UserRepository userRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private static final Logger logger = LoggerFactory.getLogger(OneTimeEncryptionScript.class);

    public OneTimeEncryptionScript(UserRepository userRepository, PasswordEncryptionService passwordEncryptionService) {
        this.userRepository = userRepository;
        this.passwordEncryptionService = passwordEncryptionService;
    }

    public void EncryptPasswords() throws NoSuchAlgorithmException {
        try{
            var users = userRepository.findAll();
            for(var user : users){
                var hashedPassword = passwordEncryptionService.encryptPassword(user.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
            }
        } catch(Exception e){
            logger.error(e.getMessage());
        }
    }
}
