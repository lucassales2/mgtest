package mg.weather.data;

import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKeyConstraint;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.REAL;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by Lucas Sales on 11/04/2017.
 */

@PrimaryKeyConstraint(
        columns = {WeatherColumns._ID},
        name = WeatherDatabase.WEATHER,
        onConflict = ConflictResolutionType.REPLACE
)
public interface WeatherColumns {
    @DataType(INTEGER)
    @NotNull
    String _ID = "_id";

    @DataType(TEXT)
    @NotNull
    String NAME = "name";

    @DataType(TEXT)
    @NotNull
    String ICON_URL = "iconUrl";

    @DataType(TEXT)
    @NotNull
    String DESCRIPTION = "description";

    @DataType(REAL)
    @NotNull
    String TEMP = "temp";

    @DataType(REAL)
    @NotNull
    String MAX_TEMP = "maxTemp";

    @DataType(REAL)
    @NotNull
    String MIN_TEMP = "minTemp";

    @DataType(TEXT)
    @NotNull
    String LATITUDE = "latitude";

    @DataType(TEXT)
    @NotNull
    String LONGITUDE = "longitude";

    @DataType(INTEGER)
    @NotNull
    String LAST_UPDATE = "lastUpdate";


}
