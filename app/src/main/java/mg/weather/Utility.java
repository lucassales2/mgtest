package mg.weather;

import android.location.Location;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class Utility {

    public static Double celsiusToFahrenheit(double temperatureInCelsius) {
        return (temperatureInCelsius * 1.8) + 32;
    }

    public static boolean isBetterLocation(Location location, Location currentBestLocation, long updateRate) {
        // A new location is always better than no location
        if (currentBestLocation == null) return true;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > updateRate;




        return isSignificantlyNewer ||
                location.distanceTo(currentBestLocation) >= 3000;

    }
}
