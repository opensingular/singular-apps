/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.app.commons.cache;

import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;

@WebListener
public class WicketSessionCacheHttpSessionListener implements javax.servlet.http.HttpSessionListener, Loggable {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (ApplicationContextProvider.isConfigured()) {
            try {
                final CacheManager manager = ApplicationContextProvider.get().getBean(CacheManager.class);
                final Cache        cache   = manager.getCache(SingularSessionCache.SINGULAR_CACHE_SESSION_CACHE);
                if (cache != null) {
                    SingularSessionKeyGenerator.extractCacheKeysFrom(se.getSession()).ifPresent(cacheKeys -> {
                        cacheKeys.forEach(cache::evict);
                    });
                }
            } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                getLogger().warn(e.getMessage(), e);
            }
        }
    }
}