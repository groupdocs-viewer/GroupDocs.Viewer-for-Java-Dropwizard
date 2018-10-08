package com.groupdocs.ui.common;

import com.groupdocs.ui.common.config.GlobalConfiguration;
import com.groupdocs.ui.common.exception.TotalGroupDocsExceptionMapper;
import com.groupdocs.ui.common.health.TemplateHealthCheck;
import com.groupdocs.ui.viewer.resources.ViewerResources;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * Main class
 * Where all the magic starts
 *
 * @author Aspose Pty Ltd
 */

public class MainService extends Application<GlobalConfiguration> {
    
    public static void main( String[] args ) throws Exception{
        new MainService().run(args);
    }

    @Override
    public void initialize(Bootstrap<GlobalConfiguration> bootstrap) {
        // add assets bundle in order to get resources from assets directory
        bootstrap.addBundle(new AssetsBundle());
        // init view bundle
        bootstrap.addBundle(new ViewBundle());
        // for injection file content in resource methods
        bootstrap.addBundle(new MultiPartBundle());
    }

    @Override
    public void run(GlobalConfiguration globalConfiguration, Environment environment) throws Exception {
        // Enable CORS headers
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        // Initiate resources (web pages)
        environment.jersey().register(new ViewerResources(globalConfiguration));

        // Add custom exception mapper
        environment.jersey().register(new TotalGroupDocsExceptionMapper());

        // Add dummy health check to get rid of console warnings
        // TODO: implement health check
        final TemplateHealthCheck healthCheck = new TemplateHealthCheck("");
        environment.healthChecks().register("HealthCheck", healthCheck);
    }
}