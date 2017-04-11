package mg.weather;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.List;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mg.weather.data.WeatherColumns;
import mg.weather.data.WeatherProvider;
import mg.weather.network.GetWeatherServiceApi;
import mg.weather.network.ServiceGenerator;
import mg.weather.network.dto.CityDto;
import mg.weather.network.response.NearbyCitiesResponse;

/**
 * Created by Lucas Sales on 11/04/2017.
 */

public class AppController {
    private static AppController instance;

    private AppController() {

    }

    public static AppController getInstance() {
        if (instance == null) {
            instance = new AppController();
        }
        return instance;
    }

    public void getWeatherList(final Context context, Location location, int count, final AppControllerListener listener) {
        if (location != null) {
            Log.d("AppController", "New Request");
            ServiceGenerator.getApiService(GetWeatherServiceApi.class)
                    .getNearbyCitiesWeather(location.getLatitude(), location.getLongitude(), count)
                    .map(new Function<NearbyCitiesResponse, ContentValues[]>() {
                        @Override
                        public ContentValues[] apply(NearbyCitiesResponse nearbyCitiesResponse) throws Exception {
                            ContentValues[] values = new ContentValues[nearbyCitiesResponse.getList().size()];
                            String currentTimeMillis = String.valueOf(System.currentTimeMillis());
                            List<CityDto> list1 = nearbyCitiesResponse.getList();
                            for (int i = 0; i < list1.size(); i++) {
                                CityDto cityDto = list1.get(i);
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(WeatherColumns._ID, cityDto.getId());
                                contentValues.put(WeatherColumns.DESCRIPTION, cityDto.getWeather().get(0).getDescription());
                                contentValues.put(WeatherColumns.ICON_URL, cityDto.getWeather().get(0).getIcon());
                                contentValues.put(WeatherColumns.NAME, cityDto.getName());
                                contentValues.put(WeatherColumns.TEMP, cityDto.getMain().getTemp());
                                contentValues.put(WeatherColumns.MAX_TEMP, cityDto.getMain().getTemp_max());
                                contentValues.put(WeatherColumns.MIN_TEMP, cityDto.getMain().getTemp_min());
                                contentValues.put(WeatherColumns.LATITUDE, String.valueOf(cityDto.getCoord().getLat()));
                                contentValues.put(WeatherColumns.LONGITUDE, String.valueOf(cityDto.getCoord().getLon()));
                                contentValues.put(WeatherColumns.LAST_UPDATE, currentTimeMillis);
                                values[i] = contentValues;
                            }

                            return values;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new BiConsumer<ContentValues[], Throwable>() {
                        @Override
                        public void accept(ContentValues[] contentValues, Throwable throwable) throws Exception {
                            if (throwable == null) {
                                context.getContentResolver().bulkInsert(WeatherProvider.Weather.CONTENT_URI, contentValues);
                            } else {
                                listener.onError(throwable);
                            }
                        }
                    });
        }
    }
}
