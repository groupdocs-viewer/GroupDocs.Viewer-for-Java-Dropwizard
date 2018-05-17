package com.groupdocs.ui.viewer.views;

import com.groupdocs.ui.viewer.config.ViewerConfig;
import io.dropwizard.views.View;
import java.nio.charset.Charset;

/**
 * @author Aspose Pty Ltd
 */

public class Viewer extends View {
    private ViewerConfig config;

    public Viewer(ViewerConfig viewerConfig){
        super("viewer.ftl", Charset.forName("UTF-8"));
        config = viewerConfig;
    }

    public ViewerConfig getConfig() {
        return config;
    }

    public void setConfig(ViewerConfig config) {
        this.config = config;
    }

}
