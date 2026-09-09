package com.youfuns.cms;

import com.youfuns.auth.login.UserCredentials;
import com.youfuns.auth.rbac.UserRole;
import com.youfuns.auth.rbac.dummy.AlwaysTruePermissions;
import com.youfuns.auth.rbac.UserRoleHolder;
import com.youfuns.auth.rbac.dummy.EmptyPerm;

import java.util.Set;
import java.util.UUID;

public class CredsDemo {
    public static void main(String[] args) {
        Set<UserRole<EmptyPerm>> roles = Set.of(new UserRole<>("TEST", Set.of(EmptyPerm.ALL)));
        AlwaysTruePermissions<EmptyPerm> perms = new AlwaysTruePermissions<>(roles);

        perms.assignToClasses(UserCredentials.class, UserRoleHolder.class);

        UserCredentials userCredentials = new UserCredentials(UUID.randomUUID(), Set.of("TestUser"), "X*fo*@rnc79y#^*F");
        UserCredentials.LoginResult result = userCredentials.login("TestUser", "wrong");
        System.out.println(result);
        UserCredentials.LoginResult result2 = userCredentials.login("TestUser", "X*fo*@rnc79y#^*F");
        System.out.println(result2);
        for (int i = 0; i < 6; i++) {
            UserCredentials.LoginResult wrong = userCredentials.login("TestUser", "wrong");
            System.out.println(wrong);
        }
        UserCredentials.LoginResult result3 = userCredentials.login("TestUser", "X*fo*@rnc79y#^*F");
        System.out.println(result3);
        userCredentials.unlockAccount(perms.issueToken(null));
        UserCredentials.LoginResult result4 = userCredentials.login("TestUser", "X*fo*@rnc79y#^*F");
        System.out.println(result4);

        System.out.println(userCredentials.changePassword(perms.issueToken(null), "wrongold", "new_X*fo*@rnc79y#^*F"));
        System.out.println(userCredentials.changePassword(perms.issueToken(null), "X*fo*@rnc79y#^*F", "new_X*fo*@rnc79y#^*F"));
        System.out.println(userCredentials.login("TestUser", "X*fo*@rnc79y#^*F"));
        System.out.println(userCredentials.login("TestUser", "new_X*fo*@rnc79y#^*F"));

    }
}
