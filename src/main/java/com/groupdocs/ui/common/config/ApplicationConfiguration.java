package com.groupdocs.ui.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;

/**
 * ApplicationConfiguration
 *
 * @author Aspose Pty Ltd
 */
public class ApplicationConfiguration extends Configuration {

    @Valid
    @JsonProperty
    private String licensePath;

    public String getLicensePath() {
        return licensePath;
    }

    public void setLicensePath(String licensePath) {
        this.licensePath = licensePath;
    }

}
