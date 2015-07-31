package alef.uchicago.edu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Robert on 7/25/2015.
 */
public class HttpDownUtil {

    private HttpURLConnection httpConnection;

    private InputStream inputStream;
    private String fileName;
    private int contentLength;

    public void downloadFile (String fileURL) throws IOException {
        URL url = new URL(fileURL);
        httpConnection = (HttpURLConnection) url.openConnection();
        int responseCode = httpConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConnection.getHeaderField("Content-Disposition");
            String contentType = httpConnection.getContentType();
            contentLength = httpConnection.getContentLength();

            if (disposition != null) {
                int index = disposition.indexOf("filename=");
                if(index > 0){
                    fileName = disposition.substring(index + 10, disposition.length() -1);
                }
            }
            else {
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            inputStream = httpConnection.getInputStream();
        }
        else {
            throw new IOException( "No files to download. Server replied: " + responseCode);
        }
    }

    public void disconnect() throws IOException {
        inputStream.close();
        httpConnection.disconnect();
    }

    public String getFileName() {return this.fileName; }

    public int getContentLength() {return  this.contentLength; }

    public InputStream getInputStream() {return this.inputStream; }
}
