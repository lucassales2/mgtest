package mg.weather.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import mg.weather.R;
import mg.weather.ui.viewmodel.CityWeatherViewModel;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class WeatherListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = WeatherListFragment.class.getSimpleName();

    private static final String ARG_LIST = "list";
    private SharedPreferences sharedPreferences;
    private WeatherListAdapter listAdapter;

    public static WeatherListFragment newInstance(ArrayList<CityWeatherViewModel> list) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_LIST, list);
        WeatherListFragment fragment = new WeatherListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        ArrayList<CityWeatherViewModel> list = getArguments().getParcelableArrayList(ARG_LIST);
        listAdapter = new WeatherListAdapter(list, sharedPreferences.getBoolean(getString(R.string.spref_metric), true));
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.spref_metric))) {
            listAdapter.setMetric(sharedPreferences.getBoolean(getString(R.string.spref_metric), true));
        }
    }
}
