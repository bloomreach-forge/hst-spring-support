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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.resourcebundle.ResourceBundleRegistry;
import org.hippoecm.hst.site.HstServices;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * {@link org.springframework.context.MessageSource} implementation that
 * accesses HST-2 Dynamic Resource Bundles first and falls back to the super class,
 * {@link ResourceBundleMessageSource} if not found from HST-2 Dynamic Resource Bundles.
 */
public class HstRepositoryResourceBundleMessageSource extends ResourceBundleMessageSource {

    public HstRepositoryResourceBundleMessageSource() {
        super();
    }

    /**
     * Obtain the resource bundle for the given basename and Locale
     * from the HST Resource Bundles first. If not found, then it
     * falls back to the super class method, {@link ResourceBundleMessageSource#doGetBundle(String, Locale)}.
     */
    @Override
    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        ResourceBundle bundle = null;

        ResourceBundleRegistry resourceBundleRegistry =
                HstServices.getComponentManager().getComponent(ResourceBundleRegistry.class.getName());

        if (resourceBundleRegistry != null) {
            HstRequestContext requestContext = RequestContextProvider.get();
            boolean preview = (requestContext != null && requestContext.isPreview());

            if (locale == null) {
                bundle = (preview ? resourceBundleRegistry.getBundleForPreview(basename) : resourceBundleRegistry.getBundle(basename));
            } else {
                bundle = (preview ? resourceBundleRegistry.getBundleForPreview(basename, locale) : resourceBundleRegistry.getBundle(basename, locale));
            }
        }

        if (bundle == null) {
            bundle = super.doGetBundle(basename, locale);
        }

        return bundle;
    }

}
