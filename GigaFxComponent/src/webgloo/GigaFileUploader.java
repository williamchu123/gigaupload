package webgloo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rajeev jha (jha.rajeev@gmail.com)
 *
 * @see also http://javafx.com/samples/ScreenshotMaker/src/Flickr.java.html
 *
 * This class is based on above javaFx Flickr Post sample.
 *
 * Following changes have been done to original code to suit our need
 *
 * 
 * 1. FileMetaData support
 * 2. Different error handling
 * 3. Fix for spaces in file names
 * 
 */
public class GigaFileUploader {

    public static class Content {

        byte[] content;
        String contentType;
    }

    public static class FileMetaData {

        String name;
        String mime;
        String uuid;

        public FileMetaData(String uuid, String name, String mime) {
            this.name = name;
            this.mime = mime;
            this.uuid = uuid;

        }
    }

    public static FileMetaData init(java.io.File f) {
        String name = f.getName();
        //remove spaces from file name
        name = name.replace(' ', '-');
        //Add random part
        String uuid = UUID.randomUUID().toString();
        String mime = new MimetypesFileTypeMap().getContentType(f);
        return new FileMetaData(uuid, name, mime);

    }

    public static Content createPostContent(
            FileMetaData fileMetaData,
            byte[] bytes,
            int chunkCount,
            int totalChunks) throws Exception {

        List<MimeBodyPart> parts = new ArrayList<MimeBodyPart>();

        // Add current chunk
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setDisposition("form-data; name=\"chunk\"");
        part1.setText("" + chunkCount);
        parts.add(part1);

        // Add total chunks
        MimeBodyPart part2 = new MimeBodyPart();
        part2.setDisposition("form-data; name=\"total_chunks\"");
        part2.setText("" + totalChunks);
        parts.add(part2);

        // Add uuid
        MimeBodyPart part3 = new MimeBodyPart();
        part3.setDisposition("form-data; name=\"uuid\"");
        part3.setText(fileMetaData.uuid);
        parts.add(part3);


        // Add bytes from file
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("Content-Type", fileMetaData.mime);
        MimeBodyPart fileContentPart = new MimeBodyPart(headers, bytes);

        fileContentPart.setDisposition("form-data; name=\"f\"; filename=" + fileMetaData.name);
        parts.add(fileContentPart);


        // Create the multipart
        MimeMultipart multiPart = new MimeMultipart("form-data");

        for (MimeBodyPart p : parts) {
            multiPart.addBodyPart(p);

        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        multiPart.writeTo(stream);

        Content c = new Content();
        c.content = stream.toByteArray();

        c.contentType = multiPart.getContentType();
        c.contentType = c.contentType.replace("\r\n", "");

        return c;
    }

    public static byte[] getBufferBytes(byte[] srcArray, int limit, int postMax) {

        if (limit < postMax) {
            byte[] buffer = new byte[limit];
            //copy portion of source array
            System.arraycopy(srcArray, 0, buffer, 0, limit);
            return buffer;

        } else {
            //full buffer, return source 
            return srcArray;
        }

    }

    public static void setFiddlerProxy() {
        // reverse proxy settings for fiddler
        // @see http://www.fiddlertool.com/fiddler/help/reverseproxy.asp
        java.util.Properties sysProperties = System.getProperties();
        sysProperties.put("proxyHost", "127.0.0.1");
        sysProperties.put("proxyPort", "8888");
        sysProperties.put("proxySet", "true");

    }

    public static String upload(
            String uploadURI,
            GigaFileUploader.Content c) throws Exception {


        //@todo - read from a properties file
        // Turn on this proxy to enable fiddler debugging
        boolean isDebugInFiddler = false;
        if (isDebugInFiddler) {
            setFiddlerProxy();

        }

        // Create Connection
        HttpURLConnection connection = (HttpURLConnection) new URL(uploadURI).openConnection();
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.addRequestProperty("Content-Type", c.contentType);

        connection.addRequestProperty("Content-Length", String.valueOf(c.content.length));
        connection.getOutputStream().write(c.content);
        connection.getOutputStream().close();

        //@todo - fix error handling
        //@todo can JavaFx log to some local file, say in windows TEMP location?

        int code = connection.getResponseCode();
        String message = null;

        if (code > 300) {

            //404 - separate handling
            if (code == 404) {
                throw new Exception("Http 404 : Invalid upload location");
            }

            BufferedReader err = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()));
            String ret;
            StringBuffer buff = new StringBuffer();
            while ((ret = err.readLine()) != null) {
                buff.append(ret);
            }

            throw new IOException(buff.toString());

        } else {

            BufferedReader inp = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuffer buff = new StringBuffer();
            String ret;
            while ((ret = inp.readLine()) != null) {
                buff.append(ret);
            }
            message = buff.toString();

            if (message.contains("error")) {
                throw new IOException("Upload script execution error!");
            }

        }

        return message;

    }
}
