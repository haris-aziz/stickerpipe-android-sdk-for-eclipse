package vc908.stickerfactory.interceptors;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import vc908.stickerfactory.Constants;

/**
 * Interceptor for network requests.
 * Use for add custom headers to requests
 *
 * @author Dmitry Nezhydenko
 * @see Interceptor
 */
public class NetworkHeaderInterceptor implements Interceptor {

    private String apiKey;
    private String deviceId;
    private String packageName;

    public NetworkHeaderInterceptor(String apiKey, String deviceId, String packageName) {
        this.apiKey = apiKey;
        this.deviceId = deviceId;
        this.packageName = packageName;
    }

    /**
     * Intercept network request chain and add custom headers
     *
     * @param chain Network request chain
     * @return Intercepted request
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request requestWithHeaders = request.newBuilder()
                .addHeader("Platform", Constants.PLATFORM)
                .addHeader("ApiKey", apiKey)
                .addHeader("Package", packageName)
                .addHeader("DeviceId", deviceId)
                .build();
        return chain.proceed(requestWithHeaders);
    }
}