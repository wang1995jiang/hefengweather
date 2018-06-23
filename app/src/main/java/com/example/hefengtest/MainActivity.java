package com.example.hefengtest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements MyScrollView.OnScrollChangeListener {

    RefreshLayout refreshLayout;
    LinearLayout.LayoutParams layoutParams,layoutParamImage;
    int y=0,realHeight=0;
    TextView pull;
    boolean isInfo=true;
    int aqiGrade=0;
    private Boolean isAirFirst = true;
    private Boolean isSunFirst = true;
    View air1,air2,air3,air4,air5,air6,scroll;
    MyScrollView scrollView;
    LinearLayout linearLayout,toolbar,toolbarTwo,refresh,linearScroll;
    Weather weather;
    AirNow airNow;
    String jsonStr="",jsonStrAir="";
    Dialog dialog;
    TextView show,downText;
    Huayuan huayuan;
    ImageView imageView,imageViewDown;
    Weather.HeWeather6Bean.DailyForecastBean dailyForecastBean;
    final String urlNow = "https://free-api.heweather.com/s6/weather?" +
            "location=nanyang"+"&" +
            "key=516b156869d3478b9741e63dfcafe6d4"+"&"+
            "lang=en"+"&"+
            "unit=m";
    final String urlAir = "https://free-api.heweather.com/s6/air/now?" +
            "location=nanyang"+"&" +
            "key=516b156869d3478b9741e63dfcafe6d4"+"&"+
            "lang=en"+"&"+
            "unit=m";
    private static String picUrl="";
    View air;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindow();
        setContentView(R.layout.activity_main);

        getNetInfor();
        showProgress();

        linearLayout=(LinearLayout)findViewById(R.id.weather_all);

        toolbar=(LinearLayout)findViewById(R.id.toolbar);
        toolbarTwo=(LinearLayout)findViewById(R.id.toolbar);
        scrollView=(MyScrollView)findViewById(R.id.scro_view);
        scrollView.setOnScrollChangeListener(this);


        imageView=(ImageView)findViewById(R.id.bing_pic);
        if(isInfo){
            new GetBingPic().execute();
        }

        refreshLayout = (RefreshLayout)findViewById(R.id.refreshLayout);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setHeaderHeight(70);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {

                new GetBingPic().execute();
            }
        });

    }

    public void getNetInfor() {
        //首先是获取网络连接管理者
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        //网络状态存在并且是已连接状态
        if (info != null && info.isConnected()) {
            Toast.makeText(MainActivity.this, "网络已连接", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            isInfo=false;
        }
    }

    private void setWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // 设置竖屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private View getNowView(LinearLayout root,Weather.HeWeather6Bean.NowBean nowBean, Weather.HeWeather6Bean.DailyForecastBean dailyForecastBean){
        View view= LayoutInflater.from(this).inflate(R.layout.weather_now,root,false);
        TextView tem=(TextView)view.findViewById(R.id.air_temperature);
        TextView condTxt=(TextView)view.findViewById(R.id.cond_txt);
        TextView tmp=(TextView)view.findViewById(R.id.tmp);
        TextView hum=(TextView)view.findViewById(R.id.hum);

        hum.setText("湿度"+nowBean.getHum());
        tem.setText(nowBean.getFl());
        condTxt.setText(ImageChose.getWeatherName(Integer.valueOf(nowBean.getCond_code())));
        tmp.setText(dailyForecastBean.getTmp_max()+"°C/"+dailyForecastBean.getTmp_min()+"°C");

        return view;
    }
    private View getHourlyView(LinearLayout root,List<Weather.HeWeather6Bean.HourlyBean> hourlyBeans){
        View view= LayoutInflater.from(this).inflate(R.layout.weather_hour,root,false);
        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.recy);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        HourAdapter hourAdapter=new HourAdapter(hourlyBeans);
        recyclerView.setAdapter(hourAdapter);

        return view;
    }
    private View getLineView(){
        View view=LayoutInflater.from(this).inflate(R.layout.line,null,false);
        return view;
    }
    private View getDailyView(LinearLayout root,Weather.HeWeather6Bean.DailyForecastBean daily){
        View view=LayoutInflater.from(this).inflate(R.layout.weather_daily_forecast,root,false);
        TextView date=(TextView)view.findViewById(R.id.date_daily);
        ImageView icon=(ImageView)view.findViewById(R.id.icon_daily);
        TextView tmp=(TextView)view.findViewById(R.id.tmp_daily);
        date.setText(ImageChose.getDivisionString(daily.getDate(),false));
        icon.setImageResource(ImageChose.getWeatherImage(Integer.valueOf(daily.getCond_code_d()),null,false));
        tmp.setText(daily.getTmp_max()+"°C/"+daily.getTmp_min()+"°C");
        return view;
    }
    private void showProgress(){
        dialog=new Dialog(this);
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);
        ProgressBar progressBar=(ProgressBar)inflate.findViewById(R.id.progress_bar);
        show=(TextView)inflate.findViewById(R.id.show_result);
        if (!isInfo){
            show.setText("网络未连接");
        }
        dialog.setContentView(inflate);
        dialog.show();
    }
    private View getJsonView(){
        View view=LayoutInflater.from(this).inflate(R.layout.text_json_show,null,false);
        TextView textView=(TextView)view.findViewById(R.id.json_show);
        if (!jsonStr.isEmpty()){
            textView.setText(jsonStr);
        }
        return view;
    }
    private View getAirNowView(LinearLayout root,AirNow.HeWeather6Bean.AirNowCityBean airNowCityBean){
        air=LayoutInflater.from(this).inflate(R.layout.air_quality,root,false);

        air1=(View)air.findViewById(R.id.air_1);
        air2=(View)air.findViewById(R.id.air_2);
        air3=(View)air.findViewById(R.id.air_3);
        air4=(View)air.findViewById(R.id.air_4);
        air5=(View)air.findViewById(R.id.air_5);
        air6=(View)air.findViewById(R.id.air_6);

        TextView aqiTxt=(TextView)air.findViewById(R.id.aqi_text);
        TextView aqi=(TextView)air.findViewById(R.id.aqi);
        TextView main=(TextView)air.findViewById(R.id.main);
        TextView pm10=(TextView)air.findViewById(R.id.pm10);
        TextView pm25=(TextView)air.findViewById(R.id.pm2_5);
        TextView no2=(TextView)air.findViewById(R.id.no2);
        TextView so2=(TextView)air.findViewById(R.id.so2);
        TextView o3=(TextView)air.findViewById(R.id.o3);
        TextView co=(TextView)air.findViewById(R.id.co);

        aqiTxt.setText(ImageChose.getAqiTxt(Integer.valueOf(airNowCityBean.getAqi())));
        aqi.setText(airNowCityBean.getAqi());
        main.setText(airNowCityBean.getMain());
        pm10.setText(airNowCityBean.getPm10());
        pm25.setText(airNowCityBean.getPm25());
        no2.setText(airNowCityBean.getNo2());
        so2.setText(airNowCityBean.getSo2());
        o3.setText(airNowCityBean.getO3());
        co.setText(airNowCityBean.getCo());

        aqiGrade=ImageChose.getAqiGrade(Integer.valueOf(airNowCityBean.getAqi()));
        return air;
    }

    private View getLifeView(LinearLayout root,List<Weather.HeWeather6Bean.LifestyleBean> lifestyleBeans){
        View view=LayoutInflater.from(this).inflate(R.layout.weather_lifestyle,root,false);
        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.recy_life);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        LifeAdapter lifeAdapter=new LifeAdapter(lifestyleBeans);
        recyclerView.setAdapter(lifeAdapter);

        return view;
    }

    private View getWindyView(LinearLayout root,Weather.HeWeather6Bean.NowBean nowBean){
        View view=LayoutInflater.from(this).inflate(R.layout.weather_win,root,false);

        ImageView gif=(ImageView)view.findViewById(R.id.windy_gif);
        TextView deg=(TextView)view.findViewById(R.id.wind_deg);
        TextView dir=(TextView)view.findViewById(R.id.wind_dir);
        TextView sc=(TextView)view.findViewById(R.id.wind_sc);
        TextView spd=(TextView)view.findViewById(R.id.wind_spd);

        Glide.with(this).load(R.drawable.wind).into(gif);
        deg.setText(nowBean.getWind_deg());
        dir.setText(ImageChose.getWindDir(nowBean.getWind_dir()));
        sc.setText(nowBean.getWind_sc()+"级");
        spd.setText(nowBean.getWind_spd());
        return view;
    }

    private View getSunView(LinearLayout root, Weather.HeWeather6Bean.DailyForecastBean dailyForecastBean){
        View sun=LayoutInflater.from(this).inflate(R.layout.weather_sun,root,false);
        Provide.setSunUp("日出 "+dailyForecastBean.getSr());
        Provide.setSunDown("日落 "+dailyForecastBean.getSs());
        huayuan=new Huayuan(this,ImageChose.getCircularDegrees(dailyForecastBean.getSr(),dailyForecastBean.getSs()));
        return sun;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onScrollChange(MyScrollView view, int x, int y, int oldx, int oldy) {


        if (y<=255){
            toolbar.setAlpha((float)(255-y)/255);
        }
        if (checkIsVisible(this,air)){
            if (isAirFirst){
                isAirFirst=false;
                new ViewChange().execute();
            }
        }

        this.y=y;

       // Log.i("oldY+++++",""+oldy);
    }

    @Override
    public void onScrollBottomListener() {
        if (isSunFirst){
            isSunFirst=false;
            linearLayout.addView(getSunView(linearLayout,dailyForecastBean));
        }

    }

    @Override
    public void onScrollTopListener() {

    }

    public static Boolean checkIsVisible(Context context, View view) {
        // 如果已经加载了，判断广告view是否显示出来，然后曝光
        int screenWidth = getScreenMetrics(context).x;
        int screenHeight = getScreenMetrics(context).y;
        Rect rect = new Rect(0, 0, screenWidth, screenHeight);
        int[] location = new int[2];
        view.getLocationInWindow(location);
        if (view.getLocalVisibleRect(rect)) {
            return true;
        } else {
            //view已不在屏幕可见区域;
            return false;
        }
    }

    /**
     * 获取屏幕宽度和高度，单位为px
     * @param context
     * @return
     */
    public static Point getScreenMetrics(Context context){
        DisplayMetrics dm =context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);
    }




    class ViewChange extends AsyncTask{

        @Override
        protected void onPreExecute() {
            air1.setAlpha(0);
            air2.setAlpha(0);
            air3.setAlpha(0);
            air4.setAlpha(0);
            air5.setAlpha(0);
            air6.setAlpha(0);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            int i=0;
            while (true){
                SystemClock.sleep(100);
                i++;
                publishProgress(i);
                if (i>aqiGrade*10){
                    break;
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            int grade=(int)values[0];
            if (grade>0&&grade<=10){
                air1.setAlpha((float)0.1*grade);
            }else if (grade>10&&grade<=20){
                air2.setAlpha((float)0.1*(grade-10));
            }else if (grade>20&&grade<=30){
                air3.setAlpha((float)0.1*(grade-20));
            }else if (grade>30&&grade<=40){
                air4.setAlpha((float)0.1*(grade-30));
            }else if (grade>40&&grade<=50){
                air5.setAlpha((float)0.1*(grade-40));
            }else if (grade>50&&grade<=60){
                air6.setAlpha((float)0.1*(grade-50));
            }
        }

        @Override
        protected void onPostExecute(Object o) {
        }
    }

    class GetBingPic extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url("http://guolin.tech/api/bing_pic").build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                picUrl=response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            if ((Boolean) o){
                Glide.with(MainActivity.this).load(picUrl).into(imageView);
                new WeatherData().execute();
            }else {
                show.setText("获取失败");
            }
        }
    }

    class WeatherData extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(urlNow).build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                jsonStr=response.body().string();
                Log.i("json++++++++",jsonStr);
                //打印json
                if (!jsonStr.isEmpty()){
                    Gson gson=new Gson();
                    weather=gson.fromJson(jsonStr,Weather.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {

            if ((Boolean) o){
                new AirData().execute();
            }else {
                show.setText("获取失败");
            }

        }
    }
    class AirData extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(urlAir).build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                jsonStrAir=response.body().string();
                Log.i("json++++++++",jsonStrAir);
                //打印json
                if (!jsonStrAir.isEmpty()){
                    Gson gson=new Gson();
                    airNow=gson.fromJson(jsonStrAir,AirNow.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o){

            if ((Boolean) o){
                show.setText("获取成功");
                dialog.dismiss();
                addIteamView();

            }else {
                show.setText("获取失败");
            }
        }
    }

    private void addIteamView(){
        if (linearLayout.getChildCount()!=0){
            linearLayout.removeAllViews();
            isSunFirst=true;
            isAirFirst=true;
        }

        linearLayout.addView(getNowView(linearLayout,weather.getHeWeather6().get(0).getNow(),weather.getHeWeather6().get(0).getDaily_forecast().get(0)));
        linearLayout.addView(getLineView());
        linearLayout.addView(getHourlyView(linearLayout,weather.getHeWeather6().get(0).getHourly()));
        linearLayout.addView(getLineView());
        dailyForecastBean=weather.getHeWeather6().get(0).getDaily_forecast().get(0);
        weather.getHeWeather6().get(0).getDaily_forecast().remove(0);
        for (Weather.HeWeather6Bean.DailyForecastBean dailyForecastBean:weather.getHeWeather6().get(0).getDaily_forecast()){
            linearLayout.addView(getDailyView(linearLayout,dailyForecastBean));
            linearLayout.addView(getLineView());
        }
        linearLayout.addView(getAirNowView(linearLayout,airNow.getHeWeather6().get(0).getAir_now_city()));
        linearLayout.addView(getLineView());
        linearLayout.addView(getLifeView(linearLayout, weather.getHeWeather6().get(0).getLifestyle()));
        linearLayout.addView(getLineView());
        linearLayout.addView(getWindyView(linearLayout,weather.getHeWeather6().get(0).getNow()));
        linearLayout.addView(getLineView());

        refreshLayout.finishRefresh();
    }
}
