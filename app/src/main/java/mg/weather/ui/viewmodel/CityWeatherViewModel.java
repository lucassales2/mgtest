package mg.weather.ui.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;

import mg.weather.network.dto.CityDto;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class CityWeatherViewModel implements Parcelable {

    private final long id;
    private final String name;
    private final String iconUrl;
    private final String description;
    private final float temp;
    private final float maxTemp;
    private final float minTemp;
    private final double latitude;
    private final double longitude;

    public CityWeatherViewModel(CityDto model) {

        this.id = model.getId();
        this.name = model.getName();
        this.iconUrl = String.format("http://openweathermap.org/img/w/%s.png", model.getWeather().get(0).getIcon());
        String description = model.getWeather().get(0).getDescription();
        this.description = description.substring(0, 1).toUpperCase() + description.substring(1);
        this.temp = model.getMain().getTemp();
        this.maxTemp = model.getMain().getTemp_max();
        this.minTemp = model.getMain().getTemp_min();
        this.latitude = model.getCoord().getLat();
        this.longitude = model.getCoord().getLon();

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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.iconUrl);
        dest.writeString(this.description);
        dest.writeFloat(this.temp);
        dest.writeFloat(this.maxTemp);
        dest.writeFloat(this.minTemp);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected CityWeatherViewModel(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.iconUrl = in.readString();
        this.description = in.readString();
        this.temp = in.readFloat();
        this.maxTemp = in.readFloat();
        this.minTemp = in.readFloat();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Creator<CityWeatherViewModel> CREATOR = new Creator<CityWeatherViewModel>() {
        @Override
        public CityWeatherViewModel createFromParcel(Parcel source) {
            return new CityWeatherViewModel(source);
        }

        @Override
        public CityWeatherViewModel[] newArray(int size) {
            return new CityWeatherViewModel[size];
        }
    };

    public long getId() {
        return id;
    }
}
