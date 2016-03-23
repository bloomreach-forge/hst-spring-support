/*
 * Copyright 2015-2016 Hippo B.V. (http://www.onehippo.com)
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

import static org.junit.Assert.assertSame;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.hippoecm.hst.container.ModifiableRequestContextProvider;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.junit.Before;
import org.junit.Test;

public class HstLocalizationUtilsTest {

    private MockHstRequestContext requestContext;
    private MapResourceBundle defaultBundle1;
    private MapResourceBundle defaultBundle2;
    private MapResourceBundle englishBundle1;
    private MapResourceBundle englishBundle2;
    private MapResourceBundle frenchBundle1;
    private MapResourceBundle frenchBundle2;

    @Before
    public void setUp() throws Exception {
        requestContext = new MockHstRequestContext();
        ModifiableRequestContextProvider.set(requestContext);

        defaultBundle1 = new MapResourceBundle();
        defaultBundle2 = new MapResourceBundle();
        englishBundle1 = new MapResourceBundle();
        englishBundle2 = new MapResourceBundle();
        frenchBundle1 = new MapResourceBundle();
        frenchBundle2 = new MapResourceBundle();
    }

    @Test
    public void testGetCurrentDefaultResourceBundle_withNullOrDefaultLocale() throws Exception {
        requestContext.setPreferredLocale(null);
        ResourceBundle bundleByNullLocale1 = HstLocalizationUtils.getCurrentDefaultResourceBundle(defaultBundle1);
        assertSame(defaultBundle1, bundleByNullLocale1);
        ResourceBundle bundleByNullLocale2 = HstLocalizationUtils.getCurrentDefaultResourceBundle(defaultBundle2);
        assertSame(defaultBundle1, bundleByNullLocale2);

        requestContext.setPreferredLocale(Locale.getDefault());
        ResourceBundle bundleByDefaultLocale1 = HstLocalizationUtils.getCurrentDefaultResourceBundle();
        assertSame(defaultBundle1, bundleByDefaultLocale1);
        ResourceBundle bundleByDefaultLocale2 = HstLocalizationUtils.getCurrentDefaultResourceBundle();
        assertSame(defaultBundle1, bundleByDefaultLocale2);
    }

    @Test
    public void testGetCurrentDefaultResourceBundle_withEnglishLocale() throws Exception {
        requestContext.setPreferredLocale(Locale.ENGLISH);
        ResourceBundle bundleByEnglish1 = HstLocalizationUtils.getCurrentDefaultResourceBundle(englishBundle1);
        assertSame(englishBundle1, bundleByEnglish1);
        ResourceBundle bundleByEnglish2 = HstLocalizationUtils.getCurrentDefaultResourceBundle(englishBundle2);
        assertSame(englishBundle1, bundleByEnglish2);
    }

    @Test
    public void testGetCurrentDefaultResourceBundle_withFrenchLocale() throws Exception {
        requestContext.setPreferredLocale(Locale.FRENCH);
        ResourceBundle bundleByFrench1 = HstLocalizationUtils.getCurrentDefaultResourceBundle(frenchBundle1);
        assertSame(frenchBundle1, bundleByFrench1);
        ResourceBundle bundleByFrench2 = HstLocalizationUtils.getCurrentDefaultResourceBundle(frenchBundle2);
        assertSame(frenchBundle1, bundleByFrench2);
    }

    @Test
    public void testGetCurrentDefaultResourceBundle_withDifferentLocales() throws Exception {
        requestContext.setPreferredLocale(Locale.ENGLISH);
        ResourceBundle bundleByEnglish1 = HstLocalizationUtils.getCurrentDefaultResourceBundle(englishBundle1);
        assertSame(englishBundle1, bundleByEnglish1);
        ResourceBundle bundleByEnglish2 = HstLocalizationUtils.getCurrentDefaultResourceBundle(englishBundle2);
        assertSame(englishBundle1, bundleByEnglish2);

        requestContext.removeAttribute(HstLocalizationUtils.DEFAULT_RESOURCE_BUNDLE_ATTR);

        requestContext.setPreferredLocale(Locale.FRENCH);
        ResourceBundle bundleByFrench1 = HstLocalizationUtils.getCurrentDefaultResourceBundle(frenchBundle1);
        assertSame(frenchBundle1, bundleByFrench1);
        ResourceBundle bundleByFrench2 = HstLocalizationUtils.getCurrentDefaultResourceBundle(frenchBundle2);
        assertSame(frenchBundle1, bundleByFrench2);
    }

    private static class MapResourceBundle extends ResourceBundle {

        private Map<String, Object> map;

        public MapResourceBundle() {
            this.map = new HashMap<>();
        }

        /**
         * Gets a resource for a given key. This is called by <code>getObject</code>.
         *
         * @param key the key of the resource
         * @return the resource for the key, or null if it doesn't exist
         */
        public final Object handleGetObject(String key) {
            return map.get(key);
        }

        public Enumeration<String> getKeys() {
            return new IteratorEnumeration(map.keySet().iterator());
        }

        public void put(final String key, final String value) {
            map.put(key, value);
        }
    }
}
