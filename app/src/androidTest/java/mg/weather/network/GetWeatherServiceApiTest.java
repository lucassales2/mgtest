package mg.weather.network;

import org.junit.Test;

import io.reactivex.observers.TestObserver;
import mg.weather.network.response.NearbyCitiesResponse;

/**
 * Created by Lucas Sales on 10/04/2017.
 */
public class GetWeatherServiceApiTest {
    @Test
    public void getNearbyCitiesWeather() throws Exception {
        GetWeatherServiceApi api = ServiceGenerator.getApiService(GetWeatherServiceApi.class);
        TestObserver<NearbyCitiesResponse> testObserver = api.getNearbyCitiesWeather(-23.6441229, -46.5403357, 50).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertComplete();

    }

}