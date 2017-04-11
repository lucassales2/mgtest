package mg.weather.ui.viewmodel;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class CityWeatherViewModel {

    private final long id;
    private final String name;
    private final String iconUrl;
    private final String description;
    private final float temp;
    private final float maxTemp;
    private final float minTemp;
    private final Float distance;

    public CityWeatherViewModel(long id, String name, String iconUrl, String description, float temp, float maxTemp, float minTemp, float distance) {
        this.id = id;
        this.name = name;
        this.iconUrl = String.format("http://openweathermap.org/img/w/%s.png", iconUrl);
        this.description = description;
        this.temp = temp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.distance = distance;
    }

    public Float getDistance() {
        return distance;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDescription() {
        return description;
    }

    public float getTemp() {
        return temp;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public long getId() {
        return id;
    }
}
