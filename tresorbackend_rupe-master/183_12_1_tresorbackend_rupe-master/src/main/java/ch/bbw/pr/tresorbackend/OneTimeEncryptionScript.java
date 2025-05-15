package ch.bbw.pr.tresorbackend;

import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import ch.bbw.pr.tresorbackend.service.UserService;

import java.security.NoSuchAlgorithmException;

public class OneTimeEncryptionScript {
    private final UserRepository userRepository;
    private final PasswordEncryptionService passwordEncryptionService;

    public OneTimeEncryptionScript(UserRepository userRepository, PasswordEncryptionService passwordEncryptionService) {
        this.userRepository = userRepository;
        this.passwordEncryptionService = passwordEncryptionService;
    }

    public void EncryptPasswords() throws NoSuchAlgorithmException {
        var users = userRepository.findAll();
        for(var user : users){
            var hashedPassword = passwordEncryptionService.encryptPassword(user.getPassword());
            user.setPassword(hashedPassword);
            userRepository.save(user);
        }
    }
}
