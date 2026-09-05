package com.youfuns.cms.frontend;

import com.youfuns.cms.user.User;
import com.youfuns.cms.user.UserRegistrationPayload;

public class Main {
    public static void main(String[] args) {
        UserRegistrationPayload payload = new UserRegistrationPayload("Tom Doe", "doe@tommail.com", "+1 (234) 567-8910", "TomDoe1", "Xk9#mP2!qL");
        User user = new User(payload);
    }
}
