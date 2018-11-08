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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.session.ExpiringSession;

/**
 * {@link ExpiringSession} implementation simply by delegating call to the underlying container's {@link HttpSession}.
 */
public class HttpSessionDelegatingSession implements ExpiringSession, Serializable {

    private static final long serialVersionUID = 1L;

    static final String NAME = HttpSessionDelegatingSession.class.getName();

    private transient HttpSession httpSession;
    private long lastAccessedTime;

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
    public long getCreationTime() {
        return httpSession.getCreationTime();
    }

    @Override
    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public long getLastAccessedTime() {
        if (lastAccessedTime > 0) {
            return lastAccessedTime;
        }

        return httpSession.getLastAccessedTime();
    }

    @Override
    public void setMaxInactiveIntervalInSeconds(int interval) {
        httpSession.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveIntervalInSeconds() {
        return httpSession.getMaxInactiveInterval();
    }

    @Override
    public boolean isExpired() {
        if (System.currentTimeMillis() - getLastAccessedTime() > 1000L * (long) getMaxInactiveIntervalInSeconds()) {
            return true;
        }

        return false;
    }

}
