package com.ttkhnvv.rtm.security.util;

import com.ttkhnvv.rtm.security.UserPrincipal;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public class SecurityUtils {
    public static UUID getCurrentUserId() {
        var principal = getCurrentPrincipal();
        if (principal == null)
            return null;
        return principal.getUser().getId();
    }

    public static UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null)
            return null;
        return (UserPrincipal) authentication.getPrincipal();
    }
}