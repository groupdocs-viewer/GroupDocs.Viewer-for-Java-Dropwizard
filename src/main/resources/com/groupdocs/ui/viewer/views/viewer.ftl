<#-- @ftlvariable name="" type="Viewer" -->
<!DOCTYPE html>
<html>
    <head>
        <title>GroupDocs.Viewer for Java Dropwizard</title>
        <link type="text/css" rel="stylesheet" href="assets/common/css/font-awesome.min.css"/>
        <link type="text/css" rel="stylesheet" href="assets/common/css/swiper.min.css">
        <link type="text/css" rel="stylesheet" href="assets/common/css/circle-progress.css"/>
        <link type="text/css" rel="stylesheet" href="assets/viewer/css/viewer.css"/>
        <link type="text/css" rel="stylesheet" href="assets/viewer/css/viewer.mobile.css"/>
        <link type="text/css" rel="stylesheet" href="assets/viewer/css/viewer-dark.css"/>
        <script type="text/javascript" src="assets/common/js/jquery.min.js"></script>
		<script type="text/javascript" src="assets/common/js/swiper.min.js"></script>
        <script type="text/javascript" src="assets/viewer/js/viewer.js"></script>
    </head>
    <body>
        <div id="element"></div>
        <script type="text/javascript">
            $('#element').viewer({
                applicationPath: 'http://${globalConfiguration.server.hostAddress}:${globalConfiguration.server.httpPort?c}/viewer',
				defaultDocument: '${globalConfiguration.viewer.defaultDocument}',
				htmlMode: ${globalConfiguration.viewer.htmlMode?c},
                preloadPageCount: ${globalConfiguration.viewer.preloadPageCount?c},
				zoom : ${globalConfiguration.viewer.zoom?c},
				pageSelector: ${globalConfiguration.common.pageSelector?c},
				search: ${globalConfiguration.viewer.search?c},
				thumbnails: ${globalConfiguration.viewer.thumbnails?c},
				rotate: ${globalConfiguration.viewer.rotate?c},
				download: ${globalConfiguration.common.download?c},
                upload: ${globalConfiguration.common.upload?c},
                print: ${globalConfiguration.common.print?c},
                browse: ${globalConfiguration.common.browse?c},
                rewrite: ${globalConfiguration.common.rewrite?c}
            });
        </script>
    </body>
</html>