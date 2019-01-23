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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.session.Session;

/**
 * {@link Session} implementation simply by delegating call to the underlying container's {@link HttpSession}.
 */
public class HttpSessionDelegatingSession implements Session, Serializable {

    private static final long serialVersionUID = 1L;

    static final String NAME = HttpSessionDelegatingSession.class.getName();

    private static final String SESSION_ATTRS_MAP_KEY = HttpSessionDelegatingSession.class.getName() + ".sessionAttrsMap";

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
        final Map<String, Object> sessionAttrs = (Map<String, Object>) httpSession.getAttribute(SESSION_ATTRS_MAP_KEY);
        return (sessionAttrs != null) ? (T) sessionAttrs.get(attributeName) : null;
    }

    @Override
    public Set<String> getAttributeNames() {
        final Map<String, Object> sessionAttrs = (Map<String, Object>) httpSession.getAttribute(SESSION_ATTRS_MAP_KEY);

        if (sessionAttrs == null) {
            return Collections.emptySet();
        }

        Set<String> attrNames;

        synchronized (sessionAttrs) {
            attrNames = new HashSet<>(sessionAttrs.keySet());
        }

        return Collections.unmodifiableSet(attrNames);
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        Map<String, Object> sessionAttrs = (Map<String, Object>) httpSession.getAttribute(SESSION_ATTRS_MAP_KEY);

        if (sessionAttrs == null) {
            sessionAttrs = new ConcurrentHashMap<>();
            httpSession.setAttribute(SESSION_ATTRS_MAP_KEY, sessionAttrs);
        }

        sessionAttrs.put(attributeName, attributeValue);
    }

    @Override
    public void removeAttribute(String attributeName) {
        final Map<String, Object> sessionAttrs = (Map<String, Object>) httpSession.getAttribute(SESSION_ATTRS_MAP_KEY);

        if (sessionAttrs != null) {
            sessionAttrs.remove(attributeName);
        }
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
