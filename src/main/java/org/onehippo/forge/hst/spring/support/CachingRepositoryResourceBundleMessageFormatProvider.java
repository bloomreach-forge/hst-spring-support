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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.util.DefaultKeyValue;
import org.hippoecm.hst.util.KeyValue;
import org.springframework.context.support.MessageSourceSupport;

/**
 * {@link RepositoryResourceBundleMessageFormatProvider} implementation providing
 * <code>MessageFormat</code> caching per <code>ResourceBundle</code>, resource key and locale.
 * Also, it maintain the cache separately based on preview/live request context.
 */
public class CachingRepositoryResourceBundleMessageFormatProvider extends MessageSourceSupport implements RepositoryResourceBundleMessageFormatProvider {

    /**
     * Cache to hold already generated live MessageFormats.
     * This Map is keyed with the ResourceBundle, which holds a Map that is
     * keyed with the message code, which in turn holds a Map that is keyed
     * with the Locale and holds the MessageFormat values.
     * @see #getMessageFormat
     */
    private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMessageFormats =
            new HashMap<ResourceBundle, Map<String, Map<Locale, MessageFormat>>>();

    /**
     * Cache to hold basename and locale per live resource bundle.
     */
    private final Map<ResourceBundle, KeyValue<String, Locale>> bundleBasenameLocales =
            new HashMap<ResourceBundle, KeyValue<String, Locale>>();

    /**
     * Cache to hold live resource bundles per each basename and locale.
     */
    private final Map<KeyValue<String, Locale>, ResourceBundle> basenameLocaleBundles =
            new HashMap<KeyValue<String, Locale>, ResourceBundle>();

    /**
     * Cache to hold already generated preview MessageFormats.
     * This Map is keyed with the ResourceBundle, which holds a Map that is
     * keyed with the message code, which in turn holds a Map that is keyed
     * with the Locale and holds the MessageFormat values.
     * @see #getMessageFormat
     */
    private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMessageFormatsForPreview =
            new HashMap<ResourceBundle, Map<String, Map<Locale, MessageFormat>>>();

    /**
     * Cache to hold basename and locale per preview resource bundle.
     */
    private final Map<ResourceBundle, KeyValue<String, Locale>> bundleBasenameLocalesForPreview =
            new HashMap<ResourceBundle, KeyValue<String, Locale>>();

    /**
     * Cache to hold preview resource bundles per each basename and locale.
     */
    private final Map<KeyValue<String, Locale>, ResourceBundle> basenameLocaleBundlesForPreview =
            new HashMap<KeyValue<String, Locale>, ResourceBundle>();

    /**
     * {@inheritDoc}
     */
    public void registerBundle(String basename, Locale locale, ResourceBundle bundle) {
        KeyValue<String, Locale> pair = new DefaultKeyValue<String, Locale>(basename, locale);

        synchronized (cachedBundleMessageFormats) {
            ResourceBundle oldBundle = basenameLocaleBundles.get(pair);

            if (oldBundle != bundle) {
                if (oldBundle != null) {
                    cachedBundleMessageFormats.remove(oldBundle);
                    bundleBasenameLocales.remove(oldBundle);
                }

                cachedBundleMessageFormats.put(bundle, new HashMap<String, Map<Locale, MessageFormat>>());
                bundleBasenameLocales.put(bundle, pair);
                basenameLocaleBundles.put(pair, bundle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerPreviewBundle(String basename, Locale locale, ResourceBundle bundle) {
        KeyValue<String, Locale> pair = new DefaultKeyValue<String, Locale>(basename, locale);

        synchronized (cachedBundleMessageFormatsForPreview) {
            ResourceBundle oldBundle = basenameLocaleBundlesForPreview.get(pair);

            if (oldBundle != bundle) {
                if (oldBundle != null) {
                    cachedBundleMessageFormatsForPreview.remove(oldBundle);
                    bundleBasenameLocalesForPreview.remove(oldBundle);
                }

                cachedBundleMessageFormatsForPreview.put(bundle, new HashMap<String, Map<Locale, MessageFormat>>());
                bundleBasenameLocalesForPreview.put(bundle, pair);
                basenameLocaleBundlesForPreview.put(pair, bundle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale) {
        Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> contextCachedBundleMessageFormats = cachedBundleMessageFormats;
        Map<ResourceBundle, KeyValue<String, Locale>> contextBundleBasenameLocales = bundleBasenameLocales;

        final HstRequestContext requestContext = RequestContextProvider.get();
        final boolean preview = requestContext != null && requestContext.isPreview();

        if (preview) {
            contextCachedBundleMessageFormats = cachedBundleMessageFormatsForPreview;
            contextBundleBasenameLocales = bundleBasenameLocalesForPreview;
        }

        synchronized (contextCachedBundleMessageFormats) {
            if (!contextBundleBasenameLocales.containsKey(bundle)) {
                return null;
            }

            Map<String, Map<Locale, MessageFormat>> codeMap = contextCachedBundleMessageFormats.get(bundle);
            Map<Locale, MessageFormat> localeMap = codeMap.get(code);

            if (localeMap != null) {
                MessageFormat result = localeMap.get(locale);

                if (result != null) {
                    return result;
                }
            }

            String msg = getStringOrNull(bundle, code);

            if (msg != null) {
                if (localeMap == null) {
                    localeMap = new HashMap<Locale, MessageFormat>();
                    codeMap.put(code, localeMap);
                }

                MessageFormat result = createMessageFormat(msg, locale);
                localeMap.put(locale, result);

                return result;
            }

            return null;
        }
    }

    /**
     * Test purpose getter.
     * @return
     */
    protected Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> getCachedBundleMessageFormats() {
        return cachedBundleMessageFormats;
    }

    /**
     * Test purpose getter.
     * @return
     */
    protected Map<ResourceBundle, KeyValue<String, Locale>> getBundleBasenameLocales() {
        return bundleBasenameLocales;
    }

    /**
     * Test purpose getter.
     * @return
     */
    protected Map<KeyValue<String, Locale>, ResourceBundle> getBasenameLocaleBundles() {
        return basenameLocaleBundles;
    }

    /**
     * Test purpose getter.
     * @return
     */
    protected Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> getCachedBundleMessageFormatsForPreview() {
        return cachedBundleMessageFormatsForPreview;
    }

    /**
     * Test purpose getter.
     * @return
     */
    protected Map<ResourceBundle, KeyValue<String, Locale>> getBundleBasenameLocalesForPreview() {
        return bundleBasenameLocalesForPreview;
    }

    /**
     * Test purpose getter.
     * @return
     */
    protected Map<KeyValue<String, Locale>, ResourceBundle> getBasenameLocaleBundlesForPreview() {
        return basenameLocaleBundlesForPreview;
    }

    private String getStringOrNull(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

}
