package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.authentication.LoginRequest;
import ch.fhnw.timerecordingbackend.dto.authentication.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    void logout(String token);
}
