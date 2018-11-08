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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Configures the basics for setting up Spring Session in a web environment by using {@link HttpSessionDelegatingRepository}.
 */
@Configuration
@EnableSpringHttpSession
public class HttpSessionDelegatingHttpSessionConfiguration extends SpringHttpSessionConfiguration {

    @Bean
    public HttpServletRequestAwareFilter servletRequestAwareFilter() {
        return new HttpServletRequestAwareFilter();
    }

    @Bean
    public HttpSessionDelegatingRepository sessionRepository() {
        return new HttpSessionDelegatingRepository();
    }

    /**
     * Setting {@link HttpServletRequest} in ThreadLocal to allow the next filter chain to be able to access
     * the underlying {@link HttpServletRequest} during request processing cycle.
     */
    public static class HttpServletRequestAwareFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            try {
                HttpSessionDelegatingContext.setCurrentServletRequest(request);
                chain.doFilter(request, response);
            } finally {
                HttpSessionDelegatingContext.clearCurrentServletRequest();
            }
        }
    }
}
