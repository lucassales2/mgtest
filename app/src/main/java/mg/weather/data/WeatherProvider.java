package mg.weather.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Lucas Sales on 11/04/2017.
 */

@ContentProvider(authority = WeatherProvider.AUTHORITY, database = WeatherDatabase.class)
public class WeatherProvider {
    public static final String AUTHORITY = "mg.weather.data.WeatherProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String WEATHER = "weather";
    }

    @TableEndpoint(table = WeatherDatabase.WEATHER)
    public static class Weather {
        @ContentUri(
                path = Path.WEATHER,
                type = "vnd.android.cursor.dir/weather"
        )
        public static final Uri CONTENT_URI = buildUri(Path.WEATHER);

        @InexactContentUri(
                name = "WEATHER_ID",
                path = Path.WEATHER + "/*",
                type = "vnd.android.cursor.item/weather",
                whereColumn = WeatherColumns._ID,
                pathSegment = 1
        )
        public static Uri withSymbol(long id) {
            return buildUri(Path.WEATHER, String.valueOf(id));
        }
    }
}
