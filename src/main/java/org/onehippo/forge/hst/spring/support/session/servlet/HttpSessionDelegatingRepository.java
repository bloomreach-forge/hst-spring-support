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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;

/**
 * {@link SessionRepository} implementation simply by delegating call to the underlying servlet container's {@link HttpSession}.
 */
public class HttpSessionDelegatingRepository implements SessionRepository<ExpiringSession> {

    @Override
    public ExpiringSession createSession() {
        final HttpServletRequest request = HttpSessionDelegatingContext.getCurrentServletRequest();

        if (request == null) {
            throw new IllegalStateException("HttpServletRequest not found yet.");
        }

        final HttpSession httpSession = request.getSession(true);
        final ExpiringSession session = new HttpSessionDelegatingSession(httpSession);
        httpSession.setAttribute(HttpSessionDelegatingSession.NAME, session);

        return session;
    }

    @Override
    public void save(ExpiringSession session) {
        // Nothing to do as container's session management does care of it.
    }

    @Override
    public ExpiringSession getSession(String id) {
        final HttpServletRequest request = HttpSessionDelegatingContext.getCurrentServletRequest();
        final HttpSession httpSession = (request != null) ? request.getSession(false) : null;

        if (httpSession == null) {
            return null;
        }

        ExpiringSession session = (ExpiringSession) httpSession.getAttribute(HttpSessionDelegatingSession.NAME);

        if (session != null) {
            if (!httpSession.getId().equals(id)) {
                session = null;
                httpSession.removeAttribute(HttpSessionDelegatingSession.NAME);
            }
        }

        return session;
    }

    @Override
    public void delete(String id) {
        // Nothing to do as container's session management does care of it.
    }
}
