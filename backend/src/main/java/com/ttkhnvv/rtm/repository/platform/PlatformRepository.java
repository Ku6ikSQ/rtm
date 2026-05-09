package com.ttkhnvv.rtm.repository.platform;

import com.ttkhnvv.rtm.entity.platform.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PlatformRepository extends JpaRepository<Platform, UUID>,
        JpaSpecificationExecutor<Platform> {
}
