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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.easymock.EasyMock;
import org.hippoecm.hst.container.ModifiableRequestContextProvider;
import org.hippoecm.hst.core.container.ComponentManager;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.hippoecm.hst.resourcebundle.ResourceBundleRegistry;
import org.hippoecm.hst.resourcebundle.SimpleListResourceBundle;
import org.hippoecm.hst.resourcebundle.internal.DefaultMutableResourceBundleFamily;
import org.hippoecm.hst.resourcebundle.internal.DefaultMutableResourceBundleRegistry;
import org.hippoecm.hst.site.HstServices;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * HstRepositoryResourceBundleMessageSourceTest
 */
public class HstRepositoryResourceBundleMessageSourceTest {

    private static final String BUNDLE_ID = HstRepositoryResourceBundleMessageSourceTest.class.getPackage().getName();

    private DefaultMutableResourceBundleRegistry registry;
    private Map<String, String> bundleContent = new HashMap<String, String>();
    private ResourceBundle defaultResourceBundle;

    private HstRepositoryResourceBundleMessageSource messageSource;

    @Before
    public void before() throws Exception {
        bundleContent.put("greeting.hello", "Hello, World!");
        bundleContent.put("greeting.hello.name", "Hello, {0}!");

        final SimpleListResourceBundle bundle = new SimpleListResourceBundle(bundleContent);
        final DefaultMutableResourceBundleFamily bundleFamily = new DefaultMutableResourceBundleFamily(BUNDLE_ID);
        bundleFamily.setDefaultBundle(bundle);
        bundleFamily.setDefaultBundleForPreview(bundle);
        registry = new DefaultMutableResourceBundleRegistry();
        registry.registerBundleFamily(BUNDLE_ID, bundleFamily);

        final ComponentManager componentManager = EasyMock.createNiceMock(ComponentManager.class);
        EasyMock.expect(componentManager.getComponent(ResourceBundleRegistry.class.getName())).andReturn(registry).anyTimes();
        EasyMock.replay(componentManager);
        HstServices.setComponentManager(componentManager);

        messageSource = new HstRepositoryResourceBundleMessageSource();
        messageSource.setBasename(BUNDLE_ID);

        final Map<String, String> defaultBundleContent = new HashMap<String, String>(bundleContent);
        defaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you cool?");
        defaultResourceBundle = new SimpleListResourceBundle(defaultBundleContent);
    }

    @Test
    public void testStaticUsage() throws Exception {
        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John!", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
    }

    @Test
    public void testDynamicChange() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHstRequestContext requestContext = new MockHstRequestContext() {
            @Override
            public boolean isPreview() {
                return false;
            }
        };
        requestContext.setServletRequest(request);
        ModifiableRequestContextProvider.set(requestContext);

        // This is done in LocalizationValve of HST-2 Container.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(defaultResourceBundle));

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));

        // Let's change the default resource bundle localization context on the fly.
        final Map<String, String> defaultBundleContent = new HashMap<String, String>(bundleContent);
        defaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you really cool?");
        defaultResourceBundle = new SimpleListResourceBundle(defaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(defaultResourceBundle));

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));

        // Let's try to turn off localizationContextResourceBundleEnabled option.
        messageSource.setLocalizationContextResourceBundleEnabled(false);
        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John!", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));

        // Let's try to turn back on localizationContextResourceBundleEnabled option.
        messageSource.setLocalizationContextResourceBundleEnabled(true);
        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
    }
}
