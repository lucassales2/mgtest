package mg.weather.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Lucas Sales on 11/04/2017.
 */

@Database(version = WeatherDatabase.VERSION)
public class WeatherDatabase {
    public static final int VERSION = 1;

    @Table(WeatherColumns.class)
    public static final String WEATHER = "weather";


    private WeatherDatabase() {

    }
}