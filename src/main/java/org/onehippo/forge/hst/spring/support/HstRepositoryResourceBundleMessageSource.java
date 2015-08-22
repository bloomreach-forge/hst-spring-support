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
import org.onehippo.forge.hst.spring.support.util.HstLocalizationUtils;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * {@link org.springframework.context.MessageSource} implementation that
 * accesses {@link Config#get(javax.servlet.ServletRequest, String)} to read the default
 * {@link LocalizationContext} which can be set by HST-2 Container ({@code LocalizationValve}).
 * (if {@link #isLocalizationContextResourceBundleEnabled()} returns true (by default))
 * to resolve {@link MessageFormat}.
 * If  falls back to the super class,
 * {@link ResourceBundleMessageSource}, if nothing found (from HST-2 Dynamic Resource Bundles).
 */
public class HstRepositoryResourceBundleMessageSource extends ResourceBundleMessageSource {

    /**
     * Flag whether or not the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}
     * in {@link #resolveCode(String, Locale)}.
     * It's true by default.
     */
    private boolean localizationContextResourceBundleEnabled = true;

    /**
     * Flag whether or not the repository resource bundle documents should be looked up
     * in {@link #getResourceBundle(String, Locale)}.
     * It's true by default.
     */
    private boolean repositoryResourceBundleEnabled = true;

    private RepositoryResourceBundleMessageFormatProvider resourceBundleMessageFormatProvider = new CachingRepositoryResourceBundleMessageFormatProvider();

    /**
     * Zero-argument default constructor.
     */
    public HstRepositoryResourceBundleMessageSource() {
        super();
    }

    /**
     * Returns true if the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}.
     * @return true if the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}
     */
    public boolean isLocalizationContextResourceBundleEnabled() {
        return localizationContextResourceBundleEnabled;
    }

    /**
     * Sets the flag whether or not the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}.
     * @param localizationContextResourceBundleEnabled the flag whether or not the default resource bundle should be found from {@link LocalizationContext}
     * by calling on {@link Config#get(javax.servlet.ServletRequest, String)}
     */
    public void setLocalizationContextResourceBundleEnabled(boolean localizationContextResourceBundleEnabled) {
        this.localizationContextResourceBundleEnabled = localizationContextResourceBundleEnabled;
    }

    /**
     * Returns true if the repository resource bundle documents should be looked up
     * in {@link #getResourceBundle(String, Locale)}.
     * @return true if the repository resource bundle documents should be looked up
     * in {@link #getResourceBundle(String, Locale)}
     */
    public boolean isRepositoryResourceBundleEnabled() {
        return repositoryResourceBundleEnabled;
    }

    /**
     * Sets the flag whether or not the repository resource bundle documents should be looked up
     * in {@link #getResourceBundle(String, Locale)}.
     * @param repositoryResourceBundleEnabled the flag whether or not the repository resource bundle documents should be looked up
     * in {@link #getResourceBundle(String, Locale)}
     */
    public void setRepositoryResourceBundleEnabled(boolean repositoryResourceBundleEnabled) {
        this.repositoryResourceBundleEnabled = repositoryResourceBundleEnabled;
    }

    /**
     * Returns {@link RepositoryResourceBundleMessageFormatProvider} internally used for
     * maintaining repository-based resource bundles and message formats from them.
     * @return {@link RepositoryResourceBundleMessageFormatProvider} internally used for
     * maintaining repository-based resource bundles and message formats from them
     */
    public RepositoryResourceBundleMessageFormatProvider getResourceBundleMessageFormatProvider() {
        return resourceBundleMessageFormatProvider;
    }

    /**
     * Sets {@link RepositoryResourceBundleMessageFormatProvider} internally to be used for
     * maintaining repository-based resource bundles and message formats from them.
     * @param resourceBundleMessageFormatProvider {@link RepositoryResourceBundleMessageFormatProvider} internally to be used for
     * maintaining repository-based resource bundles and message formats from them
     */
    public void setResourceBundleMessageFormatProvider(
            RepositoryResourceBundleMessageFormatProvider resourceBundleMessageFormatProvider) {
        this.resourceBundleMessageFormatProvider = resourceBundleMessageFormatProvider;
    }

    /**
     * {@inheritDoc}
     *
     * If {@link #isLocalizationContextResourceBundleEnabled()} returns true,
     * then it tries to find the default resource bundle from {@link LocalizationContext} first.
     * Otherwise or if not found, it proceeds with the default behavior.
     *
     * @param code message code
     * @param locale message locale
     * @return message resolved by the code and locale
     */
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        if (isLocalizationContextResourceBundleEnabled()) {
            ResourceBundle defaultResourceBundle = findDefaultResourceBundle();

            if (defaultResourceBundle != null) {
                String message = getStringOrNull(defaultResourceBundle, code);

                if (message != null) {
                    return message;
                }
            }
        }

        return super.resolveCodeWithoutArguments(code, locale);
    }

    /**
     * {@inheritDoc}
     *
     * If {@link #isLocalizationContextResourceBundleEnabled()} returns true,
     * then it tries to find the default resource bundle from {@link LocalizationContext} first.
     * Otherwise or if not found, it proceeds with the default behavior.
     *
     * @param code message code
     * @param locale message locale
     * @return message format resolved by code and locale
     */
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        if (isLocalizationContextResourceBundleEnabled()) {
            ResourceBundle defaultResourceBundle = findDefaultResourceBundle();

            if (defaultResourceBundle != null) {
                MessageFormat messageFormat = getMessageFormatFromDefaultResourceBundle(defaultResourceBundle, code, locale);

                if (messageFormat != null) {
                    return messageFormat;
                }
            }
        }

        return super.resolveCode(code, locale);
    }

    /**
     * {@inheritDoc}
     *
     * If {@link #isRepositoryResourceBundleEnabled()} returns true, then it first looks up
     * resource bundle from the repository.
     * Otherwise or if not found, it proceeds with the default behavior.
     *
     * @param basename resource bundle basename
     * @param locale resource bundle locale
     * @return resource bundle
     */
    @Override
    protected ResourceBundle getResourceBundle(String basename, Locale locale) {
        if (isRepositoryResourceBundleEnabled() && HstServices.isAvailable()) {
            ResourceBundleRegistry resourceBundleRegistry =
                    HstServices.getComponentManager().getComponent(ResourceBundleRegistry.class.getName());

            if (resourceBundleRegistry != null) {
                final HstRequestContext requestContext = RequestContextProvider.get();
                final boolean preview = requestContext != null && requestContext.isPreview();
                ResourceBundle bundle = null;

                if (locale == null) {
                    bundle = (preview ? resourceBundleRegistry.getBundleForPreview(basename) : resourceBundleRegistry.getBundle(basename));
                } else {
                    bundle = (preview ? resourceBundleRegistry.getBundleForPreview(basename, locale) : resourceBundleRegistry.getBundle(basename, locale));
                }

                if (bundle != null) {
                    if (preview) {
                        resourceBundleMessageFormatProvider.registerPreviewBundle(basename, locale, bundle);
                    } else {
                        resourceBundleMessageFormatProvider.registerBundle(basename, locale, bundle);
                    }

                    return bundle;
                }
            }
        }

        return super.getResourceBundle(basename, locale);
    }

    /**
     * Return a MessageFormat for the given bundle and code,
     * fetching already generated MessageFormats from the cache.
     *
     * @param bundle resource bundle
     * @param code message code
     * @param locale message locale
     * @return message format resolved
     */
    @Override
    protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
            throws MissingResourceException {
        MessageFormat messageFormat = resourceBundleMessageFormatProvider.getMessageFormat(bundle, code, locale);

        if (messageFormat != null) {
            return messageFormat;
        }

        return super.getMessageFormat(bundle, code, locale);
    }

    /**
     * Invokes {@link RepositoryResourceBundleMessageFormatProvider#registerBundle(String, Locale, ResourceBundle)}
     * or {@link RepositoryResourceBundleMessageFormatProvider#registerPreviewBundle(String, Locale, ResourceBundle)}
     * depending on the current request context in order to register the bundle or remove any existing outdated bundle.
     * And, it invokes {@link #getMessageFormat(ResourceBundle, String, Locale)} to return a MessageFormat.
     *
     * @param defaultResourceBundle default resource bundle
     * @param code message code
     * @param locale message locale
     * @return message format resolved
     */
    protected MessageFormat getMessageFormatFromDefaultResourceBundle(ResourceBundle defaultResourceBundle, String code, Locale locale) {
        HstRequestContext requestContext = RequestContextProvider.get();
        final boolean preview = requestContext != null && requestContext.isPreview();

        if (defaultResourceBundle != null) {
            // Use empty string basename for the default localization context resource bundle.
            if (preview) {
                resourceBundleMessageFormatProvider.registerPreviewBundle("", locale, defaultResourceBundle);
            } else {
                resourceBundleMessageFormatProvider.registerBundle("", locale, defaultResourceBundle);
            }

            return getMessageFormat(defaultResourceBundle, code, locale);
        }

        return null;
    }

    /**
     * Finds the default {@code LocalizationContext}'s {@code ResourceBundle} set by HST-2 Container in the frontend pipeline.
     * <p>
     * It tries to find it from the JSTL {@code LocalizationContext}. However, {@code JstlView} (and {@code JstlUtils}) of Spring Framework
     * may replace the {@code LocalizationContext} by its {@code MessageSourceResourceBundle}.
     * In that case, it tries to resolve the default resource bundle(s) configured in HST-2 configurations again.
     * </p>
     * @return default resource bundle
     */
    protected ResourceBundle findDefaultResourceBundle() {
        HstRequestContext requestContext = RequestContextProvider.get();

        if (requestContext != null) {
            final LocalizationContext localizationContext = (LocalizationContext) Config.get(requestContext.getServletRequest(), Config.FMT_LOCALIZATION_CONTEXT);

            if (localizationContext != null) {
                ResourceBundle defaultResourceBundle = localizationContext.getResourceBundle();

                if (defaultResourceBundle != null) {
                    if (defaultResourceBundle instanceof MessageSourceResourceBundle) {
                        // JSTL LocalizationContext has been modified by Spring Framework JstlView / JstUtils.
                        // Because MessageSourceResourceBundle is just a wrapper of this MessageSource implementation,
                        // it will cause infinte self-recursive call if you use it.
                        // So, we will need to find the LocalizationContext set by HST-2 Container.
                        defaultResourceBundle = HstLocalizationUtils.getCurrentDefaultResourceBundle();

                        if (defaultResourceBundle != null) {
                            return defaultResourceBundle;
                        }
                    } else {
                        return defaultResourceBundle;
                    }
                }

                return null;
            }
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
