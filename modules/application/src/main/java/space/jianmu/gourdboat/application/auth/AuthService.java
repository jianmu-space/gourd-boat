package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.application.auth.command.LoginCommand;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;

public interface AuthService {
    LoginResult login(LoginCommand command);
} 