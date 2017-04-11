package mg.weather.network.dto;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class WeatherDescriptionDto {
    private int id;
    private String main;
    private String description;
    private String icon;

    public int getId() {
        return id;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
