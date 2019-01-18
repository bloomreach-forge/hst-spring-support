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

import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

/**
 * {@link SessionRepository} implementation simply by delegating call to the underlying servlet container's {@link HttpSession}.
 */
public class HttpSessionDelegatingRepository implements SessionRepository<Session> {

    @Override
    public Session createSession() {
        final HttpServletRequest request = HttpSessionDelegatingContext.getCurrentServletRequest();

        if (request == null) {
            throw new IllegalStateException("HttpServletRequest not found yet.");
        }

        final HttpSession httpSession = request.getSession(true);
        final Session session = new HttpSessionDelegatingSession(httpSession);
        httpSession.setAttribute(HttpSessionDelegatingSession.NAME, session);

        return session;
    }

    @Override
    public void save(Session session) {
        // Nothing to do as container's session management does care of it.
    }

    @Override
    public Session findById(String id) {
        final HttpServletRequest request = HttpSessionDelegatingContext.getCurrentServletRequest();
        final HttpSession httpSession = (request != null) ? request.getSession(false) : null;

        if (httpSession == null) {
            return null;
        }

        Session session = (Session) httpSession.getAttribute(HttpSessionDelegatingSession.NAME);

        if (session != null) {
            // If previous JSESSIONID cookie in browser's in-memory session remains while tomcat gets restarted,
            // then we need to recreate Session again.
            if (!httpSession.getId().equals(id)) {
                session = new HttpSessionDelegatingSession(httpSession);
                httpSession.setAttribute(HttpSessionDelegatingSession.NAME, session);
            }
        }

        return session;
    }

    @Override
    public void deleteById(String id) {
        // Nothing to do as container's session management does care of it.
    }
}
