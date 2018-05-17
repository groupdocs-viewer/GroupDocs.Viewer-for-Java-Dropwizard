package com.groupdocs.ui.viewer.resources;

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Aspose Pty Ltd
 */
public abstract class ViewerResourcesBase {
    private final String DEFAULT_CHARSET = "UTF-8";

    protected void setResponseContentType(HttpServletResponse response, String contentType){
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding(DEFAULT_CHARSET);
            response.getOutputStream().flush();
        } catch (IOException ex) {
            Logger.getLogger(ViewrResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected String objectToJson(Object object){
        try {
            String auxJson = new Gson().toJson(object);
            return auxJson;
        } catch (Exception ex) {
            Logger.getLogger(ViewerResourcesBase.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected String getRequestBody(HttpServletRequest request){
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStream = request.getInputStream();
            if (inputStream != null) {
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if(inputStream != null) {
                    inputStream.close();
                }
                if(inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    protected String getJsonString(String json, String key){
        String value = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            value = jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    protected int getJsonInteger(String json, String key){
        int value = 1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            value = jsonObject.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    protected boolean getJsonBoolean(String json, String key){
        boolean value = true;
        try {
            JSONObject jsonObject = new JSONObject(json);
            value = jsonObject.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

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
