package com.groupdocs.ui.viewer.util;

import com.groupdocs.ui.common.exception.PasswordExceptions;
import com.groupdocs.ui.common.exception.TotalGroupDocsException;
import com.groupdocs.viewer.Viewer;
import com.groupdocs.viewer.options.ViewInfoOptions;
import com.groupdocs.viewer.results.Page;
import com.groupdocs.viewer.results.ViewInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Get correct message for security exceptions
     */
    public static String getExceptionMessage(String password) {
        return password == null || password.isEmpty() ? PasswordExceptions.PASSWORD_REQUIRED : PasswordExceptions.INCORRECT_PASSWORD;
    }

    public static MediaType detectMediaType(String fileName) {
        String mediaType;
        try {
            mediaType = Files.probeContentType(new File(fileName).toPath());
            if (mediaType == null) {
                mediaType = URLConnection.guessContentTypeFromName(fileName);
            }
            if (mediaType == null) {
                mediaType = new MimetypesFileTypeMap().getContentType(fileName);
            }
            if (mediaType == null || (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM) && fileName.contains("."))) {
                final String extension = fileName.substring(fileName.lastIndexOf("."));
                switch (extension) {
                    case ".otf":
                        mediaType = "font/otf";
                        break;
                    case ".sfnt":
                        mediaType = "font/sfnt";
                        break;
                    case ".ttf":
                        mediaType = "font/ttf";
                        break;
                    case ".woff":
                        mediaType = "font/woff";
                        break;
                    case ".woff2":
                        mediaType = "font/woff2";
                        break;
                    case ".eot":
                        mediaType = "application/vnd.ms-fontobject";
                        break;
                    default:
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }
            }
        } catch (IOException e) {
            logger.warn("Can't detect content type using file name '" + fileName + "'");
            throw new TotalGroupDocsException("Can't detect content type using file name '" + fileName + "'", e);
        }
        return MediaType.valueOf(mediaType);
    }

    public static void applyWidthHeightFix(Viewer viewer, ViewInfo viewInfo) {
        // Fix to detect size, because there is a bug with detecting size in HTML mode
        // The bug is already fixed in .NET and will be fixed in the next version of Java viewer
        final ViewInfo fixViewInfo = viewer.getViewInfo(ViewInfoOptions.forPngView(false));
        final List<Page> pages = viewInfo.getPages();
        final List<Page> fixPages = fixViewInfo.getPages();
        int lastFixWidth = 0, lastFixHeight = 0;
        for (int n = 0; n < Math.min(fixPages.size(), pages.size()); n++) {
            final Page page = pages.get(n);
            final Page fixPage = fixPages.get(n);
            int fixWidth = fixPage.getWidth();
            int fixHeight = fixPage.getHeight();
            if (page.getWidth() == 0 && page.getHeight() == 0) {
                pages.set(n, new Page(page.getNumber(), page.isVisible(), (fixWidth == 0) ? lastFixWidth : fixWidth, (fixHeight == 0) ? lastFixHeight : fixHeight, page.getLines()));
            }
            lastFixWidth = pages.get(n).getWidth();
            lastFixHeight = pages.get(n).getHeight();
        }
    }
}
