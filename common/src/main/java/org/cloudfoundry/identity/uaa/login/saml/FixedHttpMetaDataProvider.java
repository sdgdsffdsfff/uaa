/*******************************************************************************
 *     Cloud Foundry 
 *     Copyright (c) [2009-2014] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/

package org.cloudfoundry.identity.uaa.login.saml;

import java.net.URISyntaxException;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;

/**
 * This class works around the problem described in {@link https
 * ://issues.apache.org/jira/browse/HTTPCLIENT-646} when a socket factory is set
 * on the OpenSAML
 * {@link HTTPMetadataProvider#setSocketFactory(ProtocolSocketFactory)} all
 * subsequent GET Methods should be executed using a relative URL, otherwise the
 * HttpClient
 * resets the underlying socket factory.
 * 
 * @author Filip Hanik
 * 
 */
public class FixedHttpMetaDataProvider extends HTTPMetadataProvider implements ComparableProvider {

    /**
     * Track if we have a custom socket factory
     */
    private boolean socketFactorySet = false;
    private final String zoneId;
    private final String alias;


    public FixedHttpMetaDataProvider(String zoneId, String alias, Timer backgroundTaskTimer, HttpClient client,
                    String metadataURL) throws MetadataProviderException {
        super(backgroundTaskTimer, client, metadataURL);
        this.alias = alias;
        this.zoneId = zoneId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSocketFactory(ProtocolSocketFactory newSocketFactory) {
        // TODO Auto-generated method stub
        super.setSocketFactory(newSocketFactory);
        if (newSocketFactory != null) {
            socketFactorySet = true;
        } else {
            socketFactorySet = false;
        }
    }

    /**
     * If a custom socket factory has been set, only
     * return a relative URL so that the custom factory is retained.
     * This works around
     * https://issues.apache.org/jira/browse/HTTPCLIENT-646 {@inheritDoc}
     */
    @Override
    public String getMetadataURI() {
        if (isSocketFactorySet()) {
            java.net.URI uri;
            try {
                uri = new java.net.URI(super.getMetadataURI());
                String result = uri.getPath();
                if (uri.getQuery() != null && uri.getQuery().trim().length() > 0) {
                    result = result + "?" + uri.getQuery();
                }
                return result;
            } catch (URISyntaxException e) {
                // this can never happen, satisfy compiler
                throw new IllegalArgumentException(e);
            }
        } else {
            return super.getMetadataURI();
        }
    }

    public boolean isSocketFactorySet() {
        return socketFactorySet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ComparableProvider)) return false;

        ComparableProvider that = (ComparableProvider) o;

        if (!alias.equals(that.getAlias())) return false;
        if (!zoneId.equals(that.getZoneId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zoneId.hashCode();
        result = 31 * result + alias.hashCode();
        return result;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String getZoneId() {
        return zoneId;
    }
}
