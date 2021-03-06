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
import java.util.ResourceBundle;

import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.easymock.EasyMock;
import org.hippoecm.hst.container.ModifiableRequestContextProvider;
import org.hippoecm.hst.core.container.ComponentManager;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.hippoecm.hst.resourcebundle.ResourceBundleRegistry;
import org.hippoecm.hst.resourcebundle.SimpleListResourceBundle;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.util.DefaultKeyValue;
import org.hippoecm.hst.util.KeyValue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;

/**
 * HstRepositoryResourceBundleMessageSourceTest
 */
public class HstRepositoryResourceBundleMessageSourceTest {

    private static final String FILE_BUNDLE_ID = HstRepositoryResourceBundleMessageSourceTest.class.getPackage().getName() + ".MessageResources";

    private static final String REPOSITORY_BUNDLE_ID = HstRepositoryResourceBundleMessageSourceTest.class.getPackage().getName();

    private ResourceBundleRegistry registry;

    private ResourceBundle localizationContextBundle;
    private ResourceBundle previewLocalizationContextBundle;

    private Map<String, String> liveDefaultBundleContent;
    private Map<String, String> previewDefaultBundleContent;
    private Map<String, String> liveBundleContent = new HashMap<String, String>();
    private Map<String, String> previewBundleContent = new HashMap<String, String>();

    private ResourceBundle liveBundle;
    private ResourceBundle previewBundle;

    private HstRepositoryResourceBundleMessageSource messageSource;
    private CachingRepositoryResourceBundleMessageFormatProvider messageFormatProvider;

    private boolean previewMode;

    private MockHttpServletRequest request;
    private MockHstRequestContext requestContext;

    @Before
    public void before() throws Exception {
        liveDefaultBundleContent = new HashMap<String, String>();
        liveDefaultBundleContent.put("greeting.hello", "Hello, World!");
        liveDefaultBundleContent.put("greeting.hello.name", "Hello, {0}!");

        previewDefaultBundleContent = new HashMap<String, String>();
        previewDefaultBundleContent.put("greeting.hello", "[Preview] Hello, World!");
        previewDefaultBundleContent.put("greeting.hello.name", "[Preview] Hello, {0}!");

        liveBundleContent = new HashMap<String, String>(liveDefaultBundleContent);
        liveBundleContent.put("greeting.howdy.name", "Howdy, {0}!");
        liveBundle = new SimpleListResourceBundle(liveBundleContent);

        previewBundleContent = new HashMap<String, String>();
        previewBundleContent.put("greeting.hello", "[Preview] Hello, World!");
        previewBundleContent.put("greeting.hello.name", "[Preview] Hello, {0}!");
        previewBundleContent.put("greeting.howdy.name", "[Preview] Howdy, {0}!");
        previewBundle = new SimpleListResourceBundle(previewBundleContent);

        resetMockResourceBundleRegistry(liveBundle, previewBundle);

        final ComponentManager componentManager = EasyMock.createNiceMock(ComponentManager.class);
        EasyMock.expect(componentManager.getComponent(ResourceBundleRegistry.class.getName())).andReturn(registry).anyTimes();
        EasyMock.replay(componentManager);
        HstServices.setComponentManager(componentManager);

        messageSource = new HstRepositoryResourceBundleMessageSource();
        messageFormatProvider = (CachingRepositoryResourceBundleMessageFormatProvider) messageSource.getResourceBundleMessageFormatProvider();

        assertEquals(0, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(0, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(0, messageFormatProvider.getCachedBundleMessageFormats().size());
        assertEquals(0, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(0, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(0, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());

        messageSource.setBasenames(new String [] { REPOSITORY_BUNDLE_ID, FILE_BUNDLE_ID });

        final Map<String, String> defaultBundleContent = new HashMap<String, String>(liveBundleContent);
        defaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you cool?");
        localizationContextBundle = new SimpleListResourceBundle(defaultBundleContent);

        final Map<String, String> previewDefaultBundleContent = new HashMap<String, String>(previewBundleContent);
        previewDefaultBundleContent.put("greeting.hello.name", "[Preview] Hello, {0}! Are you cool?");
        previewLocalizationContextBundle = new SimpleListResourceBundle(previewDefaultBundleContent);

        request = new MockHttpServletRequest();
        requestContext = new MockHstRequestContext() {
            @Override
            public boolean isPreview() {
                return previewMode;
            }
        };
        requestContext.setServletRequest(request);
        ModifiableRequestContextProvider.set(requestContext);
    }

    @Test
    public void testWithoutSettingNoLocalizationContextBundle() throws Exception {
        assertEquals("Hello, World!", messageSource.getMessage("file.greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John!", messageSource.getMessage("file.greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John!", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        KeyValue<String, Locale> bundleIdLocalePair = new DefaultKeyValue<String, Locale>(REPOSITORY_BUNDLE_ID, Locale.ENGLISH);

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        Map<String, Map<Locale, MessageFormat>> messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(2, messageFormats.size());

        assertEquals(0, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(0, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(0, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());

        previewMode = true;

        assertEquals("[Preview] Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("[Preview] Hello, John!", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("[Preview] Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(2, messageFormats.size());

        assertEquals(1, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(previewBundle, messageFormatProvider.getBasenameLocaleBundlesForPreview().get(bundleIdLocalePair));
        assertEquals(1, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocalesForPreview().get(previewBundle));
        assertEquals(1, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormatsForPreview().get(previewBundle);
        assertEquals(2, messageFormats.size());

        previewMode = false;

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John!", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(2, messageFormats.size());
    }

    @Test
    public void testLocalizationContextBundleTurnedOn() throws Exception {
        // This is done in LocalizationValve of HST-2 Container.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        // Let's change the default resource bundle localization context on the fly.
        liveDefaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you really cool?");
        localizationContextBundle = new SimpleListResourceBundle(liveDefaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        KeyValue<String, Locale> defaultBundleIdLocalePair = new DefaultKeyValue<String, Locale>("", Locale.ENGLISH);
        KeyValue<String, Locale> bundleIdLocalePair = new DefaultKeyValue<String, Locale>(REPOSITORY_BUNDLE_ID, Locale.ENGLISH);

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(localizationContextBundle, messageFormatProvider.getBasenameLocaleBundles().get(defaultBundleIdLocalePair));
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(localizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        Map<String, Map<Locale, MessageFormat>> messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(localizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(1, messageFormats.size());

        assertEquals(0, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(0, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(0, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());

        previewMode = true;

        previewDefaultBundleContent.put("greeting.hello.name", "[Preview] Hello, {0}! Are you really cool?");
        previewLocalizationContextBundle = new SimpleListResourceBundle(previewDefaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(previewLocalizationContextBundle));

        assertEquals("[Preview] Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("[Preview] Hello, John! Are you really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("[Preview] Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(localizationContextBundle, messageFormatProvider.getBasenameLocaleBundles().get(defaultBundleIdLocalePair));
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(localizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(localizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(1, messageFormats.size());

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(previewLocalizationContextBundle, messageFormatProvider.getBasenameLocaleBundlesForPreview().get(defaultBundleIdLocalePair));
        assertEquals(previewBundle, messageFormatProvider.getBasenameLocaleBundlesForPreview().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocalesForPreview().get(previewLocalizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocalesForPreview().get(previewBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormatsForPreview().get(previewLocalizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormatsForPreview().get(previewBundle);
        assertEquals(1, messageFormats.size());

        previewMode = false;

        // Let's change the default resource bundle localization context on the fly.
        liveDefaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you really cool?");
        localizationContextBundle = new SimpleListResourceBundle(liveDefaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(localizationContextBundle, messageFormatProvider.getBasenameLocaleBundles().get(defaultBundleIdLocalePair));
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(localizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(localizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(1, messageFormats.size());

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(previewLocalizationContextBundle, messageFormatProvider.getBasenameLocaleBundlesForPreview().get(defaultBundleIdLocalePair));
        assertEquals(previewBundle, messageFormatProvider.getBasenameLocaleBundlesForPreview().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocalesForPreview().get(previewLocalizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocalesForPreview().get(previewBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormatsForPreview().get(previewLocalizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormatsForPreview().get(previewBundle);
        assertEquals(1, messageFormats.size());
    }

    @Test
    public void testLocalizationContextBundleTurnedOff() throws Exception {
        // This is done in LocalizationValve of HST-2 Container.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        // Let's change the default resource bundle localization context on the fly.
        final Map<String, String> defaultBundleContent = new HashMap<String, String>(liveBundleContent);
        defaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you really cool?");
        localizationContextBundle = new SimpleListResourceBundle(defaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        // Let's try to turn off localizationContextResourceBundleEnabled option.
        messageSource.setLocalizationContextResourceBundleEnabled(false);
        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John!", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));

        KeyValue<String, Locale> bundleIdLocalePair = new DefaultKeyValue<String, Locale>(REPOSITORY_BUNDLE_ID, Locale.ENGLISH);

        // While localizationContextBundle is still in cache, the message formats are cached for liveBundle..
        assertEquals(1, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(1, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(1, messageFormatProvider.getCachedBundleMessageFormats().size());
        Map<String, Map<Locale, MessageFormat>> messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        // The argumented resource key from the 'liveBundle' is not supposed to be used due to the localizationContextBundle.
        assertEquals(1, messageFormats.size());

        assertEquals(0, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(0, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(0, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());
    }

    @Test
    public void testDynamicResourceBundleRefreshed() throws Exception {
        // This is done in LocalizationValve of HST-2 Container.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        // Let's change the default resource bundle localization context on the fly.
        liveDefaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you really cool?");
        localizationContextBundle = new SimpleListResourceBundle(liveDefaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Howdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        KeyValue<String, Locale> defaultBundleIdLocalePair = new DefaultKeyValue<String, Locale>("", Locale.ENGLISH);
        KeyValue<String, Locale> bundleIdLocalePair = new DefaultKeyValue<String, Locale>(REPOSITORY_BUNDLE_ID, Locale.ENGLISH);

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(localizationContextBundle, messageFormatProvider.getBasenameLocaleBundles().get(defaultBundleIdLocalePair));
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(localizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        Map<String, Map<Locale, MessageFormat>> messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(localizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(1, messageFormats.size());

        // Now refresh the localizationContextBundle and liveBundle
        // to see how it removes the outdated bundle and message formats from the internal cache.
        // Let's change the default resource bundle localization context on the fly.

        liveDefaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you sure you are really cool?");
        localizationContextBundle = new SimpleListResourceBundle(liveDefaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        liveBundleContent.put("greeting.hello", "Hello, Wooooooorld!");
        liveBundleContent.put("greeting.hello.name", "Hello, {0}!");
        liveBundleContent.put("greeting.howdy.name", "Hooooowdy, {0}!");

        liveBundle = new SimpleListResourceBundle(liveBundleContent);
        previewBundle = new SimpleListResourceBundle(liveBundleContent);

        resetMockResourceBundleRegistry(liveBundle, previewBundle);

        assertEquals("Hello, World!", messageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you sure you are really cool?", messageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Hooooowdy, John!", messageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));

        assertEquals(2, messageFormatProvider.getBasenameLocaleBundles().size());
        assertEquals(localizationContextBundle, messageFormatProvider.getBasenameLocaleBundles().get(defaultBundleIdLocalePair));
        assertEquals(liveBundle, messageFormatProvider.getBasenameLocaleBundles().get(bundleIdLocalePair));
        assertEquals(2, messageFormatProvider.getBundleBasenameLocales().size());
        assertEquals(defaultBundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(localizationContextBundle));
        assertEquals(bundleIdLocalePair, messageFormatProvider.getBundleBasenameLocales().get(liveBundle));
        assertEquals(2, messageFormatProvider.getCachedBundleMessageFormats().size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(localizationContextBundle);
        assertEquals(1, messageFormats.size());
        messageFormats = messageFormatProvider.getCachedBundleMessageFormats().get(liveBundle);
        assertEquals(1, messageFormats.size());

        assertEquals(0, messageFormatProvider.getBasenameLocaleBundlesForPreview().size());
        assertEquals(0, messageFormatProvider.getBundleBasenameLocalesForPreview().size());
        assertEquals(0, messageFormatProvider.getCachedBundleMessageFormatsForPreview().size());
    }

    @Test
    public void testDelegatingMessageSource() throws Exception {
        DelegatingMessageSource delegatingMessageSource = new DelegatingMessageSource();
        delegatingMessageSource.setParentMessageSource(messageSource);

        // This is done in LocalizationValve of HST-2 Container.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        // Let's change the default resource bundle localization context on the fly.
        Map<String, String> defaultBundleContent = new HashMap<String, String>(liveBundleContent);
        defaultBundleContent.put("greeting.hello.name", "Hello, {0}! Are you really cool?");
        localizationContextBundle = new SimpleListResourceBundle(defaultBundleContent);
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(localizationContextBundle));

        assertEquals("Hello, World!", delegatingMessageSource.getMessage("file.greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, World!", delegatingMessageSource.getMessage("file.greeting.hello", null, null, Locale.ENGLISH));
        assertEquals("Hello, John!", delegatingMessageSource.getMessage("file.greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Hello, John!", delegatingMessageSource.getMessage("file.greeting.hello.name", new Object [] { "John" }, null, Locale.ENGLISH));

        assertEquals("Hello, World!", delegatingMessageSource.getMessage("greeting.hello", null, Locale.ENGLISH));
        assertEquals("Hello, World!", delegatingMessageSource.getMessage("greeting.hello", null, null, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", delegatingMessageSource.getMessage("greeting.hello.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Hello, John! Are you really cool?", delegatingMessageSource.getMessage("greeting.hello.name", new Object [] { "John" }, null, Locale.ENGLISH));
        assertEquals("Howdy, John!", delegatingMessageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, Locale.ENGLISH));
        assertEquals("Howdy, John!", delegatingMessageSource.getMessage("greeting.howdy.name", new Object [] { "John" }, null, Locale.ENGLISH));
    }

    private void resetMockResourceBundleRegistry(final ResourceBundle liveBundle, ResourceBundle previewBundle) {
        registry = EasyMock.createNiceMock(ResourceBundleRegistry.class);
        EasyMock.expect(registry.getBundle(REPOSITORY_BUNDLE_ID, Locale.ENGLISH)).andReturn(liveBundle).anyTimes();
        EasyMock.expect(registry.getBundleForPreview(REPOSITORY_BUNDLE_ID, Locale.ENGLISH)).andReturn(previewBundle)
                .anyTimes();
        EasyMock.expect(registry.getBundle(FILE_BUNDLE_ID, Locale.ENGLISH))
                .andReturn(ResourceBundle.getBundle(FILE_BUNDLE_ID, Locale.ENGLISH)).anyTimes();
        EasyMock.expect(registry.getBundleForPreview(FILE_BUNDLE_ID, Locale.ENGLISH))
                .andReturn(ResourceBundle.getBundle(FILE_BUNDLE_ID, Locale.ENGLISH)).anyTimes();
        EasyMock.replay(registry);

        final ComponentManager componentManager = EasyMock.createNiceMock(ComponentManager.class);
        EasyMock.expect(componentManager.getComponent(ResourceBundleRegistry.class.getName())).andReturn(registry)
                .anyTimes();
        EasyMock.replay(componentManager);
        HstServices.setComponentManager(componentManager);
    }
}
