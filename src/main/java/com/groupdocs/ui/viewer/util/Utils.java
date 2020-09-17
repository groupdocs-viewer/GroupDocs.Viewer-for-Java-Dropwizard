package com.groupdocs.ui.viewer.util;

import com.groupdocs.ui.common.exception.PasswordExceptions;
import com.groupdocs.viewer.Viewer;
import com.groupdocs.viewer.options.ViewInfoOptions;
import com.groupdocs.viewer.results.Page;
import com.groupdocs.viewer.results.ViewInfo;

import javax.ws.rs.core.MediaType;
import java.util.List;

public class Utils {

    /**
     * Get correct message for security exceptions
     */
    public static String getExceptionMessage(String password) {
        return password == null || password.isEmpty() ? PasswordExceptions.PASSWORD_REQUIRED : PasswordExceptions.INCORRECT_PASSWORD;
    }

    public static MediaType detectMediaType(String fileName) {
        switch (fileName.substring(fileName.lastIndexOf('.'))) {
            case ".png":
                return MediaType.valueOf("image/png");
            case ".jpeg":
            case ".jpg":
                return MediaType.valueOf("image/jpg");
            case ".js":
                return MediaType.valueOf("text/javascript");
            case ".css":
                return MediaType.valueOf("text/css");
        }
        return MediaType.APPLICATION_OCTET_STREAM_TYPE;
    }

    public static void applyWidthHeightFix(Viewer viewer, ViewInfo viewInfo) {
        // Fix to detect size, because there is a bug with detecting size in HTML mode
        // The bug is already fixed in .NET and will be fixed in the next version of Java viewer
        final ViewInfo fixViewInfo = viewer.getViewInfo(ViewInfoOptions.forPngView(false));
        final List<Page> fixPages = fixViewInfo.getPages();
        final List<Page> pages = viewInfo.getPages();
        for (int n = 0; n < Math.min(fixPages.size(), pages.size()); n++) {
            final Page page = pages.get(n);
            final Page fixPage = fixPages.get(n);
            pages.set(n, new Page(page.getNumber(), page.isVisible(), fixPage.getWidth(), fixPage.getHeight(), page.getLines()));
        }
    }
}
