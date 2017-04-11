package mg.weather.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;

import mg.weather.ui.viewmodel.CityWeatherViewModel;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class WeatherMapFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = WeatherMapFragment.class.getSimpleName();

    private static final String ARG_LIST = "list";
    private SharedPreferences sharedPreferences;

    public static WeatherMapFragment newInstance(ArrayList<CityWeatherViewModel> list) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_LIST, list);
        WeatherMapFragment fragment = new WeatherMapFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
