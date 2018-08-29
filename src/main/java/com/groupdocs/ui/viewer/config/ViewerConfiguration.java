package com.groupdocs.ui.viewer.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.groupdocs.ui.common.config.CommonConfiguration;

import javax.validation.Valid;

/**
 * ViewerConfiguration
 *
 * @author Aspose Pty Ltd
 */
public class ViewerConfiguration extends CommonConfiguration {

    @Valid
    @JsonProperty
    private String filesDirectory;

    @Valid
    @JsonProperty
    private String fontsDirectory;

    @Valid
    @JsonProperty
    private String defaultDocument;

    @Valid
    @JsonProperty
    private int preloadPageCount;

    @Valid
    @JsonProperty
    private boolean zoom;

    @Valid
    @JsonProperty
    private boolean search;

    @Valid
    @JsonProperty
    private boolean thumbnails;

    @Valid
    @JsonProperty
    private boolean rotate;

    @Valid
    @JsonProperty
    private boolean htmlMode;

    public String getFilesDirectory() {
        return filesDirectory;
    }

    public void setFilesDirectory(String filesDirectory) {
        this.filesDirectory = filesDirectory;
    }

    public String getFontsDirectory() {
        return fontsDirectory;
    }

    public void setFontsDirectory(String fontsDirectory) {
        this.fontsDirectory = fontsDirectory;
    }

    public String getDefaultDocument() {
        return defaultDocument;
    }

    public void setDefaultDocument(String defaultDocument) {
        this.defaultDocument = defaultDocument;
    }

    public int getPreloadPageCount() {
        return preloadPageCount;
    }

    public void setPreloadPageCount(int preloadPageCount) {
        this.preloadPageCount = preloadPageCount;
    }

    public boolean isZoom() {
        return zoom;
    }

    public void setZoom(boolean zoom) {
        this.zoom = zoom;
    }

    public boolean isSearch() {
        return search;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }

    public boolean isThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(boolean thumbnails) {
        this.thumbnails = thumbnails;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public boolean isHtmlMode() {
        return htmlMode;
    }

    public void setHtmlMode(boolean htmlMode) {
        this.htmlMode = htmlMode;
    }
}
