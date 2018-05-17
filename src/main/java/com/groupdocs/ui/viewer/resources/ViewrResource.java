package com.groupdocs.ui.viewer.resources;

import com.groupdocs.ui.viewer.config.ViewerConfig;
import com.groupdocs.ui.viewer.domain.web.MediaType;
import com.groupdocs.ui.viewer.domain.wrapper.ErrorMsgWrapper;
import com.groupdocs.ui.viewer.domain.wrapper.FileDescriptionWrapper;
import com.groupdocs.ui.viewer.domain.wrapper.LoadedPageWrapper;
import com.groupdocs.ui.viewer.domain.wrapper.RotatedPageWrapper;
import com.groupdocs.ui.viewer.domain.wrapper.UploadedDocumentWrapper;
import com.groupdocs.ui.viewer.views.Viewer;
import com.google.gson.Gson;
import com.groupdocs.viewer.converter.options.HtmlOptions;
import com.groupdocs.viewer.converter.options.ImageOptions;
import com.groupdocs.viewer.domain.FileDescription;
import com.groupdocs.viewer.domain.containers.DocumentInfoContainer;
import com.groupdocs.viewer.domain.containers.FileListContainer;
import com.groupdocs.viewer.domain.options.DocumentInfoOptions;
import com.groupdocs.viewer.domain.options.FileListOptions;
import com.groupdocs.viewer.domain.options.RotatePageOptions;
import com.groupdocs.viewer.exception.GroupDocsViewerException;
import com.groupdocs.viewer.exception.InvalidPasswordException;
import com.groupdocs.viewer.handler.ViewerHtmlHandler;
import com.groupdocs.viewer.handler.ViewerImageHandler;
import com.groupdocs.viewer.licensing.License;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;

/**
 * QuickView
 *
 * @author Aspose Pty Ltd
 */

@Path(value = "/viewer")
public class ViewrResource extends ViewerResourcesBase{
    private final ViewerConfig viewerConfig;
    private final ViewerHtmlHandler viewerHtmlHandler;
    private final ViewerImageHandler viewerImageHandler;

    /**
     * Constructor
     * @param viewerConfig config object
     */
    public ViewrResource(ViewerConfig viewerConfig) throws UnknownHostException {
        this.viewerConfig = viewerConfig;
        // set HTTP port
        SimpleServerFactory serverFactory = (SimpleServerFactory) viewerConfig.getServerFactory();
        ConnectorFactory connector = serverFactory.getConnector();
        viewerConfig.getServer().setHttpPort(((HttpConnectorFactory) connector).getPort());
        // set host address
        viewerConfig.getServer().setHostAddress(InetAddress.getLocalHost().getHostAddress());
        // create viewer application configuration
        com.groupdocs.viewer.config.ViewerConfig config = new com.groupdocs.viewer.config.ViewerConfig();
        config.setStoragePath(viewerConfig.getApplication().getFilesDirectory());
        config.setUseCache(true);
        config.getFontDirectories().add(viewerConfig.getApplication().getFontsDirectory());
        // set GroupDocs license
        License license = new License();
        license.setLicense(viewerConfig.getApplication().getLicensePath());
        // initialize viewer instance for the HTML mode
        viewerHtmlHandler = new ViewerHtmlHandler(config);
        // initialize viewer instance for the Image mode
        viewerImageHandler = new ViewerImageHandler(config);
    }

    /**
     * Get and set index page
     * @return html view
     */
    @GET
    public Viewer getView(){
        // initiate index page
        return new Viewer(viewerConfig);
    }

    /**
     * Get files and directories
     * @param request
     * @param response
     * @return files and directories list
     */
    @POST
    @Path(value = "/loadFileTree")
    public Object loadFileTree(@Context HttpServletRequest request, @Context HttpServletResponse response){
        // set response content type
        setResponseContentType(response, MediaType.APPLICATION_JSON);
        // get request body
        String requestBody = getRequestBody(request);
        String relDirPath = getJsonString(requestBody, "path");
        // get file list from storage path
        FileListOptions fileListOptions = new FileListOptions(relDirPath);
        // get temp directory name
        String tempDirectoryName =  new com.groupdocs.viewer.config.ViewerConfig().getCacheFolderName();
        try{
            FileListContainer fileListContainer = viewerImageHandler.getFileList(fileListOptions);

            ArrayList<FileDescriptionWrapper> fileList = new ArrayList<>();
            // parse files/folders list
            for(FileDescription fd : fileListContainer.getFiles()){
                FileDescriptionWrapper fileDescription = new FileDescriptionWrapper();
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
            return objectToJson(fileList);
        }catch (Exception ex){
            // set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            errorMsgWrapper.setMessage(ex.getMessage());
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        }
    }

    /**
     * Get document description
     * @param request
     * @param response
     * @return document description
     */
    @POST
    @Path(value = "/loadDocumentDescription")
    public Object loadDocumentDescription(@Context HttpServletRequest request, @Context HttpServletResponse response){
        // set response content type
        setResponseContentType(response, MediaType.APPLICATION_JSON);
        String password = "";
        try {
            // get request body
            String requestBody = getRequestBody(request);
            // get/set parameters
            String documentGuid = getJsonString(requestBody, "guid");
            boolean htmlMode = getJsonBoolean(requestBody, "htmlMode");
            password = getJsonString(requestBody, "password");
            // check if documentGuid contains path or only file name
            if(!Paths.get(documentGuid).isAbsolute()){
                documentGuid = viewerConfig.getApplication().getFilesDirectory() + "/" + documentGuid;
            }
            DocumentInfoContainer documentInfoContainer = new DocumentInfoContainer();
            // get document info options
            DocumentInfoOptions documentInfoOptions = new DocumentInfoOptions(documentGuid);
            // set password for protected document
            if(!password.isEmpty() && password != null) {
                documentInfoOptions.setPassword(password);
            }
            // get document info container
           if (htmlMode) {
                documentInfoContainer = viewerHtmlHandler.getDocumentInfo(documentGuid, documentInfoOptions);
            } else {
                documentInfoContainer = viewerImageHandler.getDocumentInfo(documentGuid, documentInfoOptions);
            }
            // return document description
            return objectToJson(documentInfoContainer.getPages());
        }catch (GroupDocsViewerException ex){
            // Set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            if(GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class) && password.isEmpty()) {
                errorMsgWrapper.setMessage("Password Required");
            }else if(GroupDocsViewerException.class.isAssignableFrom(InvalidPasswordException.class) && !password.isEmpty()){
                errorMsgWrapper.setMessage("Incorrect password");
            }else{
                errorMsgWrapper.setMessage(ex.getMessage());
            }
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        }catch (Exception ex){
            // set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            errorMsgWrapper.setMessage(ex.getMessage());
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        }
    }

    /**
     * Get document page
     * @param request
     * @param response
     * @return document page
     */
    @POST
    @Path(value = "/loadDocumentPage")
    public Object loadDocumentPage(@Context HttpServletRequest request, @Context HttpServletResponse response){
        try {
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // get request body
            String requestBody = getRequestBody(request);
            // get/set parameters
            String documentGuid = getJsonString(requestBody, "guid");
            int pageNumber = getJsonInteger(requestBody, "page");
            boolean htmlMode = getJsonBoolean(requestBody, "htmlMode");
            String password = getJsonString(requestBody, "password");
            LoadedPageWrapper loadedPage = new LoadedPageWrapper();
            String angle = "0";
            // set options
            if(htmlMode) {
                HtmlOptions htmlOptions = new HtmlOptions();
                htmlOptions.setPageNumber(pageNumber);
                htmlOptions.setCountPagesToRender(1);
                htmlOptions.setResourcesEmbedded(true);
                // set password for protected document
                if(!password.isEmpty() && password != null) {
                    htmlOptions.setPassword(password);
                }
                // get page HTML
                loadedPage.setPageHtml(viewerHtmlHandler.getPages(documentGuid, htmlOptions).get(0).getHtmlContent());
                // get page rotation angle
                angle = String.valueOf(viewerHtmlHandler.getDocumentInfo(documentGuid).getPages().get(pageNumber - 1).getAngle());
            } else {
                ImageOptions imageOptions = new ImageOptions();
                imageOptions.setPageNumber(pageNumber);
                imageOptions.setCountPagesToRender(1);
                // set password for protected document
                if(!password.isEmpty()) {
                    imageOptions.setPassword(password);
                }
                // get page image
                byte[] bytes = IOUtils.toByteArray(viewerImageHandler.getPages(documentGuid, imageOptions).get(0).getStream());
                // encode ByteArray into String
                String incodedImage = new String(Base64.getEncoder().encode(bytes));
                loadedPage.setPageImage(incodedImage);
                // get page rotation angle
                angle = String.valueOf(viewerImageHandler.getDocumentInfo(documentGuid).getPages().get(pageNumber - 1).getAngle());
            }
            loadedPage.setAngle(angle);
            // return loaded page object
            return objectToJson(loadedPage);
        }catch (Exception ex){
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            errorMsgWrapper.setMessage(ex.getMessage());
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        }
    }

    /**
     * Rotate page(s)
     * @param request
     * @param response
     * @return rotated pages list (each obejct contains page number and rotated angle information)
     */
    @POST
    @Path(value = "/rotateDocumentPages")
    public Object rotateDocumentPages(@Context HttpServletRequest request, @Context HttpServletResponse response){
        try {
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // get request body
            String requestBody = getRequestBody(request);
            // get/set parameters
            String documentGuid = getJsonString(requestBody, "guid");
            int angle =  Integer.parseInt(getJsonString(requestBody, "angle"));
            JSONArray pages = new JSONObject(requestBody).getJSONArray("pages");
            boolean htmlMode = getJsonBoolean(requestBody, "htmlMode");
            String password = getJsonString(requestBody, "password");
            // a list of the rotated pages info
            ArrayList<RotatedPageWrapper> rotatedPages = new ArrayList<RotatedPageWrapper>();
            // rotate pages
            for(int i = 0; i < pages.length(); i++) {
                // prepare rotated page info object
                RotatedPageWrapper rotatedPage = new RotatedPageWrapper();
                int pageNumber = Integer.parseInt(pages.get(i).toString());
                RotatePageOptions rotateOptions = new RotatePageOptions(pageNumber, angle);
                // perform page rotation
                String resultAngle = "0";
                // set password for protected document
                if(!password.isEmpty() && password != null) {
                    rotateOptions.setPassword(password);
                }
                if(htmlMode) {
                    viewerHtmlHandler.rotatePage(documentGuid, rotateOptions);
                    resultAngle = String.valueOf(viewerHtmlHandler.getDocumentInfo(documentGuid).getPages().get(pageNumber - 1).getAngle());
                } else {
                    viewerImageHandler.rotatePage(documentGuid, rotateOptions);
                    resultAngle = String.valueOf(viewerImageHandler.getDocumentInfo(documentGuid).getPages().get(pageNumber - 1).getAngle());
                }
                // add rotated page number
                rotatedPage.setPageNumber(pageNumber);
                // add rotated page angle
                rotatedPage.setAngle(resultAngle);
                // add rotated page object into resulting list
                rotatedPages.add(rotatedPage);
            }
            return new Gson().toJson(rotatedPages);
        }catch (Exception ex){
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            errorMsgWrapper.setMessage(ex.getMessage());
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        }
    }

    /**
     * Download document
     * @param request
     * @param response
     */
    @GET
    @Path(value = "/downloadDocument")
    public Object downloadDocument(@Context HttpServletRequest request, @Context HttpServletResponse response) throws ServletException, IOException {
        int bytesRead = 0;
        int count = 0;
        byte[] buff = new byte[16 * 1024];
        OutputStream out = response.getOutputStream();
        // set response content type
        setResponseContentType(response, MediaType.APPLICATION_OCTET_STREAM);
        // get document path
        String documentGuid = request.getParameter("path");
        String fileName = new File(documentGuid).getName();
        // set response content disposition
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        BufferedOutputStream outStream = null;
        BufferedInputStream inputStream = null;
        try {
            // download the document
            inputStream = new BufferedInputStream(new FileInputStream(documentGuid));
            outStream = new BufferedOutputStream(out);
            while ((count = inputStream.read(buff)) != -1) {
                outStream.write(buff, 0, count);
            }
            return outStream;
        } catch (Exception ex){
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            errorMsgWrapper.setMessage(ex.getMessage());
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        } finally {
            // close streams
            if (inputStream != null)
                inputStream.close();
            if (outStream != null)
                outStream.close();
        }
    }

    /**
     * Upload document
     * @param request
     * @param response
     * @return uploaded document object (the object contains uploaded document guid)
     */
    @POST
    @Path(value = "/uploadDocument")
    public Object uploadDocument(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            // set multipart configuration
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
            request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // get the file chosen by the user
            Part filePart = request.getPart("file");
            // get document URL
            String documentUrl = request.getParameter("url");
            // get rewrite mode
            boolean rewrite = Boolean.parseBoolean(request.getParameter("rewrite"));
            InputStream uploadedInputStream = null;
            String fileName = "";
            if(documentUrl.isEmpty() || documentUrl == null) {
                // get the InputStream to store the file
                uploadedInputStream = filePart.getInputStream();
                fileName = filePart.getSubmittedFileName();
            } else {
                // get the InputStream from the URL
                URL url =  new URL(documentUrl);
                uploadedInputStream = url.openStream();
                fileName = FilenameUtils.getName(url.getPath());
            }
            // get documents storage path
            String documentStoragePath = viewerConfig.getApplication().getFilesDirectory();
            // save the file
            File file = new File(documentStoragePath + "/" + fileName);
            // check rewrite mode
            if(rewrite) {
                // save file with rewrite if exists
                Files.copy(uploadedInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                if (file.exists())
                {
                    // get file with new name
                    file = getFreeFileName(documentStoragePath, fileName);
                }
                // save file with out rewriting
                Files.copy(uploadedInputStream, file.toPath());
            }
            UploadedDocumentWrapper uploadedDocument = new UploadedDocumentWrapper();
            uploadedDocument.setGuid(documentStoragePath + "/" + fileName);
            return objectToJson(uploadedDocument);
        }catch(Exception ex){
            // set response content type
            setResponseContentType(response, MediaType.APPLICATION_JSON);
            // set exception message
            ErrorMsgWrapper errorMsgWrapper = new ErrorMsgWrapper();
            errorMsgWrapper.setMessage(ex.getMessage());
            errorMsgWrapper.setException(ex);
            return objectToJson(errorMsgWrapper);
        }
    }
}
