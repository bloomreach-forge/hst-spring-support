/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.hst.spring.support.session.servlet;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.session.Session;

/**
 * {@link Session} implementation simply by delegating call to the underlying container's {@link HttpSession}.
 */
public class HttpSessionDelegatingSession implements Session, Serializable {

    private static final long serialVersionUID = 1L;

    static final String NAME = HttpSessionDelegatingSession.class.getName();

    private transient HttpSession httpSession;
    private Instant lastAccessedTime;

    HttpSessionDelegatingSession(final HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public String getId() {
        return httpSession.getId();
    }

    @Override
    public <T> T getAttribute(String attributeName) {
        return (T) httpSession.getAttribute(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
        final Set<String> attrNames = new HashSet<>();

        for (Enumeration<String> e = httpSession.getAttributeNames(); e.hasMoreElements();) {
            attrNames.add(e.nextElement());
        }

        return Collections.unmodifiableSet(attrNames);
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        httpSession.setAttribute(attributeName, attributeValue);
    }

    @Override
    public void removeAttribute(String attributeName) {
        httpSession.removeAttribute(attributeName);
    }

    @Override
    public Instant getCreationTime() {
        return Instant.ofEpochMilli(httpSession.getCreationTime());
    }

    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public Instant getLastAccessedTime() {
        if (lastAccessedTime != null) {
            return lastAccessedTime;
        }

        return Instant.ofEpochMilli(httpSession.getLastAccessedTime());
    }

    @Override
    public void setMaxInactiveInterval(Duration interval) {
        httpSession.setMaxInactiveInterval((int) interval.getSeconds());
    }

    @Override
    public Duration getMaxInactiveInterval() {
        return Duration.ofSeconds(httpSession.getMaxInactiveInterval());
    }

    @Override
    public boolean isExpired() {
        final long lastAccessedTimeMillis = getLastAccessedTime().toEpochMilli();
        final long maxInactiveIntervalSeconds = getMaxInactiveInterval().getSeconds();

        if (System.currentTimeMillis() - lastAccessedTimeMillis > 1000L * maxInactiveIntervalSeconds) {
            return true;
        }

        return false;
    }

    @Override
    public String changeSessionId() {
        final HttpServletRequest request = HttpSessionDelegatingContext.getCurrentServletRequest();
        httpSession.invalidate();
        httpSession = request.getSession(true);
        return httpSession.getId();
    }

}
