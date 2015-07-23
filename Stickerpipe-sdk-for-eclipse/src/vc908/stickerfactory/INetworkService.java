package vc908.stickerfactory;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.HEAD;
import retrofit.http.POST;
import retrofit.http.Query;
import vc908.stickerfactory.model.response.NetworkResponseModel;
import vc908.stickerfactory.model.response.StickersResponse;
import vc908.stickerfactory.utils.TypedJsonString;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public interface INetworkService {
	@GET("/api/v1/client-packs")
	void getStickersList(@Query("type") String type, Callback<StickersResponse> calback);

	@POST("/api/v1/track-statistic")
	void sendAnalytics(@Body TypedJsonString body, Callback<NetworkResponseModel> callback);

	@HEAD("/api/v1/client-packs")
	void getStickersListHeaders(@Query("type") String type, Callback<Void> callback);
}
