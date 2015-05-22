package com.blogspot.alkrinker.httpclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;

/**
 *
 * @author alkrinker
 */
public class Client {

    static final Logger logger = Logger.getLogger(Client.class);

    /**
     * Sends an HTTP GET request to a url
     *
     * @param endpoint - The URL of the server.
     * @param requestParameters - all the request parameters (Example:
     * "param1=val1&param2=val2").
     *
     * @return - The response from the end point
     */
    public static String sendGetRequest(String endpoint, String requestParameters) {
        String result = null;
        if (endpoint.startsWith("http://")) {
            // Send a GET request to the servlet
            try {
                // Send data
                String urlStr = endpoint;
                if (requestParameters != null && requestParameters.length() > 0) {
                    urlStr += "?" + requestParameters;
                }

                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection();

                StringBuilder sb;
                // Get the response
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    sb = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                }
                result = sb.toString();
            } catch (MalformedURLException ex) {

            } catch (Exception e) {
                logger.error("Error occured during GetRequest operation. " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Deletes via DELETE request.
     *
     * @param endpoint - The server's address
     *
     */
    public static void deleteData(URL endpoint) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) endpoint.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestMethod("DELETE");
            logger.info("HTTP URL Connection response " + httpURLConnection.getResponseCode());
        } catch (IOException exception) {
            logger.error("Error occured " + exception.getMessage());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    /**
     * Puts provided data to a server via PUT request.
     *
     * @param endpoint - The server's address
     * @param data - Data to put
     *
     */
    public static void putData(URL endpoint, String data) {
        HttpURLConnection httpURLConnection = null;
        DataOutputStream dataOutputStream = null;
        try {
            httpURLConnection = (HttpURLConnection) endpoint.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(data.getBytes());
        } catch (IOException exception) {
            logger.error("Error occured " + exception.getMessage());
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.flush();
                    dataOutputStream.close();
                } catch (IOException exception) {
                    logger.error("Error occured durin dataOutputStream close " + exception.getMessage());
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    /**
     * Reads data from the data reader and posts it to a server via POST
     * request.
     *
     * @param data - The data you want to send
     * @param endpoint - The server's address
     * @param output - writes the server's response to output
     *
     * @throws Exception
     */
    public static void postData(Reader data, URL endpoint, Writer output) throws Exception {
        HttpURLConnection urlc = null;
        try {
            urlc = (HttpURLConnection) endpoint.openConnection();
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
            }
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");

            OutputStream out = urlc.getOutputStream();

            try (Writer writer = new OutputStreamWriter(out, "UTF-8")) {
                pipe(data, writer);
                writer.close();
            } catch (IOException e) {
                throw new Exception("IOException while posting data", e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            InputStream in = urlc.getInputStream();
            try (Reader reader = new InputStreamReader(in)) {
                pipe(reader, output);
                reader.close();
            } catch (IOException e) {
                throw new Exception("IOException while reading response", e);
            } finally {
                if (in != null) {
                    in.close();
                }
            }

        } catch (IOException e) {
            throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }

}
