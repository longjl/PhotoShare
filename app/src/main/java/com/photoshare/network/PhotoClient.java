package com.photoshare.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.photoshare.Constants;

/**
 * Created by longjianlin on 15/4/24.
 */
public class PhotoClient {
    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * 设置超时时间
     */
    static {
        client.setTimeout(Constants.CLIENT_TIMEOUT);
    }

    /**
     * GET 方式请求
     *
     * @param url             相对路径
     * @param params          请求参数
     * @param responseHandler 响应处理
     */
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    /**
     * POST 方式请求
     *
     * @param url             相对路径
     * @param params          请求参数
     * @param responseHandler 响应处理
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    /**
     * PUT 方式请求
     *
     * @param url             相对路径
     * @param params          请求参数
     * @param responseHandler 响应处理
     */
    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.put(url, params, responseHandler);
    }

}
