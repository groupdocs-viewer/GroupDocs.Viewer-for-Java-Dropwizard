package com.groupdocs.ui.common.config;

import io.dropwizard.Configuration;

/**
 * ServerConfiguration
 *
 * @author Aspose Pty Ltd
 */
public class ServerConfiguration extends Configuration{
    private int httpPort;
    private String hostAddress;

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

}
