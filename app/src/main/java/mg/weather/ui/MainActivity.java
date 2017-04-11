package mg.weather.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mg.weather.R;
import mg.weather.Utility;
import mg.weather.network.GetWeatherServiceApi;
import mg.weather.network.ServiceGenerator;
import mg.weather.network.dto.CityDto;
import mg.weather.network.response.NearbyCitiesResponse;
import mg.weather.ui.viewmodel.CityWeatherViewModel;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnCameraMoveListener {

    private static final String ARG_LIST = "list";
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<CityWeatherViewModel> cityWeatherViewModels;
    private Map<Long, Marker> markerMap = new HashMap<>();
    private Location currentLocation;
    private Location mapLocation;
    private SupportMapFragment mapFragment;
    private GoogleMap mGoogleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null) {
            cityWeatherViewModels = savedInstanceState.getParcelableArrayList(ARG_LIST);
            if (getSupportFragmentManager().findFragmentByTag(WeatherListFragment.TAG) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.list_fragment, WeatherListFragment.newInstance(cityWeatherViewModels), WeatherListFragment.TAG)
                        .commit();
            }

        } else {
            getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
        }


        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (!granted) {
                            finish();
                        }
                    }
                });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.spref_metric), true)) {
            menu.getItem(0).setTitle(R.string.change_unit_fahrenheit);
        } else {
            menu.getItem(0).setTitle(R.string.change_unit_celcius);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_change_unit:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean isMetric = !sharedPreferences.getBoolean(getString(R.string.spref_metric), true);
                if (isMetric) {
                    item.setTitle(R.string.change_unit_fahrenheit);
                } else {
                    item.setTitle(R.string.change_unit_celcius);
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.spref_metric), isMetric).apply();
                break;

            case R.id.item_change_view:
                Fragment listFragment = getSupportFragmentManager().findFragmentByTag(WeatherListFragment.TAG);
                if (listFragment != null) {
                    if (mapFragment.isHidden()) {
                        item.setIcon(R.drawable.ic_list);
                        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                        getSupportFragmentManager().beginTransaction().hide(listFragment).commit();
                    } else {
                        item.setIcon(R.drawable.ic_map);
                        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                        getSupportFragmentManager().beginTransaction().show(listFragment).commit();
                    }
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_LIST, cityWeatherViewModels);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (cityWeatherViewModels != null ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (currentLocation != null) {
            getWeatherList(currentLocation);
        }

    }

    private void getWeatherList(Location location) {
        if (location != null) {
             ServiceGenerator.getApiService(GetWeatherServiceApi.class)
                    .getNearbyCitiesWeather(location.getLatitude(), location.getLongitude(), 50)
                    .map(new Function<NearbyCitiesResponse, ArrayList<CityWeatherViewModel>>() {
                        @Override
                        public ArrayList<CityWeatherViewModel> apply(NearbyCitiesResponse nearbyCitiesResponse) throws Exception {
                            ArrayList<CityWeatherViewModel> list = new ArrayList<>();
                            for (CityDto cityDto : nearbyCitiesResponse.getList()) {
                                list.add(new CityWeatherViewModel(cityDto));
                            }
                            return list;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<ArrayList<CityWeatherViewModel>>() {
                        @Override
                        public void accept(ArrayList<CityWeatherViewModel> cityWeatherViewModels) throws Exception {
                            if (getSupportFragmentManager().findFragmentByTag(WeatherListFragment.TAG) == null) {
                                MainActivity.this.cityWeatherViewModels = cityWeatherViewModels;
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.list_fragment, WeatherListFragment.newInstance(cityWeatherViewModels), WeatherListFragment.TAG)
                                        .commit();
                            }
                            updateMap(cityWeatherViewModels);
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        if (location != null) {
            mGoogleMap.setMyLocationEnabled(true);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
            mGoogleMap.moveCamera(cameraUpdate);
        }
        mGoogleMap.setOnCameraMoveListener(this);
        mGoogleMap.setMaxZoomPreference(10);
        mGoogleMap.setMinZoomPreference(10);
        if (cityWeatherViewModels != null)
            updateMap(cityWeatherViewModels);

    }

    private void updateMap(List<CityWeatherViewModel> cityWeatherViewModels) {

        mGoogleMap.setInfoWindowAdapter(this);
        for (CityWeatherViewModel cityWeatherViewModel : cityWeatherViewModels) {
            if (markerMap.containsKey(cityWeatherViewModel.getId())) {
                markerMap.get(cityWeatherViewModel.getId()).setTag(cityWeatherViewModel);
            } else {
                LatLng latLng = new LatLng(cityWeatherViewModel.getLatitude(), cityWeatherViewModel.getLongitude());
                Marker marker = mGoogleMap
                        .addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(cityWeatherViewModel.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker()));
                marker.setTag(cityWeatherViewModel);
                markerMap.put(cityWeatherViewModel.getId(), marker);
            }
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null, false);
        CityWeatherViewModel viewModel = (CityWeatherViewModel) marker.getTag();
        TextView currentMarkerTextView = (TextView) view.findViewById(R.id.text_marker_temp);
        boolean aBoolean = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.spref_metric), true);
        Double temp = aBoolean ? viewModel.getTemp() : Utility.celsiusToFahrenheit(viewModel.getTemp());
        currentMarkerTextView.setText(String.format("%dº", temp.intValue()));
        ImageView imageView = (ImageView) view.findViewById(R.id.img_marker);
        Glide.with(this).load(viewModel.getIconUrl()).into(imageView);
        return view;
    }

    @Override
    public void onCameraMove() {
        Location cameraLocation = new Location("");
        LatLng target = mGoogleMap.getCameraPosition().target;
        cameraLocation.setLatitude(target.latitude);
        cameraLocation.setLongitude(target.longitude);
        if (Utility.isBetterLocation(cameraLocation, mapLocation, 180000)) {
            mapLocation = cameraLocation;
            getWeatherList(mapLocation);
        }


    }
}
