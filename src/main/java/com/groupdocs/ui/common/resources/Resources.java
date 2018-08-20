package com.groupdocs.ui.common.resources;

import com.groupdocs.ui.common.config.GlobalConfiguration;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resources
 *
 * @author Aspose Pty Ltd
 */
public abstract class Resources {
    protected final String DEFAULT_CHARSET = "UTF-8";
    protected final GlobalConfiguration globalConfiguration;

    /**
     * Constructor
     * @param globalConfiguration global application configuration
     * @throws UnknownHostException
     */
    public Resources(GlobalConfiguration globalConfiguration) throws UnknownHostException {
        this.globalConfiguration = globalConfiguration;

        // set HTTP port
        SimpleServerFactory serverFactory = (SimpleServerFactory) globalConfiguration.getServerFactory();
        ConnectorFactory connector = serverFactory.getConnector();
        globalConfiguration.getServer().setHttpPort(((HttpConnectorFactory) connector).getPort());

        // set host address
        globalConfiguration.getServer().setHostAddress(InetAddress.getLocalHost().getHostAddress());

    }

    /**
     * Rename file if exist
     * @param directory directory where files are located
     * @param fileName file name
     * @return new file with new file name
     */
    protected File getFreeFileName(String directory, String fileName){
        File file = null;
        try {
            File folder = new File(directory);
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                int number = i + 1;
                String newFileName = FilenameUtils.removeExtension(fileName) + "-Copy(" + number + ")." + FilenameUtils.getExtension(fileName);
                file = new File(directory + "/" + newFileName);
                if(file.exists()) {
                    continue;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}
