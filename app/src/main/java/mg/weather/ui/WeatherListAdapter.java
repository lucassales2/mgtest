package mg.weather.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import mg.weather.R;
import mg.weather.Utility;
import mg.weather.ui.viewmodel.CityWeatherViewModel;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {

    private final List<CityWeatherViewModel> list;
    private boolean isMetric;

    public WeatherListAdapter(List<CityWeatherViewModel> list, boolean isMetric) {
        this.list = list;
        this.isMetric = isMetric;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CityWeatherViewModel viewModel = list.get(position);
        holder.textViewCityName.setText(viewModel.getName());
        holder.textViewWeatherDescription.setText(viewModel.getDescription());
        Double temp = isMetric ? viewModel.getTemp() : Utility.celsiusToFahrenheit(viewModel.getTemp());
        Double tempMax = isMetric ? viewModel.getMaxTemp() : Utility.celsiusToFahrenheit(viewModel.getMaxTemp());
        Double tempMin = isMetric ? viewModel.getMinTemp() : Utility.celsiusToFahrenheit(viewModel.getMinTemp());
        String tempString = String.format("%dº", temp.intValue());
        String tempMaxMinString = String.format("Min : %dº  Max %dº", tempMin.intValue(), tempMax.intValue());
        holder.textViewTemp.setText(tempString);
        holder.textViewTempMinMax.setText(tempMaxMinString);
        Glide.with(holder.itemView.getContext()).load(viewModel.getIconUrl()).into(holder.imageViewIcon);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setMetric(boolean metric) {
        if (isMetric != metric) {
            isMetric = !isMetric;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewCityName;
        ImageView imageViewIcon;
        TextView textViewWeatherDescription;
        TextView textViewTemp;
        TextView textViewTempMinMax;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewCityName = (TextView) itemView.findViewById(R.id.textView_cityName);
            imageViewIcon = (ImageView) itemView.findViewById(R.id.img_weather_icon);
            textViewWeatherDescription = (TextView) itemView.findViewById(R.id.text_weather_desc);
            textViewTemp = (TextView) itemView.findViewById(R.id.text_temp);
            textViewTempMinMax = (TextView) itemView.findViewById(R.id.text_temp_max_min);
        }
    }
}
