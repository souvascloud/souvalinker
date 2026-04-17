package com.souvanik.souvalinker.aspect;

import com.souvanik.souvalinker.annotation.ReadOnlyReplica;
import com.souvanik.souvalinker.config.DataSourceContextHolder;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Aspect
@Component
public class ReplicaRoutingAspect {

    @Before(
            "@annotation(readOnlyReplica)"
    )
    public void useReplica(
            ReadOnlyReplica readOnlyReplica) {

        DataSourceContextHolder
                .setReplica();
    }


    @After(
            "@annotation(readOnlyReplica)"
    )
    public void clearContext(
            ReadOnlyReplica readOnlyReplica) {

        DataSourceContextHolder
                .clear();
    }
}