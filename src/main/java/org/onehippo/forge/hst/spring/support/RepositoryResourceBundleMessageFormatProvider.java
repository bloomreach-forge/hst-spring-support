/*
 * Copyright 2015-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.hst.spring.support;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Repository based ResourceBundle MessageFormat Provider.
 * <p>
 * This interface is responsible for creating and caching the resolved <code>MessageFormat</code> instances
 * and refreshing outdated <code>MessageFormat</code> instances.
 * </p>
 */
public interface RepositoryResourceBundleMessageFormatProvider {

    public void registerBundle(String basename, Locale locale, ResourceBundle bundle);

    /**
     * Return a MessageFormat for the given bundle and code,
     * fetching already generated MessageFormats from the cache.
     * @param bundle the ResourceBundle to work on
     * @param code the message code to retrieve
     * @param locale the Locale to use to build the MessageFormat
     * @return the resulting MessageFormat, or {@code null} if no message
     * defined for the given code
     */
    public MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale);

}
