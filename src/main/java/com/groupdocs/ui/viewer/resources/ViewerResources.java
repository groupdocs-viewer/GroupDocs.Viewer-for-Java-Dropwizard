package com.groupdocs.ui.viewer.resources;

import com.groupdocs.ui.common.config.GlobalConfiguration;
import com.groupdocs.ui.common.entity.web.FileDescriptionEntity;
import com.groupdocs.ui.common.entity.web.LoadedPageEntity;
import com.groupdocs.ui.common.entity.web.UploadedDocumentEntity;
import com.groupdocs.ui.common.entity.web.request.FileTreeRequest;
import com.groupdocs.ui.common.entity.web.request.LoadDocumentPageRequest;
import com.groupdocs.ui.common.entity.web.request.LoadDocumentRequest;
import com.groupdocs.ui.common.exception.TotalGroupDocsException;
import com.groupdocs.ui.common.resources.Resources;
import com.groupdocs.ui.viewer.entity.web.RotatedPageEntity;
import com.groupdocs.ui.viewer.model.web.RotateDocumentPagesRequest;
import com.groupdocs.ui.viewer.views.Viewer;
import com.groupdocs.viewer.config.ViewerConfig;
import com.groupdocs.viewer.converter.options.HtmlOptions;
import com.groupdocs.viewer.converter.options.ImageOptions;
import com.groupdocs.viewer.domain.FileDescription;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.groupdocs.ui.common.exception.PasswordExceptions.INCORRECT_PASSWORD;
import static com.groupdocs.ui.common.exception.PasswordExceptions.PASSWORD_REQUIRED;
import static javax.ws.rs.core.MediaType.*;

/**
 * Viewer Resources
 *
 * @author Aspose Pty Ltd
 */

@Path(value = "/viewer")
public class ViewerResources extends Resources {

    private static final Logger logger = LoggerFactory.getLogger(ViewerResources.class);

    private final ViewerHandler viewerHandler;

    /**
     * Constructor
     * @param globalConfiguration global configuration object
     * @throws UnknownHostException
     */
    public ViewerResources(GlobalConfiguration globalConfiguration) throws UnknownHostException {
        super(globalConfiguration);

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

    /**
     * Get and set viewer page
     * @return html view
     */
    @GET
    public Viewer getView(){
        // initiate index page
        return new Viewer(globalConfiguration, DEFAULT_CHARSET);
    }

    /**
     * Get files and directories
     * @param fileTreeRequest request's object with specified path
     * @return files and directories list
     */
    @POST
    @Path(value = "/loadFileTree")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public List<FileDescriptionEntity> loadFileTree(FileTreeRequest fileTreeRequest){

        String relDirPath = fileTreeRequest.getPath();
        // get file list from storage path
        FileListOptions fileListOptions = new FileListOptions(relDirPath);
        // get temp directory name
        String tempDirectoryName =  new com.groupdocs.viewer.config.ViewerConfig().getCacheFolderName();
        try{
            FileListContainer fileListContainer = viewerHandler.getFileList(fileListOptions);

            List<FileDescriptionEntity> fileList = new ArrayList<>();
            // parse files/folders list
            for(FileDescription fd : fileListContainer.getFiles()){
                FileDescriptionEntity fileDescription = new FileDescriptionEntity();
                fileDescription.setGuid(fd.getGuid());
                // check if current file/folder is temp directory or is hidden
                if(tempDirectoryName.equals(fd.getName()) || new File(fileDescription.getGuid()).isHidden()) {
                    // ignore current file and skip to next one
                    continue;
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
                // add object to array list
                fileList.add(fileDescription);
            }
            return fileList;
        }catch (Exception ex){
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    /**
     * Get document description
     * @param loadDocumentRequest request's object with parameters
     * @return document description
     */
    @POST
    @Path(value = "/loadDocumentDescription")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public List<PageData> loadDocumentDescription(LoadDocumentRequest loadDocumentRequest){
        String password = "";
        try {
            // get/set parameters
            String documentGuid = loadDocumentRequest.getGuid();
            password = loadDocumentRequest.getPassword();
            // check if documentGuid contains path or only file name
            if(!Paths.get(documentGuid).isAbsolute()){
                documentGuid = globalConfiguration.getViewer().getFilesDirectory() + File.separator + documentGuid;
            }
            DocumentInfoContainer documentInfoContainer;
            // get document info options
            DocumentInfoOptions documentInfoOptions = new DocumentInfoOptions(documentGuid);
            // set password for protected document
            if (StringUtils.isNotEmpty(password)) {
                documentInfoOptions.setPassword(password);
            }
            // get document info container
            documentInfoContainer = viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions);
            // return document description
            return documentInfoContainer.getPages();
        }catch (GroupDocsViewerException ex){
            // Set exception message
            String message = ex.getMessage();
            if(GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class) && password.isEmpty()) {
                message = PASSWORD_REQUIRED;
            }else if(GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class) && !password.isEmpty()){
                message = INCORRECT_PASSWORD;
            }
            throw new TotalGroupDocsException(message, ex);
        }catch (Exception ex){
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    /**
     * Get document page
     * @param loadDocumentPageRequest request's object with parameters
     * @return document page
     */
    @POST
    @Path(value = "/loadDocumentPage")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public LoadedPageEntity loadDocumentPage(LoadDocumentPageRequest loadDocumentPageRequest){
        try {
            // get/set parameters
            String documentGuid = loadDocumentPageRequest.getGuid();
            int pageNumber = loadDocumentPageRequest.getPage();
            String password = loadDocumentPageRequest.getPassword();
            LoadedPageEntity loadedPage = new LoadedPageEntity();
            DocumentInfoOptions documentInfoOptions = new DocumentInfoOptions(documentGuid);
            // set password for protected document
            if (StringUtils.isNotEmpty(password)) {
                documentInfoOptions.setPassword(password);
            }
            String angle;
            // set options
            if(globalConfiguration.getViewer().isHtmlMode()) {
                HtmlOptions htmlOptions = new HtmlOptions();
                htmlOptions.setPageNumber(pageNumber);
                htmlOptions.setCountPagesToRender(1);
                htmlOptions.setResourcesEmbedded(true);
                // set password for protected document
                if (StringUtils.isNotEmpty(password)) {
                    htmlOptions.setPassword(password);
                }
                // get page HTML
                PageHtml page = (PageHtml) viewerHandler.getPages(documentGuid, htmlOptions).get(0);
                loadedPage.setPageHtml(page.getHtmlContent());
                // get page rotation angle
                angle = String.valueOf(viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions).getPages().get(pageNumber - 1).getAngle());
            } else {
                ImageOptions imageOptions = new ImageOptions();
                imageOptions.setPageNumber(pageNumber);
                imageOptions.setCountPagesToRender(1);
                // set password for protected document
                if (StringUtils.isNotEmpty(password)) {
                    imageOptions.setPassword(password);
                }
                // get page image
                PageImage page = (PageImage) viewerHandler.getPages(documentGuid, imageOptions).get(0);
                byte[] bytes = IOUtils.toByteArray(page.getStream());
                // encode ByteArray into String
                String encodedImage = new String(Base64.getEncoder().encode(bytes));
                loadedPage.setPageImage(encodedImage);
                // get page rotation angle
                angle = String.valueOf(viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions).getPages().get(pageNumber - 1).getAngle());
            }
            loadedPage.setAngle(angle);
            // return loaded page object
            return loadedPage;
        }catch (Exception ex){
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    /**
     * Rotate page(s)
     * @param rotateDocumentPagesRequest request's object with parameters
     * @return rotated pages list (each object contains page number and rotated angle information)
     */
    @POST
    @Path(value = "/rotateDocumentPages")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public List<RotatedPageEntity> rotateDocumentPages(RotateDocumentPagesRequest rotateDocumentPagesRequest){
        try {
            // get/set parameters
            String documentGuid = rotateDocumentPagesRequest.getGuid();
            int angle =  rotateDocumentPagesRequest.getAngle();
            List<Integer> pages = rotateDocumentPagesRequest.getPages();
            String password = rotateDocumentPagesRequest.getPassword();
            DocumentInfoOptions documentInfoOptions = new DocumentInfoOptions(documentGuid);
            // set password for protected document
            if (StringUtils.isNotEmpty(password)) {
                documentInfoOptions.setPassword(password);
            }
            // a list of the rotated pages info
            List<RotatedPageEntity> rotatedPages = new ArrayList<>();
            // rotate pages
            for(int i = 0; i < pages.size(); i++) {
                // prepare rotated page info object
                RotatedPageEntity rotatedPage = new RotatedPageEntity();
                int pageNumber = Integer.parseInt(pages.get(i).toString());
                RotatePageOptions rotateOptions = new RotatePageOptions(pageNumber, angle);
                // perform page rotation
                String resultAngle;
                // set password for protected document
                if (StringUtils.isNotEmpty(password)) {
                    rotateOptions.setPassword(password);
                }
                viewerHandler.rotatePage(documentGuid, rotateOptions);
                resultAngle = String.valueOf(viewerHandler.getDocumentInfo(documentGuid, documentInfoOptions).getPages().get(pageNumber - 1).getAngle());
                // add rotated page number
                rotatedPage.setPageNumber(pageNumber);
                // add rotated page angle
                rotatedPage.setAngle(resultAngle);
                // add rotated page object into resulting list
                rotatedPages.add(rotatedPage);
            }
            return rotatedPages;
        }catch (Exception ex){
            throw new TotalGroupDocsException(ex.getMessage(), ex);
        }
    }

    /**
     * Download document
     * @param documentGuid path to document parameter
     * @param response
     */
    @GET
    @Path(value = "/downloadDocument")
    @Produces(APPLICATION_OCTET_STREAM)
    public void downloadDocument(@QueryParam("path") String documentGuid, @Context HttpServletResponse response) throws IOException {
        downloadFile(response, documentGuid);
    }

    /**
     * Upload document
     * @param inputStream file content
     * @param fileDetail file description
     * @param documentUrl url for document
     * @param rewrite flag for rewriting file
     * @return uploaded document object (the object contains uploaded document guid)
     */
    @POST
    @Path(value = "/uploadDocument")
    @Produces(APPLICATION_JSON)
    @Consumes(MULTIPART_FORM_DATA)
    public UploadedDocumentEntity uploadDocument(@FormDataParam("file") InputStream inputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileDetail,
                                 @FormDataParam("url") String documentUrl,
                                 @FormDataParam("rewrite") Boolean rewrite) {
        // upload file
        String pathname = uploadFile(documentUrl, inputStream, fileDetail, rewrite, null);
        // create response
        UploadedDocumentEntity uploadedDocument = new UploadedDocumentEntity();
        uploadedDocument.setGuid(pathname);
        return uploadedDocument;
    }

    @Override
    protected String getStoragePath(Map<String, Object> params) {
        return globalConfiguration.getViewer().getFilesDirectory();
    }
}
