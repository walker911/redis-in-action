package com.walker.redis.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Connection Timeout: the time to establish the connection with the remote host.
 * Socket Timeout: the time waiting for data - after the connection was established;
 * maximum time of inactivity between two data packets.
 * Connection Manager Timeout: the time to wait for a connection from the connection manager/pool.
 *
 * @author walker
 * @date 2018/11/19
 */
public class HttpClientUtil {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final int CONNECT_TIMEOUT = 5000;

    private static final int SOCKET_TIMEOUT = 5000;

    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;

    private static RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setConnectionRequestTimeout(SOCKET_TIMEOUT)
            .setSocketTimeout(CONNECTION_REQUEST_TIMEOUT)
            .build();

    private HttpClientUtil() {
    }

    /**
     * form post
     *
     * @param url    地址
     * @return result
     * @throws IOException IOException
     */
    public static String doPost(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpPost httpPost = new HttpPost(url);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
            }
        }
    }

    public static void main(String[] args) throws IOException {
    }
}
