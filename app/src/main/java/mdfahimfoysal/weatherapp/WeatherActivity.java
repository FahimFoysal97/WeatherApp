package mdfahimfoysal.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;


public class WeatherActivity extends AppCompatActivity {

    boolean complete = false;
    boolean remembered = false;
    String addressName,addressRegion,addressCountry;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        String searchLoc = formatLocation(getIntent().getStringExtra("Location").toLowerCase());

        WeatherDownload weatherDownload = new WeatherDownload();

        String baseURL = "http://api.apixu.com/v1/forecast.json?key=f31f1ea3dff34bf48dc103231182505&q=";

        try {
            updateData(weatherDownload.execute(baseURL + searchLoc + "&days=7").get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Button btn = findViewById(R.id.button);
        if(WeatherMenuActivity.isSearching){
            remembered = false;
            btn.setText("Remember This Location");
            btn.setOnClickListener(v -> rememberLocation());
        }
        else {
            remembered = true;
            btn.setText("Forget This Location");
            btn.setOnClickListener(v -> forgetLocation());
        }

    }

    private String formatLocation(String str){
        str = str.toLowerCase();
        String temp[] = str.split(",");
        for(int i = 0; i<temp.length; i++){
            temp[i]=temp[i].trim();
            temp[i]=temp[i].replaceAll(" ","+");
        }
        StringBuilder strBuilder = new StringBuilder(temp[0]);
        for (int i = 1; i<temp.length; i++) {
            strBuilder.append(",").append(temp[i]);
        }
        str = strBuilder.toString();
        return str;
    }


    private String dateTimeFormat(String str){
        String temp[] = str.split(" ");
        String date[] = temp[0].split("-");
        String formatedDate = date[2]+"-"+date[1]+"-"+date[0];
        String time[] = temp[1].split(":");
        String formatedTime;
        if(Integer.parseInt(time[0])==0){
            formatedTime = "12:"+time[1]+" AM";
        }
        else if(Integer.parseInt(time[0])<12){
            formatedTime = temp[1] + " AM";
        }
        else if(Integer.parseInt(time[0])==12){
            formatedTime = temp[1] + " PM";
        }
        else {
            formatedTime = String.valueOf(Integer.parseInt(time[0])-12) + ":" + time[1] + " PM";
        }
        return formatedDate + " " + formatedTime;
    }

    private  String getDay(String string){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = newDateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        newDateFormat.applyPattern("EE");
        return newDateFormat.format(date);
    }



    private void updateData(String result){

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonLocObj = new JSONObject(jsonObject.getString("location"));

            String string = "";


            //I'll do db here

            addressName = jsonLocObj.getString("name");
            string += addressName;
            if (!jsonLocObj.getString("region").equals("")) {
                addressRegion =  jsonLocObj.getString("region");
                string+=", " + addressRegion;
            }
            addressCountry = jsonLocObj.getString("country");
            string += ", " + addressCountry;

            ((TextView)findViewById(R.id.locationView)).setText(string);
            string ="Last Updated : " + dateTimeFormat(jsonLocObj.getString("localtime"));

            ((TextView)findViewById(R.id.lastUpdatedTextView)).setText(string);


            JSONObject jsonCurObj = new JSONObject(jsonObject.getString("current"));
            ((TextView)findViewById(R.id.temperatureTextView)).setText((jsonCurObj.getString("temp_c")+"\u00B0C"));
            if(jsonCurObj.getString("is_day").equals("1")){
                findViewById(R.id.weatherBackground).setBackgroundResource(R.drawable.day_image_croped);
            }
            else {
                findViewById(R.id.weatherBackground).setBackgroundResource(R.drawable.night_image_croped);
            }
            ((TextView)findViewById(R.id.feelTemperatureTextView)).setText(("Feels : "+jsonCurObj.getString("feelslike_c")+"\u00B0C"));
            ((TextView)findViewById(R.id.humidityTextView)).setText(("Humidity : "+jsonCurObj.getString("humidity")+"%"));
            ((TextView)findViewById(R.id.cloudTextView)).setText(("Cloud : "+jsonCurObj.getString("cloud")+"%"));
            JSONObject jsonConObj = new JSONObject(jsonCurObj.getString("condition"));
            ((TextView)findViewById(R.id.weatherConditionTextView)).setText((jsonConObj.getString("text")));
            JSONObject jsonForObj = new JSONObject(jsonObject.getString("forecast"));
            JSONArray jsonForArr = jsonForObj.getJSONArray("forecastday");
            ((TextView)findViewById(R.id.highLowTextView)).setText(("Max : "+jsonForArr.getJSONObject(0).getJSONObject("day").getString("maxtemp_c")+"\u00B0C\nMin : "+jsonForArr.getJSONObject(0).getJSONObject("day").getString("mintemp_c")+"\u00B0C"));
            Bitmap bitmap = getBitmap(jsonConObj.getString("icon"));
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun);
            ((ImageView)findViewById(R.id.conditionImg)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,64,64,false)
            );


            JSONObject jsonForDayObj = jsonForArr.getJSONObject(1);
            ((TextView)findViewById(R.id.dayTextView1)).setText((getDay(jsonForDayObj.getString("date")) + "\t\t"));
            JSONObject jsonDayobj = jsonForDayObj.getJSONObject("day");
            ((TextView)findViewById(R.id.avgTempTextView1)).setText(("\t\tAverage\n"+jsonDayobj.getString("avgtemp_c")+"\u00b0C"));
            ((TextView)findViewById(R.id.highLowTempTextView1)).setText((
                    "Max : " + jsonDayobj.getString("maxtemp_c") + "\u00b0C\n"+
                    "Min : " + jsonDayobj.getString("mintemp_c") + "\u00b0C\n"+
                    "Humidity : " + jsonDayobj.getString("avghumidity") + "%\n"));
            //bitmap = (new ImageDownload()).execute(("http:"+jsonDayobj.getJSONObject("condition").getString("icon"))).get();
            bitmap = getBitmap(jsonDayobj.getJSONObject("condition").getString("icon"));
            ((ImageView)findViewById(R.id.condImageView1)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,48,48,false)
            );


            jsonForDayObj = jsonForArr.getJSONObject(2);
            ((TextView)findViewById(R.id.dayTextView2)).setText((getDay(jsonForDayObj.getString("date")) + "\t\t"));
            jsonDayobj = jsonForDayObj.getJSONObject("day");
            ((TextView)findViewById(R.id.avgTempTextView2)).setText(("\t\tAverage\n"+jsonDayobj.getString("avgtemp_c")+"\u00b0C"));
            ((TextView)findViewById(R.id.highLowTempTextView2)).setText((
                    "Max : " + jsonDayobj.getString("maxtemp_c") + "\u00b0C\n"+
                    "Min : " + jsonDayobj.getString("mintemp_c") + "\u00b0C\n"+
                    "Humidity : " + jsonDayobj.getString("avghumidity") + "%\n"));
            //bitmap = (new ImageDownload()).execute(("http:"+jsonDayobj.getJSONObject("condition").getString("icon"))).get();
            bitmap = getBitmap(jsonDayobj.getJSONObject("condition").getString("icon"));
            ((ImageView)findViewById(R.id.condImageView2)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,48,48,false)
            );


            jsonForDayObj = jsonForArr.getJSONObject(3);
            ((TextView)findViewById(R.id.dayTextView3)).setText((getDay(jsonForDayObj.getString("date")) + "\t\t"));
            jsonDayobj = jsonForDayObj.getJSONObject("day");
            ((TextView)findViewById(R.id.avgTempTextView3)).setText(("\t\tAverage\n"+jsonDayobj.getString("avgtemp_c")+"\u00b0C"));
            ((TextView)findViewById(R.id.highLowTempTextView3)).setText((
                    "Max : " + jsonDayobj.getString("maxtemp_c") + "\u00b0C\n"+
                    "Min : " + jsonDayobj.getString("mintemp_c") + "\u00b0C\n"+
                    "Humidity : " + jsonDayobj.getString("avghumidity") + "%\n"));
            //bitmap = (new ImageDownload()).execute(("http:"+jsonDayobj.getJSONObject("condition").getString("icon"))).get();
            bitmap = getBitmap(jsonDayobj.getJSONObject("condition").getString("icon"));
            ((ImageView)findViewById(R.id.condImageView3)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,48,48,false)
            );


            jsonForDayObj = jsonForArr.getJSONObject(4);
            ((TextView)findViewById(R.id.dayTextView4)).setText((getDay(jsonForDayObj.getString("date")) + "\t\t"));
            jsonDayobj = jsonForDayObj.getJSONObject("day");
            ((TextView)findViewById(R.id.avgTempTextView4)).setText(("\t\tAverage\n"+jsonDayobj.getString("avgtemp_c")+"\u00b0C"));
            ((TextView)findViewById(R.id.highLowTempTextView4)).setText((
                    "Max : " + jsonDayobj.getString("maxtemp_c") + "\u00b0C\n"+
                    "Min : " + jsonDayobj.getString("mintemp_c") + "\u00b0C\n"+
                    "Humidity : " + jsonDayobj.getString("avghumidity") + "%\n"));
            //bitmap = (new ImageDownload()).execute(("http:"+jsonDayobj.getJSONObject("condition").getString("icon"))).get();
            bitmap = getBitmap(jsonDayobj.getJSONObject("condition").getString("icon"));
            ((ImageView)findViewById(R.id.condImageView4)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,48,48,false)
            );


            jsonForDayObj = jsonForArr.getJSONObject(5);
            ((TextView)findViewById(R.id.dayTextView5)).setText((getDay(jsonForDayObj.getString("date")) + "\t\t"));
            jsonDayobj = jsonForDayObj.getJSONObject("day");
            ((TextView)findViewById(R.id.avgTempTextView5)).setText(("\t\tAverage\n"+jsonDayobj.getString("avgtemp_c")+"\u00b0C"));
            ((TextView)findViewById(R.id.highLowTempTextView5)).setText((
                    "Max : " + jsonDayobj.getString("maxtemp_c") + "\u00b0C\n"+
                    "Min : " + jsonDayobj.getString("mintemp_c") + "\u00b0C\n"+
                    "Humidity : " + jsonDayobj.getString("avghumidity") + "%\n"));
            //bitmap = (new ImageDownload()).execute(("http:"+jsonDayobj.getJSONObject("condition").getString("icon"))).get();
            bitmap = getBitmap(jsonDayobj.getJSONObject("condition").getString("icon"));
            ((ImageView)findViewById(R.id.condImageView5)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,48,48,false)
            );


            jsonForDayObj = jsonForArr.getJSONObject(6);
            ((TextView)findViewById(R.id.dayTextView6)).setText((getDay(jsonForDayObj.getString("date")) + "\t\t"));
            jsonDayobj = jsonForDayObj.getJSONObject("day");
            ((TextView)findViewById(R.id.avgTempTextView6)).setText(("\t\tAverage\n"+jsonDayobj.getString("avgtemp_c")+"\u00b0C"));
            ((TextView)findViewById(R.id.highLowTempTextView6)).setText((
                    "Max : " + jsonDayobj.getString("maxtemp_c") + "\u00b0C\n"+
                    "Min : " + jsonDayobj.getString("mintemp_c") + "\u00b0C\n"+
                    "Humidity : " + jsonDayobj.getString("avghumidity") + "%\n"));
            //bitmap = (new ImageDownload()).execute(("http:"+jsonDayobj.getJSONObject("condition").getString("icon"))).get();
            bitmap = getBitmap(jsonDayobj.getJSONObject("condition").getString("icon"));
            ((ImageView)findViewById(R.id.condImageView6)).setImageBitmap(
                    Bitmap.createScaledBitmap(bitmap,48,48,false)
            );

            //findViewById(R.id.progressBar).setVisibility(View.GONE);
            complete = true;

        } catch (JSONException e) {
            e.printStackTrace();

            getBack();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
    }

    public class WeatherDownload extends AsyncTask<String, Void, String> {



        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection;
            StringBuilder str= new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    char cur = (char) data;
                    str.append(cur);
                    data = reader.read();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return str.toString();
        }


    }

    static public class ImageDownload extends AsyncTask<String,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            Bitmap bitmap=null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

    }

    public void rememberLocation(){
        if(complete){
            remembered = false;
            for (String str : WeatherMenuActivity.addresses) {
                if(str.equals(addressName +", "+ addressRegion +", "+ addressCountry)){
                    remembered = true;
                }
            }
            if(!remembered) {
                WeatherMenuActivity.addresses.add(addressName +", "+ addressRegion +", "+ addressCountry);
                Collections.sort(WeatherMenuActivity.addresses, String::compareToIgnoreCase);
                WeatherMenuActivity.arrayAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Location Remembered", Toast.LENGTH_SHORT).show();
                //SharedPreferences sharedPreferences = this.getSharedPreferences("mdfahimfoysal.weatherapp", Context.MODE_PRIVATE);
                //sharedPreferences.edit().clear().apply();
                SQLiteDatabase WeatherDB = this.openOrCreateDatabase("WeatherDB",MODE_PRIVATE,null);

                WeatherDB.execSQL("INSERT INTO LOCATIONS (name,region,country) VALUES (\""+addressName+"\",\""+addressRegion+"\",\""+addressCountry+"\")");
                /*try {
                    //sharedPreferences.edit().putString("Addresses",ObjectSerializer.serialize(WeatherMenuActivity.addresses)).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            else Toast.makeText(this, "Location Already Remembered", Toast.LENGTH_SHORT).show();
        }
    }

    /*public void saveRememberedAddresses(){
        sharedPreferences.edit().clear().apply();
        try {
            sharedPreferences.edit().putString("Addresses",ObjectSerializer.serialize(WeatherMenuActivity.addresses)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    public void forgetLocation(){
        if(!remembered){
            Toast.makeText(this, "Location Already Forgotten", Toast.LENGTH_SHORT).show();
            return;
        }
        WeatherMenuActivity.addresses.remove(getIntent().getIntExtra("Position",0));
        WeatherMenuActivity.arrayAdapter.notifyDataSetChanged();
        remembered = false;
        SQLiteDatabase WeatherDB = this.openOrCreateDatabase("WeatherDB",MODE_PRIVATE,null);

        WeatherDB.execSQL("DELETE FROM Locations WHERE name = \"" + addressName+"\"");
        //SharedPreferences sharedPreferences = this.getSharedPreferences("mdfahimfoysal.weatherapp", Context.MODE_PRIVATE);
        //sharedPreferences.edit().clear().apply();
        /*try {
            sharedPreferences.edit().putString("Addresses",ObjectSerializer.serialize(WeatherMenuActivity.addresses)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Toast.makeText(this, "Location Forgotten", Toast.LENGTH_SHORT).show();
    }

    public void getBack(){
        this.finish();
        Toast.makeText(this,"Sorry, can not connect to the server", Toast.LENGTH_SHORT).show();
    }


    private Bitmap getBitmap(String string){
        String temp[]=string.split("/");

        if(temp[temp.length-2].equals("day")){
            switch (temp[temp.length-1]) {
                case "113.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d113);
                case "116.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d116);
                case "119.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d119);
                case "143.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d143);
                case "176.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d176);
                case "179.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d179);
                case "182.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d182);
                case "185.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d185);
                case "200.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d200);
                case "227.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d227);
                case "230.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d230);
                case "248.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d248);
                case "260.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d260);
                case "263.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d263);
                case "266.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d266);
                case "281.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d281);
                case "284.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d284);
                case "293.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d293);
                case "296.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d296);
                case "299.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d299);
                case "302.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d302);
                case "305.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d305);
                case "308.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d308);
                case "311.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d311);
                case "314.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d314);
                case "317.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d317);
                case "320.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d320);
                case "323.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d323);
                case "326.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d326);
                case "329.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d329);
                case "332.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d332);
                case "335.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d335);
                case "338.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d338);
                case "350.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d350);
                case "353.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d353);
                case "356.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d356);
                case "359.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d359);
                case "362.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d362);
                case "365.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d365);
                case "368.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d368);
                case "371.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d371);
                case "374.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d374);
                case "377.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d377);
                case "386.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d386);
                case "389.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d389);
                case "392.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d392);
                case "395.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.d395);
            }

        }
        else {
            switch (temp[temp.length-1]) {
                case "113.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n113);
                case "116.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n116);
                case "119.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n119);
                case "143.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n143);
                case "176.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n176);
                case "179.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n179);
                case "182.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n182);
                case "185.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n185);
                case "200.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n200);
                case "227.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n227);
                case "230.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n230);
                case "248.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n248);
                case "260.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n260);
                case "263.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n263);
                case "266.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n266);
                case "281.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n281);
                case "284.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n284);
                case "293.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n293);
                case "296.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n296);
                case "299.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n299);
                case "302.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n302);
                case "305.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n305);
                case "308.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n308);
                case "311.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n311);
                case "314.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n314);
                case "317.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n317);
                case "320.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n320);
                case "323.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n323);
                case "326.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n326);
                case "329.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n329);
                case "332.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n332);
                case "335.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n335);
                case "338.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n338);
                case "350.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n350);
                case "353.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n353);
                case "356.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n356);
                case "359.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n359);
                case "362.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n362);
                case "365.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n365);
                case "368.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n368);
                case "371.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n371);
                case "374.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n374);
                case "377.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n377);
                case "386.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n386);
                case "389.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n389);
                case "392.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n392);
                case "395.png":
                    return BitmapFactory.decodeResource(getResources(), R.drawable.n395);
            }
        }

        try {
            return (new ImageDownload()).execute(("http:"+string)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return  null;
    }

}
