package mg.weather.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import mg.weather.AppController;
import mg.weather.AppControllerListener;
import mg.weather.R;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean fragmentsInitiated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean granted) throws Exception {
                            if (!granted) {
                                finish();
                            } else {
                                connectFragments();
                            }
                        }
                    });
        }
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
                Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(WeatherMapFragment.TAG);
                if (listFragment != null && mapFragment != null) {
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
    public void onConnected(@Nullable Bundle bundle) {
        connectFragments();
    }

    private void connectFragments() {
        if (fragmentsInitiated || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        fragmentsInitiated = true;

        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (getSupportFragmentManager().findFragmentByTag(WeatherListFragment.TAG) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_fragment, WeatherListFragment.newInstance(currentLocation), WeatherListFragment.TAG)
                    .commit();

        }
        if (getSupportFragmentManager().findFragmentByTag(WeatherMapFragment.TAG) == null) {
            WeatherMapFragment mapFragment = WeatherMapFragment.newInstance(currentLocation);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_fragment, mapFragment, WeatherMapFragment.TAG)
                    .commit();
            getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
        }
        AppController.getInstance().getWeatherList(getApplicationContext(), currentLocation, 50, new AppControllerListener() {
            @Override
            public void onError(Throwable t) {
                Observable.just(t).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, R.string.connection_error_message, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
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
}
