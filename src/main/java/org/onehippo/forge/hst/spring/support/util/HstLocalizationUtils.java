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
package org.onehippo.forge.hst.spring.support.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.resourcebundle.CompositeResourceBundle;
import org.hippoecm.hst.resourcebundle.ResourceBundleRegistry;
import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HST-2 Localization related utilities.
 */
public class HstLocalizationUtils {

    private static Logger log = LoggerFactory.getLogger(HstLocalizationUtils.class);

    static final String DEFAULT_RESOURCE_BUNDLE_ATTR = HstLocalizationUtils.class.getName() + ".defaultResourceBundle";

    private static final Object[][] EMPTY_RESOURCE_BUNDLE_CONTENTS = new Object[0][2];

    private static final ResourceBundle EMPTY_RESOURCE_BUNDLE = new ListResourceBundle() {
        @Override
        protected Object[][] getContents() {
            return EMPTY_RESOURCE_BUNDLE_CONTENTS;
        }
    };

    private HstLocalizationUtils() {
    }

    /**
     * Resolve and return the default resource bundle(s) configured in HST-2 configurations.
     * <p>
     * For performance reason, the resolved default resource bundle is stored in HstRequestContext attribute
     * not to resolve again in the same request processing cycle.
     * </p>
     * @return current default resource bundle
     */
    public static ResourceBundle getCurrentDefaultResourceBundle() {
        return getCurrentDefaultResourceBundle(null);
    }

    /**
     * Resolve and return the default resource bundle(s) configured in HST-2 configurations.
     * <p>
     * For performance reason, the resolved default resource bundle is stored in HstRequestContext attribute
     * not to resolve again in the same request processing cycle.
     * </p>
     * @param defaultBundleWhenNotFound default resource bundle to use when no bundle found.
     * @return current default resource bundle
     */
    public static ResourceBundle getCurrentDefaultResourceBundle(final ResourceBundle defaultBundleWhenNotFound) {
        HstRequestContext requestContext = RequestContextProvider.get();

        if (requestContext == null) {
            return null;
        }

        ResourceBundle defaultResourceBundle = (ResourceBundle) requestContext.getAttribute(DEFAULT_RESOURCE_BUNDLE_ATTR);

        if (defaultResourceBundle != null) {
            return defaultResourceBundle;
        }

        List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();

        if (requestContext.getResolvedMount() != null) {
            String[] bundleIds = null;

            if (requestContext.getResolvedSiteMapItem() != null) {
                bundleIds = requestContext.getResolvedSiteMapItem().getHstSiteMapItem().getResourceBundleIds();
            } else {
                bundleIds = requestContext.getResolvedMount().getMount().getDefaultResourceBundleIds();
            }

            Locale locale = requestContext.getPreferredLocale();
            ResourceBundleRegistry resourceBundleRegistry = getResourceBundleRegistry();
            ResourceBundle bundle = null;

            for (String bundleId : bundleIds) {
                try {
                    if (resourceBundleRegistry != null) {
                        if (locale == null) {
                            bundle = (requestContext.isPreview() ? resourceBundleRegistry.getBundleForPreview(bundleId)
                                    : resourceBundleRegistry.getBundle(bundleId));
                        } else {
                            bundle = (requestContext.isPreview()
                                    ? resourceBundleRegistry.getBundleForPreview(bundleId, locale)
                                    : resourceBundleRegistry.getBundle(bundleId, locale));
                        }
                    } else {
                        if (locale == null) {
                            bundle = ResourceBundle.getBundle(bundleId, Locale.getDefault(),
                                    Thread.currentThread().getContextClassLoader());
                        } else {
                            bundle = ResourceBundle.getBundle(bundleId, locale,
                                    Thread.currentThread().getContextClassLoader());
                        }
                    }

                    if (bundle != null) {
                        bundles.add(bundle);
                    }
                } catch (MissingResourceException e) {
                    log.warn("Resource bundle not found by the basename, '{}'. {}", bundleId, e);
                }
            }
        }

        if (bundles.isEmpty()) {
            defaultResourceBundle = defaultBundleWhenNotFound != null ? defaultBundleWhenNotFound : EMPTY_RESOURCE_BUNDLE;
        } else if (bundles.size() == 1) {
            defaultResourceBundle = bundles.get(0);
        } else {
            defaultResourceBundle = new CompositeResourceBundle(bundles.toArray(new ResourceBundle[bundles.size()]));
        }

        requestContext.setAttribute(DEFAULT_RESOURCE_BUNDLE_ATTR, defaultResourceBundle);

        return defaultResourceBundle;
    }

    private static ResourceBundleRegistry getResourceBundleRegistry() {
        if (HstServices.getComponentManager() != null) {
            return HstServices.getComponentManager().getComponent(ResourceBundleRegistry.class.getName());
        }

        return null;
    }

}
