package mg.weather.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import mg.weather.AppController;
import mg.weather.AppControllerListener;
import mg.weather.R;
import mg.weather.Utility;
import mg.weather.data.WeatherColumns;
import mg.weather.data.WeatherProvider;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class WeatherMapFragment extends Fragment implements
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnCameraMoveListener {

    public static final String TAG = WeatherMapFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;
    private static final String ARG_LOCATION = "location";


    private SharedPreferences sharedPreferences;
    private GoogleMap mGoogleMap;
    private LongSparseArray<Marker> markerMap = new LongSparseArray<>();
    private Location mapLocation;
    private Cursor cursor;
    private boolean toastShown = false;

    public static WeatherMapFragment newInstance(Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_LOCATION, location);
        WeatherMapFragment fragment = new WeatherMapFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather_map, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        mGoogleMap.setInfoWindowAdapter(this);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = getArguments().getParcelable(ARG_LOCATION);
        mGoogleMap.setMyLocationEnabled(true);
        if (location != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
            mGoogleMap.moveCamera(cameraUpdate);
        }
        mGoogleMap.setOnCameraMoveListener(this);
        mGoogleMap.setMaxZoomPreference(10);
        mGoogleMap.setMinZoomPreference(10);
        updateMap();

    }

    private void updateMap() {
        if (mGoogleMap != null && cursor != null && cursor.moveToFirst()) {
            do {

                InfoHolder holder = new InfoHolder(cursor.getLong(0), cursor.getFloat(1), Double.valueOf(cursor.getString(2)), Double.valueOf(cursor.getString(3)), cursor.getString(4), cursor.getString(5));
                if (markerMap.get(holder.getId()) == null) {
                    Marker marker = mGoogleMap
                            .addMarker(new MarkerOptions()
                                    .position(holder.getLatLng())
                                    .title(holder.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker()));

                    markerMap.put(holder.getId(), marker);
                }
                markerMap.get(holder.getId()).setTag(holder);

            } while (cursor.moveToNext());

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                WeatherProvider.Weather.CONTENT_URI,
                new String[]{WeatherColumns._ID, WeatherColumns.TEMP, WeatherColumns.LATITUDE, WeatherColumns.LONGITUDE, WeatherColumns.NAME, WeatherColumns.ICON_URL}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            cursor = data;
            updateMap();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor = null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.marker_layout, null, false);
        InfoHolder infoHolder = (InfoHolder) marker.getTag();
        TextView currentMarkerTextView = (TextView) view.findViewById(R.id.text_marker_temp);
        boolean aBoolean = sharedPreferences.getBoolean(getString(R.string.spref_metric), true);
        Double temp = aBoolean ? infoHolder.getTemperature() : Utility.celsiusToFahrenheit(infoHolder.getTemperature());
        currentMarkerTextView.setText(String.format("%dº", temp.intValue()));
        ImageView imageView = (ImageView) view.findViewById(R.id.img_marker);
        Glide.with(this).load(infoHolder.getIconUrl()).into(imageView);
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
            AppController.getInstance().getWeatherList(getContext(), mapLocation, 30, new AppControllerListener() {
                @Override
                public void onError(Throwable t) {
                    Observable.just(t).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (!toastShown) {
                                Toast.makeText(getContext(), R.string.connection_error_message, Toast.LENGTH_SHORT).show();
                                toastShown = true;
                            }
                        }
                    });
                }
            });
        }
    }


    private class InfoHolder {
        private final long id;
        private final float temperature;
        private final LatLng latLng;
        private final String name;
        private final String iconUrl;

        InfoHolder(long id, float temperature, double latitude, double longitude, String name, String iconUrl) {
            this.id = id;
            this.temperature = temperature;
            this.name = name;
            this.iconUrl = String.format("http://openweathermap.org/img/w/%s.png", iconUrl);
            latLng = new LatLng(latitude, longitude);
        }

        String getName() {
            return name;
        }

        long getId() {
            return id;
        }

        float getTemperature() {
            return temperature;
        }

        LatLng getLatLng() {
            return latLng;
        }

        String getIconUrl() {
            return iconUrl;
        }
    }
}
