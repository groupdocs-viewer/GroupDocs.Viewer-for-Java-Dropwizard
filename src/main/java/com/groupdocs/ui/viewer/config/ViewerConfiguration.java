package com.groupdocs.ui.viewer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;

import static com.groupdocs.ui.common.config.DefaultDirectories.defaultViewerDirectory;
import static com.groupdocs.ui.common.config.DefaultDirectories.relativePathToAbsolute;

/**
 * ViewerConfiguration
 *
 * @author Aspose Pty Ltd
 */
public class ViewerConfiguration extends Configuration {

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

    @Valid
    @JsonProperty
    private boolean cache;

    @Valid
    @JsonProperty
    private boolean saveRotateState = true;

    @Valid
    @JsonProperty
    private String watermarkText;

    @Valid
    @JsonProperty
    private boolean printAllowed;

    public String getFilesDirectory() {
        return filesDirectory;
    }

    public void setFilesDirectory(String filesDirectory) {
        this.filesDirectory = StringUtils.isEmpty(filesDirectory) ? defaultViewerDirectory() : relativePathToAbsolute(filesDirectory);
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

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public boolean isSaveRotateState() {
        return saveRotateState;
    }

    public void setSaveRotateState(boolean saveRotateState) {
        this.saveRotateState = saveRotateState;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public boolean getPrintAllowed() {
        return printAllowed;
    }

    public void setPrintAllowed(boolean printAllowed) {
        this.printAllowed = printAllowed;
    }
}
