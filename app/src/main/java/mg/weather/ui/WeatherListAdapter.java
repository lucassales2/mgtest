package mg.weather.ui;

import android.database.Cursor;
import android.location.Location;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import mg.weather.R;
import mg.weather.Utility;
import mg.weather.data.WeatherColumns;
import mg.weather.ui.viewmodel.CityWeatherViewModel;

/**
 * Created by Lucas Sales on 10/04/2017.
 */

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {

    private final SortedList<CityWeatherViewModel> mSortedList;
    private final Location mUserLocation;
    private boolean isMetric;

    public WeatherListAdapter(Location userLocation, boolean isMetric) {
        this.mUserLocation = userLocation;
        this.mSortedList = new SortedList<>(CityWeatherViewModel.class, new SortedList.Callback<CityWeatherViewModel>() {
            @Override
            public int compare(CityWeatherViewModel o1, CityWeatherViewModel o2) {
                return o1.getDistance().compareTo(o2.getDistance());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(CityWeatherViewModel oldItem, CityWeatherViewModel newItem) {
                return oldItem.getId() == newItem.getId() && oldItem.getTemp() == newItem.getTemp();
            }

            @Override
            public boolean areItemsTheSame(CityWeatherViewModel item1, CityWeatherViewModel item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });

        this.isMetric = isMetric;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CityWeatherViewModel viewModel = mSortedList.get(position);
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
        return mSortedList.size();
    }

    public void setMetric(boolean metric) {
        if (isMetric != metric) {
            isMetric = !isMetric;
            notifyDataSetChanged();
        }
    }

    public void update(Cursor data) {
        if (data.moveToFirst()) {
            do {
                Location location = new Location("");
                Double latitude = Double.valueOf(data.getString(data.getColumnIndex(WeatherColumns.LATITUDE)));
                Double longitude = Double.valueOf(data.getString(data.getColumnIndex(WeatherColumns.LONGITUDE)));
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                Float distance = location.distanceTo(mUserLocation);
                CityWeatherViewModel viewModel = new CityWeatherViewModel(
                        data.getLong(data.getColumnIndex(WeatherColumns._ID)),
                        data.getString(data.getColumnIndex(WeatherColumns.NAME)),
                        data.getString(data.getColumnIndex(WeatherColumns.ICON_URL)),
                        data.getString(data.getColumnIndex(WeatherColumns.DESCRIPTION)),
                        data.getFloat(data.getColumnIndex(WeatherColumns.TEMP)),
                        data.getFloat(data.getColumnIndex(WeatherColumns.MAX_TEMP)),
                        data.getFloat(data.getColumnIndex(WeatherColumns.MIN_TEMP)),
                        distance
                );
                mSortedList.add(viewModel);

            } while (data.moveToNext());
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
