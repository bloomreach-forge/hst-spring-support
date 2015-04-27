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
package org.onehippo.forge.hst.spring.support.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hippoecm.hst.configuration.sitemap.HstSiteMap;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.util.HstRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class handles a {@link HstPageNotFoundException} to forward request to the page not found sitemap item
 * configured in HST configuration.
 */
public class HstBridgeHandlerExceptionsResolver implements HandlerExceptionResolver, Ordered {

    private static Logger log = LoggerFactory.getLogger(HstBridgeHandlerExceptionsResolver.class);

    /**
     * Sitemap item reference id of the 'page not found' sitemap item.
     */
    private String pageNotFoundRefId;

    /**
     * The order value of this object.
     */
    private int order = Integer.MIN_VALUE;

    /**
     * Returns the reference id of the 'page not found' sitemap item.
     * @return the reference id of the 'page not found' sitemap item
     */
    public String getPageNotFoundRefId() {
        return pageNotFoundRefId;
    }

    /**
     * Sets the reference id of the 'page not found' sitemap item.
     * @param pageNotFoundRefId the reference id of the 'page not found' sitemap item
     */
    public void setPageNotFoundRefId(String pageNotFoundRefId) {
        this.pageNotFoundRefId = pageNotFoundRefId;
    }

    /**
     * If the {@code exception} is instance of {@link HstPageNotFoundException},
     * then it tries to resolve the sitemap item path of the configured 'page not found' sitemap item
     * and forward request to the 'page not found' sitemap item path info.
     * <p></p>
     * If the 'page not found' sitemap item is not found, then it simply returns an empty {@link ModelAndView}.
     * {@inheritDoc}
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        if (exception instanceof HstPageNotFoundException) {
            HstResponse hstResponse = HstRequestUtils.getHstResponse(request, response);

            if (hstResponse != null) {
                String pathInfo = resolvePageNotFoundPathInfo();

                // If the path info of the page nout found sitemap item exists,
                // thenthe response forwards the request to that path info.
                // Otherwise nothing happens.
                if (pathInfo != null) {
                    try {
                        hstResponse.forward(pathInfo);
                    } catch (IOException e) {
                        log.error("Error when forwarding to '{}' sitemap item.", pageNotFoundRefId, e);
                    }
                }
            } else {
                log.warn("HstPageNotFoundException occurred with a non-HstResponse.");
            }
        }

        return new ModelAndView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order value of this object.
     * @param order order value of this object
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Finds path info of the 'page not found' sitemap item.
     */
    protected String resolvePageNotFoundPathInfo() {
        String pathInfo = null;

        if (getPageNotFoundRefId() != null) {
            final HstRequestContext requestContext = RequestContextProvider.get();
            final ResolvedSiteMapItem resolvedSiteMapItem = requestContext.getResolvedSiteMapItem();

            if (resolvedSiteMapItem != null) {
                final HstSiteMap siteMap = resolvedSiteMapItem.getHstSiteMapItem().getHstSiteMap();
                final HstSiteMapItem pageNotFoundSiteMapItem = siteMap.getSiteMapItemByRefId(getPageNotFoundRefId());

                if (pageNotFoundSiteMapItem != null) {
                    String siteMapItemPath = pageNotFoundSiteMapItem.getValue();

                    if (siteMapItemPath != null) {
                        pathInfo = new StringBuilder(siteMapItemPath.length() + 1).append('/').append(siteMapItemPath).toString();
                    }
                }
            }
        }

        return pathInfo;
    }
}
    