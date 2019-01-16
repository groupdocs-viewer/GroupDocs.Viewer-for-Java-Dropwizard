package com.groupdocs.ui.viewer.service;

import com.groupdocs.ui.common.config.GlobalConfiguration;
import com.groupdocs.ui.common.entity.web.FileDescriptionEntity;
import com.groupdocs.ui.common.entity.web.LoadDocumentEntity;
import com.groupdocs.ui.common.entity.web.PageDescriptionEntity;
import com.groupdocs.ui.common.entity.web.request.FileTreeRequest;
import com.groupdocs.ui.common.entity.web.request.LoadDocumentPageRequest;
import com.groupdocs.ui.common.entity.web.request.LoadDocumentRequest;
import com.groupdocs.ui.common.exception.TotalGroupDocsException;
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

    private final ViewerHandler viewerHandler;
    private GlobalConfiguration globalConfiguration;

    public ViewerServiceImpl(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
        // create viewer application configuration
        ViewerConfig config = new ViewerConfig();
        String filesDirectory = globalConfiguration.getViewer().getFilesDirectory();
        if (StringUtils.isNotEmpty(filesDirectory) && !filesDirectory.endsWith(File.separator)) {
            filesDirectory = filesDirectory + File.separator;
        }
        config.setStoragePath(filesDirectory);
        config.setUseCache(globalConfiguration.getViewer().isCache());
        config.getFontDirectories().add(globalConfiguration.getViewer().getFontsDirectory());

        try {
            // set GroupDocs license
            License license = new License();
            license.setLicense(globalConfiguration.getApplication().getLicensePath());
        } catch (Throwable ex) {
            logger.error("Can not verify Viewer license!");
        }

        if (globalConfiguration.getViewer().isHtmlMode()) {
            // initialize total instance for the HTML mode
            viewerHandler = new ViewerHtmlHandler(config);
        } else {
            // initialize total instance for the Image mode
            viewerHandler = new ViewerImageHandler(config);
        }
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
        FileDescriptionEntity fileDescription = new FileDescriptionEntity();
        fileDescription.setGuid(fd.getGuid());
        // get temp directory name
        String tempDirectoryName = new ViewerConfig().getCacheFolderName();
        // check if current file/folder is temp directory or is hidden
        if (tempDirectoryName.equals(fd.getName()) || new File(fileDescription.getGuid()).isHidden()) {
            // ignore current file and skip to next one
            return null;
        } else {
            // set file/folder name
            fileDescription.setName(fd.getName());
        }
        // set file type
        fileDescription.setDocType(fd.getDocumentType());
        // set is directory true/false
        fileDescription.setDirectory(fd.isDirectory());
        // set file size
        fileDescription.setSize(fd.getSize());
        return fileDescription;
    }

    @Override
    public LoadDocumentEntity loadDocumentDescription(LoadDocumentRequest loadDocumentRequest, boolean loadAllPages) {
        // get/set parameters
        String documentGuid = getGuid(loadDocumentRequest.getGuid());
        String password = loadDocumentRequest.getPassword();
        // get document info options
        DocumentInfoOptions documentInfoOptions = getDocumentInfoOptions(documentGuid, password);
        try {
            // get document info container
            DocumentInfoContainer documentInfoContainer = viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions);
            List<Page> pagesData = Collections.EMPTY_LIST;

            if (loadAllPages) {
                pagesData = getPagesData(documentGuid, password);
            }

            List<PageDescriptionEntity> pages = getPageDescriptionEntities(documentInfoContainer.getPages(), pagesData);

            LoadDocumentEntity loadDocumentEntity = new LoadDocumentEntity();
            loadDocumentEntity.setGuid(loadDocumentRequest.getGuid());
            loadDocumentEntity.setPages(pages);
            // return document description
            return loadDocumentEntity;
        } catch (GroupDocsViewerException ex) {
            throw new TotalGroupDocsException(getExceptionMessage(password, ex), ex);
        } catch (Exception ex) {
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    protected List<Page> getPagesData(String documentGuid, String password) throws Exception {
        List<Page> pagesData;
        if (globalConfiguration.getViewer().isHtmlMode()) {
            HtmlOptions htmlOptions = createCommonHtmlOptions(password);
            pagesData = viewerHandler.getPages(documentGuid, htmlOptions);
        } else {
            ImageOptions imageOptions = createCommonImageOptions(password);
            pagesData = viewerHandler.getPages(documentGuid, imageOptions);
        }
        return pagesData;
    }

    @Override
    public PageDescriptionEntity loadDocumentPage(LoadDocumentPageRequest loadDocumentPageRequest) {
        try {
            // get/set parameters
            String documentGuid = loadDocumentPageRequest.getGuid();
            int pageNumber = loadDocumentPageRequest.getPage();
            String password = loadDocumentPageRequest.getPassword();
            DocumentInfoOptions documentInfoOptions = getDocumentInfoOptions(documentGuid, password);
            PageData pageData = viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions).getPages().get(pageNumber - 1);
            PageDescriptionEntity loadedPage = getPageDescriptionEntity(pageData);
            // set options
            if (globalConfiguration.getViewer().isHtmlMode()) {
                HtmlOptions htmlOptions = createHtmlOptions(pageNumber, password);
                // get page HTML
                PageHtml page = (PageHtml) viewerHandler.getPages(documentGuid, htmlOptions).get(0);
                loadedPage.setData(page.getHtmlContent());
            } else {
                ImageOptions imageOptions = createImageOptions(pageNumber, password);
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
            DocumentInfoOptions documentInfoOptions = getDocumentInfoOptions(documentGuid, password);
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

    private DocumentInfoOptions getDocumentInfoOptions(String documentGuid, String password) {
        DocumentInfoOptions documentInfoOptions = new DocumentInfoOptions(documentGuid);
        // set password for protected document
        if (StringUtils.isNotEmpty(password)) {
            documentInfoOptions.setPassword(password);
        }
        return documentInfoOptions;
    }

    protected String getExceptionMessage(String password, GroupDocsViewerException ex) {
        // Set exception message
        String message = ex.getMessage();
        if (GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class) && password.isEmpty()) {
            message = PASSWORD_REQUIRED;
        } else if (GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class) && !password.isEmpty()) {
            message = INCORRECT_PASSWORD;
        }
        return message;
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
            PageData page = containerPages.get(i);
            PageDescriptionEntity pageDescriptionEntity = getPageDescriptionEntity(page);
            if (!pagesData.isEmpty()) {
                Page pageData = pagesData.get(i);
                pageDescriptionEntity.setData(getPageData(pageData));
            }
            pages.add(pageDescriptionEntity);
        }
        return pages;
    }

    private String getPageData(Page pageData) throws IOException {
        boolean htmlMode = globalConfiguration.getViewer().isHtmlMode();
        if (htmlMode) {
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

    private String getGuid(String guid) {
        Iterable<java.nio.file.Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        // check if documentGuid contains path or only file name
        for (java.nio.file.Path root : rootDirectories) {
            if (guid.startsWith(root.toString())) {
                return guid;
            }
        }
        return globalConfiguration.getViewer().getFilesDirectory() + File.separator + guid;
    }
}
