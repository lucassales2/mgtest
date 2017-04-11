package mg.weather.network.response;

import java.util.List;

import mg.weather.network.dto.CityDto;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class NearbyCitiesResponse {
    private String message;
    private int code;
    private int count;
    private List<CityDto> list;

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public int getCount() {
        return count;
    }

    public List<CityDto> getList() {
        return list;
    }


}
