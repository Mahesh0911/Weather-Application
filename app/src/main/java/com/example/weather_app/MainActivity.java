package com.example.weather_app;

import static android.media.CamcorderProfile.get;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/*
private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
private static final String API_KEY = "d3c8c11f22a6c8f2c156965e798fb582";
*/

public class MainActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    private TextView cityLocation;
    private TextView weatherCondition;
    private TextView dayDisplay;
    private TextView tempDegree;
    private TextView tempMin;
    private TextView tempMax;
    private TextView Day;
    private TextView Date;
    private TextView humidity;
    private TextView windSpeed;
    private TextView condition;
    private TextView sunrise;
    private TextView sunset;
    private TextView seaLevel;
    private View SearchBarView;
    private ConstraintLayout mainActivity;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchBarView = findViewById(R.id.searchBarView);
        cityLocation = findViewById(R.id.cityLocation);
        weatherCondition = findViewById(R.id.weatherCondition);
        dayDisplay = findViewById(R.id.dayDisplay);
        tempDegree = findViewById(R.id.tempDegree);
        tempMin = findViewById(R.id.minTemp);
        tempMax = findViewById(R.id.maxTemp);
        Day = findViewById(R.id.Day);
        Date = findViewById(R.id.Date);
        humidity = findViewById(R.id.humidity);
        windSpeed = findViewById(R.id.windSpeed);
        condition = findViewById(R.id.condition);
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);
        seaLevel = findViewById(R.id.seaLevel);
        tempDegree = findViewById(R.id.tempDegree);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        mainActivity = findViewById(R.id.mainActivity);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is disabled", Toast.LENGTH_SHORT).show();
            startActivity(new android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            finish();
        } else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    String city = getCityNameFromLocation(this, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    getWeather(city);
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }


            }
        }


        //SearchView
        ((SearchView) SearchBarView).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getWeather(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

    }

    private void getWeather(String city) {
        final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
        final String API_KEY = "d3c8c11f22a6c8f2c156965e798fb582";

        String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

        MyHttpURLConnection.fetchData(urlString, new MyHttpURLConnection.HttpCallback() {
            @Override
            public void onSuccess(String result) throws JSONException {

                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");

                String city = jsonObject.getString("name");
                String temp = main.getString("temp")+ " °C";
                String tempMn ="Min Temp: " + main.getString("temp_min")+ " °C";
                String tempMx = "Max Temp: "+main.getString("temp_max")+ " °C";
                String humdity = main.getString("humidity")+ " %";
               if(main.has("sea_level")){
                    String sealvl = main.getString("sea_level")+ " hPa";
                   seaLevel.setText(sealvl);
                }else {
                    seaLevel.setText("N/A");
                }

                String windSpid = jsonObject.getJSONObject("wind").getString("speed")+ " m/s";
                String conditon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                //Convert the timestamp to date
                SimpleDateFormat sdfDate = new SimpleDateFormat("d MMMM, yyyy");
                SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE");
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

                long sunriseTimestamp = jsonObject.getJSONObject("sys").getLong("sunrise");
                long timestamp = jsonObject.getLong("dt");
                long sunsetTimestamp = jsonObject.getJSONObject("sys").getLong("sunset");

                // Multiply by 1000 to convert seconds to milliseconds
                String sunrse = sdfTime.format(sunriseTimestamp * 1000L);
                String sunst = sdfTime.format(sunsetTimestamp * 1000L);
                String date = sdfDate.format(timestamp * 1000L);
                String day = sdfDay.format(timestamp * 1000L);


                cityLocation.setText(city);
                tempDegree.setText(temp);
                tempMin.setText(tempMn);
                tempMax.setText(tempMx);
                humidity.setText(humdity);
                windSpeed.setText(windSpid);
                condition.setText(conditon);
                weatherCondition.setText(conditon);
                sunrise.setText(sunrse);
                sunset.setText(sunst);

                Date.setText(date);
                Day.setText(day);

                int weatherId = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
                changeBackground(weatherId);
                //Toast.makeText(MainActivity.this, "Weather ID : " + weatherId, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "No city found.", Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error: " + errorMessage);
            }
        });
    }

    public void changeBackground(int weatherId){
        if(weatherId>=200 && weatherId<=232){
            lottieAnimationView.setAnimation(R.raw.thunderstorm);
            mainActivity.setBackgroundColor(Color.parseColor("#7F94F6"));

        }else if(weatherId>=300 && weatherId<=321) {
            lottieAnimationView.setAnimation(R.raw.shower_rain);
            mainActivity.setBackgroundColor(Color.parseColor("#C2C8E3"));
        }else if(weatherId>=500 && weatherId<=531) {
            lottieAnimationView.setAnimation(R.raw.rain);
            mainActivity.setBackgroundColor(Color.parseColor("#C2C8E3"));
        }else if(weatherId>=600 && weatherId<=622) {
           lottieAnimationView.setAnimation(R.raw.snow);
            mainActivity.setBackgroundColor(Color.parseColor("#C2C8E3"));
        }else if(weatherId>=701 && weatherId<=781) {
          lottieAnimationView.setAnimation(R.raw.mist);
            mainActivity.setBackgroundColor(Color.parseColor("#A5ACCF"));
        }else if(weatherId==800) {
            lottieAnimationView.setAnimation(R.raw.clear_sky);
            mainActivity.setBackgroundColor(Color.parseColor("#4B5CB1"));
        }else if(weatherId==801) {
            lottieAnimationView.setAnimation(R.raw.few_clouds);
            mainActivity.setBackgroundColor(Color.parseColor("#A9B8FD"));
        }else if(weatherId>=802 && weatherId<=804) {
            lottieAnimationView.setAnimation(R.raw.broken_scattered_clouds);
            mainActivity.setBackgroundColor(Color.parseColor("#A9B8FD"));

        }else{
            lottieAnimationView.setAnimation(R.raw.haze_all);
            mainActivity.setBackgroundColor(Color.parseColor("#4B5CB1"));
        }
        lottieAnimationView.playAnimation();
    }

    public static String getCityNameFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address =  addresses.get(0);
                return address.getLocality(); // Get the city name
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}