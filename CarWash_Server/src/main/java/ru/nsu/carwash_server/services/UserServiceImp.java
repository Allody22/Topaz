package ru.nsu.carwash_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.repository.users.UserRepository;
import ru.nsu.carwash_server.repository.users.UserVersionsRepository;
import ru.nsu.carwash_server.services.interfaces.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;

    private final UserVersionsRepository userVersionsRepository;


    @Autowired
    public UserServiceImp(UserVersionsRepository userVersionsRepository,
                          UserRepository userRepository) {
        this.userVersionsRepository = userVersionsRepository;
        this.userRepository = userRepository;
    }

    public void saveNewUser(User user, Set<Role> roles, int version, UserVersions userFirstVersion) {
        user.setDateOfCreation(new Date());
        user.setRoles(roles);
        userFirstVersion.setVersion(version);
        userFirstVersion.setUser(user);
        user.addUserVersion(userFirstVersion);

        userRepository.save(user);
    }

    public boolean existByPhone(String username) {
        return userVersionsRepository.existsByPhone(username);
    }

    public User getFullUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotInDataBaseException("пользователей не найден пользователь с айди : ", userId.toString()));

    }

    public List<String> getAllActualPhones() {
        List<User> userList = userRepository.getAllUsers();
        List<String> usernames = new ArrayList<>();
        for (var user : userList) {
            usernames.add(getActualUserVersionById(user.getId()).getPhone());
        }
        return usernames;
    }

    public UserVersions getActualUserVersionByPhone(String username) {
        var userVersionList = userVersionsRepository.findLatestVersionByUsername
                (username);
        UserVersions latestUserVersion;
        if (!userVersionList.isEmpty()) {
            latestUserVersion = userVersionList.get(0);
        } else {
            throw new NotInDataBaseException("пользователей не найден" +
                    " пользователь с именем: ", username);
        }
        return latestUserVersion;
    }

    public UserVersions getActualUserVersionById(Long id) {
        var userVersionList = userVersionsRepository.findLatestVersionByUserId(id);
        UserVersions latestUserVersion;
        if (!userVersionList.isEmpty()) {
            latestUserVersion = userVersionList.get(0);
        } else {
            throw new NotInDataBaseException("пользователей не найден" +
                    " пользователь с айди: ", id.toString());
        }
        return latestUserVersion;
    }
}
