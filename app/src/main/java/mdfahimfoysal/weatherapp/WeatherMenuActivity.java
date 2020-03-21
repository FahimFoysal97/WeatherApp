package mdfahimfoysal.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class WeatherMenuActivity extends AppCompatActivity {

    static boolean isSearching;
    static ArrayList<String> addresses = new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //p.setVisibility(View.INVISIBLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_menu);
        //SharedPreferences sharedPreferences = this.getSharedPreferences("mdfahimfoysal.weatherapp", Context.MODE_PRIVATE);

        SQLiteDatabase WeatherDB = this.openOrCreateDatabase("WeatherDB",MODE_PRIVATE,null);
        //WeatherDB.execSQL("DROP TABLE Locations ");

        WeatherDB.execSQL("CREATE TABLE IF NOT EXISTS Locations (name varchar PRIMARY KEY, region varchar, country varchar) ");
        //WeatherDB.execSQL("INSERT INTO LOCATIONS VALUES (\"Debidwar\",\"Comilla\",\"Bangladesh\")");
        //WeatherDB.execSQL("DELETE FROM LOCATIONS WHERE NAME = \"Debidwar\"");
        Cursor c = WeatherDB.rawQuery("SELECT * From Locations",null);
        int nameIndex = c.getColumnIndex("name");
        int regionIndex = c.getColumnIndex("region");
        int countryIndex = c.getColumnIndex("country");
        c.moveToFirst();
        while(c!=null){
            try {
                String str = c.getString(nameIndex);
                if(c.getString(regionIndex)!=null)str+=(", " + c.getString(regionIndex));
                str+=(", " + c.getString(countryIndex));
                addresses.add(str);
            }   catch(Exception e){
                break;
            }

            c.moveToNext();
        }

        /*try {
            addresses = (ArrayList<String>) (ObjectSerializer.deserialize(sharedPreferences.getString("Addresses",ObjectSerializer.serialize(new ArrayList<String>()))));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //addresses = (ArrayList<String>)
        ListView listView =  findViewById(R.id.locationList);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,addresses);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {

            Intent intent = new Intent(getApplicationContext(),WeatherActivity.class);
            intent.putExtra("Location",addresses.get(position));
            intent.putExtra("Position",position);
            isSearching = false;
            startActivity(intent);


        });

        findViewById(R.id.locInputTextView).setOnKeyListener((v, keyCode, event) -> {

            if(keyCode==KeyEvent.KEYCODE_ENTER) {
                getWeather(v);
                return true;
            }
            return false;
        });

    }







    public void getWeather(View view) {

        if(((EditText) findViewById(R.id.locInputTextView)).getText().toString().equals(""))return;
        Intent intent = new Intent(getApplicationContext(),WeatherActivity.class);
        intent.putExtra("Location",((EditText) findViewById(R.id.locInputTextView)).getText().toString());
        isSearching = true;
        startActivity(intent);
        ((EditText) findViewById(R.id.locInputTextView)).setText("");
    }


    /*public void getRememberedAddresses(){
        try {
            addresses = (ArrayList<String>) (ObjectSerializer.deserialize(sharedPreferences.getString("Addresses",ObjectSerializer.serialize(new ArrayList<String>()))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRememberedAddresses(){
        sharedPreferences.edit().clear().apply();
        try {
            sharedPreferences.edit().putString("Addresses",ObjectSerializer.serialize(addresses)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    /*
    Location location;
    public LocationManager locationManager;
    public LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startListening();
        }
    }

    public void startListening(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void getCurrentLocationWeather(View v){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        } else{

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Intent intent = new Intent(getApplicationContext(),SearchWeatherLocActivity.class);
            intent.putExtra("Location",(String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude())));
            startActivity(intent);
            //System.out.println(location.toString());
        }
    }


    public void getCurLocWeather(View view){

        Intent intent = new Intent(getApplicationContext(),SearchWeatherLocActivity.class);
        intent.putExtra("Location",location.getLatitude()+","+location.getLongitude());
        startActivity(intent);
    }*/
}
