package mg.weather.network;

import io.reactivex.Single;
import mg.weather.network.response.NearbyCitiesResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public interface GetWeatherServiceApi {
    @GET("find")
    Single<NearbyCitiesResponse> getNearbyCitiesWeather(@Query("lat") double lat, @Query("lon") double lon, @Query("cnt") int cnt);
}
