package com.mavuno.famers.union.mavuno.services;

import com.mavuno.famers.union.mavuno.entities.UserEntity;
import com.mavuno.famers.union.mavuno.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class UserService implements UserDetailsService {



    private final UserRepository userRepository;

    private HashMap<String, Object> response = new HashMap<>();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }





    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email);
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}