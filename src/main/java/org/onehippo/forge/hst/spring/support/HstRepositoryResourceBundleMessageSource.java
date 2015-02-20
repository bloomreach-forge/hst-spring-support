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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.resourcebundle.ResourceBundleRegistry;
import org.hippoecm.hst.site.HstServices;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * {@link org.springframework.context.MessageSource} implementation that
 * accesses {@link Config#get(javax.servlet.ServletRequest, String)} to read the default
 * {@link LocalizationContext} which can be set by HST-2 Container (<code>LocalizationValve</code>).
 * (if {@link #isLocalizationContextResourceBundleEnabled()} returns true (by default))
 * to resolve {@link MessageFormat}.
 * If  falls back to the super class,
 * {@link ResourceBundleMessageSource}, if nothing found (from HST-2 Dynamic Resource Bundles).
 */
public class HstRepositoryResourceBundleMessageSource extends ResourceBundleMessageSource {

    /**
     * Flag whether or not the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}.
     */
    private boolean localizationContextResourceBundleEnabled = true;

    private boolean repositoryResourceBundleEnabled = true;

    /**
     * Zero-argument default constructor.
     */
    public HstRepositoryResourceBundleMessageSource() {
        super();
    }

    /**
     * Returns true if the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}.
     */
    public boolean isLocalizationContextResourceBundleEnabled() {
        return localizationContextResourceBundleEnabled;
    }

    /**
     * Sets the flag whether or not the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}.
     */
    public void setLocalizationContextResourceBundleEnabled(boolean localizationContextResourceBundleEnabled) {
        this.localizationContextResourceBundleEnabled = localizationContextResourceBundleEnabled;
    }

    public boolean isRepositoryResourceBundleEnabled() {
        return repositoryResourceBundleEnabled;
    }

    public void setRepositoryResourceBundleEnabled(boolean repositoryResourceBundleEnabled) {
        this.repositoryResourceBundleEnabled = repositoryResourceBundleEnabled;
    }

    /**
     * {@inheritDoc}
     * <p></p>
     * If {@link #isLocalizationContextResourceBundleEnabled()} returns true,
     * then it tries to find the default resource bundle from {@link LocalizationContext} first.
     * Otherwise or if not found, it proceeds to the default behavior.
     */
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        if (isLocalizationContextResourceBundleEnabled()) {
            HstRequestContext requestContext = RequestContextProvider.get();

            if (requestContext != null) {
                final LocalizationContext localizationContext = (LocalizationContext) Config.get(requestContext.getServletRequest(), Config.FMT_LOCALIZATION_CONTEXT);
                final ResourceBundle defaultResourceBundle = localizationContext.getResourceBundle();

                if (defaultResourceBundle != null) {
                    MessageFormat messageFormat = getMessageFormat(defaultResourceBundle, code, locale);

                    if (messageFormat != null) {
                        return messageFormat;
                    }
                }
            }
        }

        return super.resolveCode(code, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResourceBundle getResourceBundle(String basename, Locale locale) {
        if (isRepositoryResourceBundleEnabled() && HstServices.isAvailable()) {
            ResourceBundleRegistry resourceBundleRegistry =
                    HstServices.getComponentManager().getComponent(ResourceBundleRegistry.class.getName());

            if (resourceBundleRegistry != null) {
                final HstRequestContext requestContext = RequestContextProvider.get();
                boolean preview = (requestContext != null && requestContext.isPreview());
                ResourceBundle bundle = null;

                if (locale == null) {
                    bundle = (preview ? resourceBundleRegistry.getBundleForPreview(basename) : resourceBundleRegistry.getBundle(basename));
                } else {
                    bundle = (preview ? resourceBundleRegistry.getBundleForPreview(basename, locale) : resourceBundleRegistry.getBundle(basename, locale));
                }

                if (bundle != null) {
                    return bundle;
                }
            }
        }

        return super.getResourceBundle(basename, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
            throws MissingResourceException {
        String msg = getStringOrNull(bundle, code);

        if (msg != null) {
            MessageFormat result = createMessageFormat(msg, locale);
            return result;
        }

        return null;
    }

    private String getStringOrNull(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

}
