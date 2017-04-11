package mg.weather.network.dto;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class CoordinatesDto {
    private double lat;
    private double lon;

    private CoordinatesDto(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
