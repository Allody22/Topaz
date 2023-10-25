package ru.nsu.carwash_server.services;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.repository.users.UserVersionsRepository;

import java.util.List;

@Service
@EnableTransactionManagement
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserVersionsRepository userVersionsRepository;

    public UserDetailsServiceImpl(UserVersionsRepository userVersionsRepository) {
        this.userVersionsRepository = userVersionsRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserVersions> userVersions = userVersionsRepository.findLatestVersionByUsername(username);
        UserVersions latestUserVersion;

        if (!userVersions.isEmpty()) {
            latestUserVersion = userVersions.get(0);
        } else {
            throw new NotInDataBaseException("пользователей не найден пользователь с телефоном: ", username);
        }

        var user = latestUserVersion.getUser();

        return UserDetailsImpl.build(user, latestUserVersion);
    }

}
