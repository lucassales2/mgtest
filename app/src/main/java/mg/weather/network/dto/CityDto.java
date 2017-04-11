package mg.weather.network.dto;

import java.util.List;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class CityDto {

    private long id;
    private String name;
    private CoordinatesDto coord;
    private MainDto main;
    private List<WeatherDescriptionDto> weather;


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CoordinatesDto getCoord() {
        return coord;
    }

    public MainDto getMain() {
        return main;
    }

    public List<WeatherDescriptionDto> getWeather() {
        return weather;
    }

}
