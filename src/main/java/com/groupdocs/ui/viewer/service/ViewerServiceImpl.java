package com.groupdocs.ui.viewer.service;

import com.groupdocs.ui.common.config.DefaultDirectories;
import com.groupdocs.ui.common.config.GlobalConfiguration;
import com.groupdocs.ui.common.entity.web.FileDescriptionEntity;
import com.groupdocs.ui.common.entity.web.LoadDocumentEntity;
import com.groupdocs.ui.common.entity.web.PageDescriptionEntity;
import com.groupdocs.ui.common.entity.web.request.FileTreeRequest;
import com.groupdocs.ui.common.entity.web.request.LoadDocumentPageRequest;
import com.groupdocs.ui.common.entity.web.request.LoadDocumentRequest;
import com.groupdocs.ui.common.exception.TotalGroupDocsException;
import com.groupdocs.ui.viewer.config.ViewerConfiguration;
import com.groupdocs.ui.viewer.entity.web.RotatedPageEntity;
import com.groupdocs.ui.viewer.model.web.RotateDocumentPagesRequest;
import com.groupdocs.viewer.config.ViewerConfig;
import com.groupdocs.viewer.converter.options.HtmlOptions;
import com.groupdocs.viewer.converter.options.ImageOptions;
import com.groupdocs.viewer.domain.FileDescription;
import com.groupdocs.viewer.domain.Page;
import com.groupdocs.viewer.domain.PageData;
import com.groupdocs.viewer.domain.containers.DocumentInfoContainer;
import com.groupdocs.viewer.domain.containers.FileListContainer;
import com.groupdocs.viewer.domain.containers.PdfDocumentInfoContainer;
import com.groupdocs.viewer.domain.html.PageHtml;
import com.groupdocs.viewer.domain.image.PageImage;
import com.groupdocs.viewer.domain.options.DocumentInfoOptions;
import com.groupdocs.viewer.domain.options.FileListOptions;
import com.groupdocs.viewer.domain.options.RotatePageOptions;
import com.groupdocs.viewer.exception.GroupDocsViewerException;
import com.groupdocs.viewer.exception.InvalidPasswordException;
import com.groupdocs.viewer.handler.ViewerHandler;
import com.groupdocs.viewer.handler.ViewerHtmlHandler;
import com.groupdocs.viewer.handler.ViewerImageHandler;
import com.groupdocs.viewer.licensing.License;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static com.groupdocs.ui.common.exception.PasswordExceptions.INCORRECT_PASSWORD;
import static com.groupdocs.ui.common.exception.PasswordExceptions.PASSWORD_REQUIRED;
import static com.groupdocs.ui.viewer.service.ViewerOptionsFactory.*;

public class ViewerServiceImpl implements ViewerService {
    private static final Logger logger = LoggerFactory.getLogger(ViewerServiceImpl.class);

    public static final String PDF = "pdf";

    private final ViewerHandler viewerHandler;
    private ViewerConfiguration viewerConfiguration;

    public ViewerServiceImpl(GlobalConfiguration globalConfiguration) {
        viewerConfiguration = globalConfiguration.getViewer();

        try {
            // set GroupDocs license
            License license = new License();
            license.setLicense(globalConfiguration.getApplication().getLicensePath());
        } catch (Throwable ex) {
            logger.error("Can not verify Viewer license!");
        }
        // create viewer application configuration
        ViewerConfig config = getViewerConfig();

        if (viewerConfiguration.isHtmlMode()) {
            // initialize total instance for the HTML mode
            viewerHandler = new ViewerHtmlHandler(config);
        } else {
            // initialize total instance for the Image mode
            viewerHandler = new ViewerImageHandler(config);
        }
    }

    private ViewerConfig getViewerConfig() {
        ViewerConfig config = new ViewerConfig();
        String filesDirectory = viewerConfiguration.getFilesDirectory();
        if (StringUtils.isNotEmpty(filesDirectory) && !filesDirectory.endsWith(File.separator)) {
            filesDirectory = filesDirectory + File.separator;
        }
        config.setStoragePath(filesDirectory);
        config.setEnableCaching(viewerConfiguration.isCache());
        if (!StringUtils.isEmpty(viewerConfiguration.getFontsDirectory())) {
            config.getFontDirectories().add(viewerConfiguration.getFontsDirectory());
        }
        return config;
    }

    @Override
    public List<FileDescriptionEntity> loadFileTree(FileTreeRequest fileTreeRequest) {
        String relDirPath = fileTreeRequest.getPath();
        // get file list from storage path
        FileListOptions fileListOptions = new FileListOptions(relDirPath);
        FileListContainer fileListContainer;
        try {
            fileListContainer = viewerHandler.getFileList(fileListOptions);
        } catch (Exception ex) {
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
        List<FileDescriptionEntity> fileList = new ArrayList<>();
        // parse files/folders list
        for (FileDescription fd : fileListContainer.getFiles()) {
            FileDescriptionEntity fileDescription = getFileDescriptionEntity(fd);
            if (fileDescription != null) {
                // add object to array list
                fileList.add(fileDescription);
            }
        }
        return fileList;

    }

    protected FileDescriptionEntity getFileDescriptionEntity(FileDescription fd) {
        // get temp directory name
        String tempDirectoryName = new ViewerConfig().getCacheFolderName();
        // check if current file/folder is temp directory or is hidden
        if (!tempDirectoryName.equals(fd.getName()) && !new File(fd.getGuid()).isHidden()) {
            FileDescriptionEntity fileDescription = new FileDescriptionEntity();
            fileDescription.setGuid(fd.getGuid());
            // set file/folder name
            fileDescription.setName(fd.getName());
            // set file type
            fileDescription.setDocType(fd.getFileFormat());
            // set is directory true/false
            fileDescription.setDirectory(fd.isDirectory());
            // set file size
            fileDescription.setSize(fd.getSize());
            return fileDescription;
        }
        // ignore current file and skip to next one
        return null;
    }

    @Override
    public LoadDocumentEntity loadDocumentDescription(LoadDocumentRequest loadDocumentRequest, boolean loadAllPages) {
        // get/set parameters
        String documentGuid = loadDocumentRequest.getGuid();
        if (!DefaultDirectories.isAbsolutePath(documentGuid)) {
            documentGuid = viewerConfiguration.getFilesDirectory() + File.separator + documentGuid;
        }
        String password = loadDocumentRequest.getPassword();
        // get document info options
        DocumentInfoOptions documentInfoOptions = getDocumentInfoOptions(password);
        try {
            // get document info container
            DocumentInfoContainer documentInfoContainer = viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions);

            // return document description
            return getLoadDocumentEntity(documentGuid, password, documentInfoContainer, loadAllPages);
        } catch (GroupDocsViewerException ex) {
            throw new TotalGroupDocsException(getExceptionMessage(password, ex), ex);
        } catch (Exception ex) {
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    private LoadDocumentEntity getLoadDocumentEntity(String documentGuid, String password, DocumentInfoContainer documentInfoContainer, boolean loadAllPages) throws Exception {
        List<Page> pagesData = loadAllPages ? getPagesData(documentGuid, password) : Collections.EMPTY_LIST;

        List<PageDescriptionEntity> pages = getPageDescriptionEntities(documentInfoContainer.getPages(), pagesData);

        LoadDocumentEntity loadDocumentEntity = new LoadDocumentEntity();
        loadDocumentEntity.setGuid(documentGuid);
        loadDocumentEntity.setPages(pages);
        if (viewerConfiguration.getPrintAllowed() && PDF.equals(FilenameUtils.getExtension(documentGuid)) && documentInfoContainer instanceof PdfDocumentInfoContainer) {
            loadDocumentEntity.setPrintAllowed(((PdfDocumentInfoContainer) documentInfoContainer).getPrintingAllowed());
        }
        return loadDocumentEntity;
    }

    protected List<Page> getPagesData(String documentGuid, String password) throws Exception {
        if (viewerConfiguration.isHtmlMode()) {
            HtmlOptions htmlOptions = createCommonHtmlOptions(password, viewerConfiguration.getWatermarkText());
            return viewerHandler.getPages(documentGuid, htmlOptions);
        } else {
            ImageOptions imageOptions = createCommonImageOptions(password, viewerConfiguration.getWatermarkText());
            return viewerHandler.getPages(documentGuid, imageOptions);
        }
    }

    @Override
    public PageDescriptionEntity loadDocumentPage(LoadDocumentPageRequest loadDocumentPageRequest) {
        try {
            String documentGuid = loadDocumentPageRequest.getGuid();
            int pageNumber = loadDocumentPageRequest.getPage();
            String password = loadDocumentPageRequest.getPassword();
            DocumentInfoOptions documentInfoOptions = getDocumentInfoOptions(password);
            PageData pageData = viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions).getPages().get(pageNumber - 1);
            PageDescriptionEntity loadedPage = getPageDescriptionEntity(pageData);
            // set options
            if (viewerConfiguration.isHtmlMode()) {
                HtmlOptions htmlOptions = createHtmlOptions(pageNumber, password, viewerConfiguration.getWatermarkText());
                // get page HTML
                PageHtml page = (PageHtml) viewerHandler.getPages(documentGuid, htmlOptions).get(0);
                loadedPage.setData(page.getHtmlContent());
            } else {
                ImageOptions imageOptions = createImageOptions(pageNumber, password, viewerConfiguration.getWatermarkText());
                // get page image
                PageImage page = (PageImage) viewerHandler.getPages(documentGuid, imageOptions).get(0);
                loadedPage.setData(getStringFromStream(page.getStream()));
            }
            // return loaded page object
            return loadedPage;
        } catch (Exception ex) {
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<RotatedPageEntity> rotateDocumentPages(RotateDocumentPagesRequest rotateDocumentPagesRequest) {
        try {
            // get/set parameters
            String documentGuid = rotateDocumentPagesRequest.getGuid();
            List<Integer> pages = rotateDocumentPagesRequest.getPages();
            String password = rotateDocumentPagesRequest.getPassword();
            DocumentInfoOptions documentInfoOptions = getDocumentInfoOptions(password);
            // a list of the rotated pages info
            List<RotatedPageEntity> rotatedPages = new ArrayList<>();
            // rotate pages
            for (int i = 0; i < pages.size(); i++) {
                int pageNumber = pages.get(i);
                RotatePageOptions rotateOptions = new RotatePageOptions(pageNumber, rotateDocumentPagesRequest.getAngle());
                // set password for protected document
                if (StringUtils.isNotEmpty(password)) {
                    rotateOptions.setPassword(password);
                }
                // perform page rotation
                viewerHandler.rotatePage(documentGuid, rotateOptions);
                int resultAngle = viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions).getPages().get(pageNumber - 1).getAngle();
                // prepare rotated page info object
                RotatedPageEntity rotatedPage = getRotatedPageEntity(pageNumber, resultAngle);
                // add rotated page object into resulting list
                rotatedPages.add(rotatedPage);
            }
            return rotatedPages;
        } catch (Exception ex) {
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    private DocumentInfoOptions getDocumentInfoOptions(String password) {
        DocumentInfoOptions documentInfoOptions = new DocumentInfoOptions();
        // set password for protected document
        if (StringUtils.isNotEmpty(password)) {
            documentInfoOptions.setPassword(password);
        }
        return documentInfoOptions;
    }

    protected String getExceptionMessage(String password, GroupDocsViewerException ex) {
        // Set exception message
        if (GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class)) {
            return StringUtils.isEmpty(password) ? PASSWORD_REQUIRED : INCORRECT_PASSWORD;
        } else {
            return ex.getMessage();
        }
    }

    protected RotatedPageEntity getRotatedPageEntity(int pageNumber, int resultAngle) {
        RotatedPageEntity rotatedPage = new RotatedPageEntity();
        // add rotated page number
        rotatedPage.setPageNumber(pageNumber);
        // add rotated page angle
        rotatedPage.setAngle(resultAngle);
        return rotatedPage;
    }

    protected List<PageDescriptionEntity> getPageDescriptionEntities(List<PageData> containerPages, List<Page> pagesData) throws IOException {
        List<PageDescriptionEntity> pages = new ArrayList<>();
        for (int i = 0; i < containerPages.size(); i++) {
            PageDescriptionEntity pageDescriptionEntity = getPageDescriptionEntity(containerPages.get(i));
            if (!pagesData.isEmpty()) {
                Page pageData = pagesData.get(i);
                pageDescriptionEntity.setData(getPageData(pageData));
            }
            pages.add(pageDescriptionEntity);
        }
        return pages;
    }

    private String getPageData(Page pageData) throws IOException {
        if (viewerConfiguration.isHtmlMode()) {
            return ((PageHtml) pageData).getHtmlContent();
        } else {
            return getStringFromStream(((PageImage) pageData).getStream());
        }
    }

    private PageDescriptionEntity getPageDescriptionEntity(PageData page) {
        PageDescriptionEntity pageDescriptionEntity = new PageDescriptionEntity();
        pageDescriptionEntity.setNumber(page.getNumber());
        pageDescriptionEntity.setAngle(page.getAngle());
        pageDescriptionEntity.setHeight(page.getHeight());
        pageDescriptionEntity.setWidth(page.getWidth());
        return pageDescriptionEntity;
    }

    private String getStringFromStream(InputStream inputStream) throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        // encode ByteArray into String
        return Base64.getEncoder().encodeToString(bytes);
    }
}
