package vc908.stickerfactory;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import vc908.stickerfactory.interceptors.NetworkHeaderInterceptor;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.model.response.NetworkResponseModel;
import vc908.stickerfactory.model.response.StickersResponse;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.TypedJsonString;
import vc908.stickerfactory.utils.Utils;

/**
 * Class contains all network logic
 *
 * @author Dmitry Nezhydenko
 */
public final class NetworkManager {

	public static final String BASE_URL = "http://api.stickerpipe.com";
	// public static final String BASE_URL = "http://work.stk.908.vc";
	public static final String HEADER_LAST_MODIFY = "Last-Modified";

	public static final String PACK_TAB_ICON = "tab_icon";
	public static final String PACK_MAIN_ICON = "main_icon";

	private static final String TAG = NetworkManager.class.getSimpleName();
	private static NetworkManager instance;
	private final Context mContext;
	private final INetworkService mNetworkService;
	private OkHttpClient mOkHttpClient;
	private Gson gson;

	/**
	 * Private constructor to prevent creating new objects. Initialize
	 * {@link NetworkManager#mOkHttpClient }
	 *
	 * @param context
	 *            manager context
	 * @param apiKey
	 *            requests api key
	 */
	private NetworkManager(Context context, String apiKey) {
		mContext = context;
		mOkHttpClient = new OkHttpClient();
		mOkHttpClient.networkInterceptors()
				.add(new NetworkHeaderInterceptor(apiKey, Utils.getDeviceId(context), context.getPackageName()));
		initGson();
		mNetworkService = new RestAdapter.Builder().setClient(new OkClient(mOkHttpClient)).setEndpoint(BASE_URL + "/")
				.setConverter(new GsonConverter(gson)).setLogLevel(RestAdapter.LogLevel.FULL).build()
				.create(INetworkService.class);

	}

	/**
	 * Return singleton manager instance.
	 *
	 * @return manager instance
	 * @throws NetworkManagerNotInitializedException
	 *             when try to get instance before {@link #init} call
	 */
	public static synchronized NetworkManager getInstance() throws NetworkManagerNotInitializedException {
		if (instance == null) {
			throw new NetworkManagerNotInitializedException();
		} else {
			return instance;
		}
	}

	/**
	 * Initialize {@link Gson} parser
	 */
	private void initGson() {
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	}

	/**
	 * Create and initialize manager instance
	 *
	 * @param context
	 *            manager context
	 * @param apiKey
	 *            requests api key
	 * @throws NetworkManagerAlreadyInitializedException
	 *             when try to initialize already initialized instance
	 */
	public static void init(Context context, String apiKey) throws NetworkManagerAlreadyInitializedException {
		if (instance == null) {
			instance = new NetworkManager(context, apiKey);
		} else {
			throw new NetworkManagerAlreadyInitializedException();
		}
	}

	/**
	 * Request free stickers list and store them
	 */

	public void updateStickersList() {
		if (Utils.isNetworkAvailable(mContext)) {
			mNetworkService.getStickersList("free", new Callback<StickersResponse>() {

				@Override
				public void success(StickersResponse stickersResponse, Response responce) {
					List<String> storedPacks = StorageManager.getInstance().getPacksName();
					List<StickersPack> newPacks = new ArrayList<>();
					for (StickersPack pack : stickersResponse.getData()) {
						if (storedPacks.contains(pack.getName())) {
							storedPacks.remove(pack.getName());
						} else {
							newPacks.add(pack);
						}
					}
					StorageManager.getInstance().storeStickers(newPacks);
					// remove pack, which not get from serverside
					for (String storedPack : storedPacks) {
						StorageManager.getInstance().removePack(storedPack);
					}
				}

				@Override
				public void failure(RetrofitError error) {
					onNetworkResponseFail(error);

				}
			});
		}
	}

	/**
	 * Check last modified date of client packs
	 */
	public void checkPackUpdates() {
		if (Utils.isNetworkAvailable(mContext)) {
			mNetworkService.getStickersListHeaders("free", new retrofit.Callback<Void>() {
				@Override
				public void success(Void aVoid, retrofit.client.Response response) {
					for (Header header : response.getHeaders()) {
						if (HEADER_LAST_MODIFY.equals(header.getName())) {
							try {
								long lastModify = Long.parseLong(header.getValue()) * 1000;
								Logger.d(TAG, "Last modify date for packs: " + lastModify);
								if (lastModify > StorageManager.getInstance().getPacksLastModifyDate()) {
									Logger.d(TAG, "Packs is out of date.");
									updateStickersList();
								} else {
									Logger.d(TAG, "Packs is up to date");
								}
							} catch (NumberFormatException e) {
								Logger.e(TAG, "Invalid header Last modify: " + header.getValue(), e);
							}
							break;
						}
					}
				}

				@Override
				public void failure(RetrofitError error) {
					Logger.e(TAG, error.getMessage());
				}
			});
		}
	}

	/**
	 * Send analytics data to server side
	 *
	 * @param data
	 *            Data in JSON format
	 */
	public void sendAnalyticsData(JSONArray data) {
		mNetworkService.sendAnalytics(new TypedJsonString(data.toString()), new Callback<NetworkResponseModel>() {

			@Override
			public void success(NetworkResponseModel arg0, Response arg1) {
				StorageManager.getInstance().clearAnalytics();

			}

			@Override
			public void failure(RetrofitError error) {
				onNetworkResponseFail(error);

			}
		});
	}

	/**
	 * Handle network response error
	 *
	 * @param throwable
	 *            Network response error
	 */
	private void onNetworkResponseFail(Throwable throwable) {
		if (throwable instanceof RetrofitError) {
			RetrofitError retrofitError = (RetrofitError) throwable;
			String responseBody = "Response body is empty";
			if (retrofitError.getBody() != null) {
				responseBody = retrofitError.getBody().toString();
			}
			Logger.e(TAG, retrofitError.getUrl() + " : " + responseBody, throwable);
		} else {
			Logger.e(TAG, throwable.getMessage());
		}
	}

	/**
	 * Create sticker uri from given code
	 *
	 * @param code
	 *            Sticker code
	 * @return Uri to sticker
	 */
	public Uri getStickerUri(String code) {
		String url = String.format(BASE_URL + "/stk/%s/%s_%s.png", NamesHelper.getPackName(code),
				NamesHelper.getStickerName(code), Utils.getDensityName(mContext));
		return Uri.parse(url);
	}

	/**
	 * Create image icon uri from given name and pack name
	 *
	 * @param imageName
	 *            Image name
	 * @param packName
	 *            Pack name
	 * @return Uri to image
	 */
	public Uri geIconUri(String packName, String imageName) {
		String url = String.format(BASE_URL + "/stk/%s/%s_%s.png", packName, imageName, Utils.getDensityName(mContext));
		return Uri.parse(url);
	}

	/**
	 * Class represents initialize missing exception.
	 *
	 * @see NetworkManager#init
	 */
	public static class NetworkManagerNotInitializedException extends RuntimeException {
		@Override
		public String getMessage() {
			return "Network manager not initialized. Call init(...) before getting instance";
		}
	}

	/**
	 * Class represents double initialize exception.
	 *
	 * @see NetworkManager#init
	 */
	public static class NetworkManagerAlreadyInitializedException extends RuntimeException {
		@Override
		public String getMessage() {
			return "Network manager already initialized";
		}
	}
}
