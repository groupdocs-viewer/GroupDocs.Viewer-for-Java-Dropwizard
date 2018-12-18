package com.groupdocs.ui.common.entity.web;

import java.util.List;

public class LoadDocumentEntity {
    /**
     * Document Guid
     */
    private String guid;
    /**
     * list of pages
     */
    private List<PageDescriptionEntity> pages;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public List<PageDescriptionEntity> getPages() {
        return pages;
    }

    public void setPages(List<PageDescriptionEntity> pages) {
        this.pages = pages;
    }
}
