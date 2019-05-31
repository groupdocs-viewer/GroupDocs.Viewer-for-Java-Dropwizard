package com.groupdocs.ui.viewer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groupdocs.ui.common.config.CommonConfiguration;
import com.groupdocs.ui.viewer.config.ViewerConfiguration;

import javax.validation.Valid;

public class ViewerConfigurationModel {

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
    private boolean pageSelector;

    @Valid
    @JsonProperty
    private boolean download;

    @Valid
    @JsonProperty
    private boolean upload;

    @Valid
    @JsonProperty
    private boolean print;

    @Valid
    @JsonProperty
    private boolean browse;

    @Valid
    @JsonProperty
    private boolean rewrite;

    @Valid
    @JsonProperty
    private boolean enableRightClick;

    public boolean isPageSelector() {
        return pageSelector;
    }

    public void setPageSelector(boolean pageSelector) {
        this.pageSelector = pageSelector;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public boolean isBrowse() {
        return browse;
    }

    public void setBrowse(boolean browse) {
        this.browse = browse;
    }

    public boolean isRewrite() {
        return rewrite;
    }

    public void setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
    }

    public boolean isEnableRightClick() {
        return enableRightClick;
    }

    public void setEnableRightClick(boolean enableRightClick) {
        this.enableRightClick = enableRightClick;
    }

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


    public static ViewerConfigurationModel createViewerConfiguration(ViewerConfiguration viewerConfiguration, CommonConfiguration commonConfiguration) {
        ViewerConfigurationModel config = new ViewerConfigurationModel();
        config.setFilesDirectory(viewerConfiguration.getFilesDirectory());
        config.setFontsDirectory(viewerConfiguration.getFontsDirectory());
        config.setDefaultDocument(viewerConfiguration.getDefaultDocument());
        config.setPreloadPageCount(viewerConfiguration.getPreloadPageCount());
        config.setZoom(viewerConfiguration.isZoom());
        config.setSearch(viewerConfiguration.isSearch());
        config.setThumbnails(viewerConfiguration.isThumbnails());
        config.setRotate(viewerConfiguration.isRotate());
        config.setHtmlMode(viewerConfiguration.isHtmlMode());
        config.setCache(viewerConfiguration.isCache());
        config.setSaveRotateState(viewerConfiguration.isSaveRotateState());
        config.setWatermarkText(viewerConfiguration.getWatermarkText());
        config.setPageSelector(commonConfiguration.isPageSelector());
        config.setDownload(commonConfiguration.isDownload());
        config.setUpload(commonConfiguration.isUpload());
        config.setPrint(commonConfiguration.isPrint());
        config.setBrowse(commonConfiguration.isBrowse());
        config.setRewrite(commonConfiguration.isRewrite());
        config.setEnableRightClick(commonConfiguration.isEnableRightClick());
        return config;
    }

    @Override
    public String toString() {
        return "ViewerConfigurationModel{" +
                "filesDirectory='" + filesDirectory + '\'' +
                ", fontsDirectory='" + fontsDirectory + '\'' +
                ", defaultDocument='" + defaultDocument + '\'' +
                ", preloadPageCount=" + preloadPageCount +
                ", zoom=" + zoom +
                ", search=" + search +
                ", thumbnails=" + thumbnails +
                ", rotate=" + rotate +
                ", htmlMode=" + htmlMode +
                ", cache=" + cache +
                ", saveRotateState=" + saveRotateState +
                ", watermarkText='" + watermarkText + '\'' +
                ", pageSelector=" + pageSelector +
                ", download=" + download +
                ", upload=" + upload +
                ", print=" + print +
                ", browse=" + browse +
                ", rewrite=" + rewrite +
                ", enableRightClick=" + enableRightClick +
                '}';
    }
}
