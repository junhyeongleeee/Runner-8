package com.example.Runner8.TRASH;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.Runner8.R;
import com.example.Runner8.ui.map.Calcurate.Calculator;
import com.example.Runner8.ui.map.Courses;
import com.example.Runner8.ui.map.Kalman.KalmanFilter;
import com.example.Runner8.ui.map.SingleTon.MapSingleTon;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.geometry.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MapService extends Service{

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSSS";

    private NotificationManager notificationManager;
    Notification notification;
    LocationManager locationManager;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;

    TimerTask timerTask;
    private Timer timer = new Timer();
    double TimeStamp = 0;

    String speed = "";

    KalmanFilter kalmanFilter = new KalmanFilter();
    Calculator calculator = new Calculator();

    private FusedLocationProviderClient fusedLocationProviderClient;

    double current_distance = 0;
    String currentSec = "";
    double current_kcal = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("onCreate", "onCreate!!");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Service ????????? Activity ???????????? ????????? ??? ??? ???????????? ?????????
        //????????? ????????? ?????? ????????? null

        return null;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("onStartCommand", "onStartCommand!!");
        Log.i("MapSingleTon", MapSingleTon.getInstance().isForeground_check() + "");

        // Location
        /*
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("PERMISSION", "check!!");
        }

         */
        notificationManager = (NotificationManager) getSystemService((Context.NOTIFICATION_SERVICE));

        createNotificationChannel();

        // locationRequest = new LocationRequest();

        /*
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(3000);
        locationRequest.setMaxWaitTime(6000);


         */
        /*
        if (ActivityCompat.checkSelfPermission(getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplication(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplication(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        speed = String.valueOf(location.getSpeed());

                        // ?????? ?????? ?????? ?????????
                        LatLng c_l = new LatLng(location);
                        // ?????? ????????? ?????????
                        LatLng p_l = Courses.getInstance().getPrev_loc();
                        // ?????? ?????? ????????? ?????????
                        Courses.getInstance().setCur_loc(c_l);

                        if (location.getAccuracy() < 40) {

                            // ?????? ????????? ????????? ????????? ??????
                            if (c_l.distanceTo(p_l) > 0.10) {

                                SimpleDateFormat sdf3 = new SimpleDateFormat(DATE_FORMAT);
                                sdf3.setTimeZone(TimeZone.getTimeZone("GMT"));

                                Courses.getInstance().setCur_time(System.currentTimeMillis());
                                Date date = new Date();

                                // LOG
                                //////////////////////////////////////////////////////////////////////////////////////////////////

                                //////////////////////////////////////////////////////////////////////////////////////////////////

                                // KalmanFilter
                                // TimeStamp, Speed, lat, long, accuracy

                                double cur_time = System.currentTimeMillis();
                                double prev_time = Courses.getInstance().getPrev_time();

                                // TimeStamp
                                TimeStamp = (cur_time - prev_time) / 1000;

                                Log.i("TimeStamp", TimeStamp + " ");

                                // Distance
                                LatLng cur_latlng = new LatLng(location.getLatitude(), location.getLongitude());
                                double distance = Courses.getInstance().getPrev_loc().distanceTo(cur_latlng);

                                // SPEED
                                double speed = distance / TimeStamp;

                                Log.i("distance", distance + " ");
                                Log.i("getAccuracy", location.getAccuracy() + "");

                                // kalman
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    MapSingleTon.getInstance().processKalmanFilter(speed, location.getLatitude(),
                                            location.getLongitude(), System.currentTimeMillis(),
                                            location.getAccuracy(), location.getAltitude(), location.getVerticalAccuracyMeters());
                                }
                                LatLng kalman_latlng = new LatLng(MapSingleTon.getInstance().getKalmanFilter().getLatitude(),
                                        MapSingleTon.getInstance().getKalmanFilter().getLongitude());
                                Location kalman_location = new Location("kalman");
                                kalman_location.setLatitude(kalman_latlng.latitude);
                                kalman_location.setLongitude(kalman_latlng.longitude);


                                // Courses kalman
                                Courses.getInstance().addKalman_latLngs(kalman_latlng);

                                //////////////////////////////////////////////////////////////////////////////////////////////////

                                // ????????? ??????

                                // ?????? ?????? ??? ?????? (1km ???)
                                if (MapSingleTon.getInstance().isDistance_check())
                                    // Courses.getInstance().intervalSpeechChecking(Courses.getInstance().getTotal_distance());

                                    // QUARTER LOCATION
                                    ///////////////////////////////////////////////////////////////////

                                    // ???????????? ????????? ????????? 500m??? ?????? ??? quarter point ??? ?????? ???.
                                    if (Courses.getInstance().getQuarterly_distance() + distance > 500.0) {

                                        Courses.getInstance().addQuarter_location(location);

                                        // point ??? ?????? ????????? quarterly_distance ??? ?????? ?????? ???.
                                        Courses.getInstance().clearQuarterly_distance();
                                    }
                                    // 500m ???????????? ?????? ?????? ??????
                                    else
                                        Courses.getInstance().addQuarterly_distance(distance);


                                // follow ?????? ??? ??????
                                if (Courses.getInstance().isItem_pick_check()) {

                                    if (MapSingleTon.getInstance().isQuarter_check()) {

                                        if (Courses.getInstance().getRecord_quarter_locations().size() !=
                                                Courses.getInstance().getUser_qua_index()) {

                                            if (cur_latlng.distanceTo(
                                                    Courses.getInstance().getRecord_quarter_locations().get(
                                                            Courses.getCoursesInstance().getUser_qua_index())) < 30) {

                                                Courses.getInstance().speeching(
                                                        Courses.getInstance().getUser_qua_index() + 1 + "????????? ????????? ????????????.");
                                                // check
                                                Courses.getInstance().PlusQuarter_index();
                                                // ????????? ????????? ????????? ??? check_count ??? ???????????? ????????? ???????????? ????????? ???.
                                            }
                                        }
                                    }

                                    if (MapSingleTon.getInstance().isArrive_check()) {
                                        LatLng finish_lng = new LatLng(Double.valueOf(MapSingleTon.getInstance().getFinish_lat()),
                                                Double.valueOf(MapSingleTon.getInstance().getFinish_long()));

                                        Courses.getInstance().guideChecking(cur_latlng, finish_lng);
                                    }
                                }

                                ///////////////////////////////////////////////////////////////////

                                // ?????? ????????? ????????? ?????? ???????????? ?????????
                                Courses.getInstance().addLatLngs(c_l);

                                // ?????? ????????? ?????? ?????????
                                Courses.getInstance().setPrev_loc(c_l);

                                // ?????? ?????? ?????????
                                Courses.getInstance().setPrev_time(Courses.getInstance().getCur_time());

                                // ?????? ?????? ?????????
                                Courses.getInstance().addTime(System.currentTimeMillis());

                                // Courses Locations
                                Courses.getInstance().addLocations(kalman_location);

                                //
                                Courses.getInstance().addTotal_distance(distance);
                            }
                        }
                    }
                    else {

                        Log.i("SERVICE NULL", "check!!");
                    }
                })
                .addOnFailureListener(e -> Log.i("OnFailureListener", "check!!"));


        //

        current_distance = Math.round(Courses.getInstance().getTotal_distance() * 100) / 100;
        int current_second = Courses.getInstance().getTotal_second();
        String regular_time = calculator.time_result(current_distance);
        current_kcal = Math.round(calculator.kcal_result(
                Integer.valueOf(regular_time.split("[ ]")[0])) * 100) / 100;
        currentSec = calculator.format_time_num(current_second);

        // ?????? ?????? ????????? ?????? ?????? ??? ????????????
        notification = new NotificationCompat.Builder(getApplicationContext(), "position")
                .setContentText("?????? : " + current_distance + "m \n"
                        + "?????? : " + currentSec + "\n"
                        + "????????? : " + current_kcal + "Kcal" + "TimeStamp : " + TimeStamp)
                .setContentTitle("Runner 8")
                .setSmallIcon(R.drawable.run1)
                .setOngoing(true)
                .build();


        notification.flags = Notification.FLAG_NO_CLEAR;

        notificationManager.notify(10, notification);
        startForeground(10, notification);


        // Log.i("fusedLocant", fusedLocationProviderClient.getLocationAvailability().toString());

        */
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplication());

        timerTask = new TimerTask() {

            @Override
            public void run() {

                // Log.i("locationManager", locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) + "");
                // Log.i("locationManager", locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) + "");

                Log.i("isForeground_check()", MapSingleTon.getInstance().isForeground_check() + "");
                if (MapSingleTon.getInstance().isForeground_check()) {
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplication());

                    if (ActivityCompat.checkSelfPermission(getApplication(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplication(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                } else {
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplication());
                }

                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(location -> {

                            Log.i("check", "check!!");
                            if (location != null) {
                                Log.i("location", location.getLatitude() + "  " + location.getLongitude());

                                speed = String.valueOf(location.getSpeed());

                                // ?????? ?????? ?????? ?????????
                                LatLng c_l = new LatLng(location);
                                // ?????? ????????? ?????????
                                LatLng p_l = Courses.getInstance().getPrev_loc();
                                // ?????? ?????? ????????? ?????????
                                Courses.getInstance().setCur_loc(c_l);

                                if (location.getAccuracy() < 40) {

                                    if (p_l != null) {
                                        // ?????? ????????? ????????? ????????? ??????
                                        if (c_l.distanceTo(p_l) > 0.10) {

                                            SimpleDateFormat sdf3 = new SimpleDateFormat(DATE_FORMAT);
                                            sdf3.setTimeZone(TimeZone.getTimeZone("GMT"));

                                            Courses.getInstance().setCur_time(System.currentTimeMillis());
                                            Date date = new Date();

                                            // LOG
                                            //////////////////////////////////////////////////////////////////////////////////////////////////

                                            //////////////////////////////////////////////////////////////////////////////////////////////////

                                            // KalmanFilter
                                            // TimeStamp, Speed, lat, long, accuracy

                                            double cur_time = System.currentTimeMillis();
                                            double prev_time = Courses.getInstance().getPrev_time();

                                            // TimeStamp
                                            TimeStamp = (cur_time - prev_time) / 1000;

                                            Log.i("TimeStamp", TimeStamp + " ");

                                            // Distance
                                            LatLng cur_latlng = new LatLng(location.getLatitude(), location.getLongitude());
                                            double distance = Courses.getInstance().getPrev_loc().distanceTo(cur_latlng);

                                            // SPEED
                                            double speed = distance / TimeStamp;

                                            Log.i("distance", distance + " ");
                                            Log.i("getAccuracy", location.getAccuracy() + "");

                                            // kalman
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                MapSingleTon.getInstance().processKalmanFilter(speed, location.getLatitude(),
                                                        location.getLongitude(), System.currentTimeMillis(),
                                                        location.getAccuracy(), location.getAltitude(), location.getVerticalAccuracyMeters());
                                            }
                                            LatLng kalman_latlng = new LatLng(MapSingleTon.getInstance().getKalmanFilter().getLatitude(),
                                                    MapSingleTon.getInstance().getKalmanFilter().getLongitude());
                                            Location kalman_location = new Location("kalman");
                                            kalman_location.setLatitude(kalman_latlng.latitude);
                                            kalman_location.setLongitude(kalman_latlng.longitude);


                                            // Courses kalman
                                            Courses.getInstance().addKalman_latLngs(kalman_latlng);

                                            //////////////////////////////////////////////////////////////////////////////////////////////////

                                            // ????????? ??????

                                            // ?????? ?????? ??? ?????? (1km ???)
                                            if (MapSingleTon.getInstance().isDistance_check())
                                                // Courses.getInstance().intervalSpeechChecking(Courses.getInstance().getTotal_distance());

                                                // QUARTER LOCATION
                                                ///////////////////////////////////////////////////////////////////

                                                // ???????????? ????????? ????????? 500m??? ?????? ??? quarter point ??? ?????? ???.
                                                if (Courses.getInstance().getQuarterly_distance() + distance > 500.0) {

                                                    Courses.getInstance().addQuarter_location(location);

                                                    // point ??? ?????? ????????? quarterly_distance ??? ?????? ?????? ???.
                                                    Courses.getInstance().clearQuarterly_distance();
                                                }
                                                // 500m ???????????? ?????? ?????? ??????
                                                else
                                                    Courses.getInstance().addQuarterly_distance(distance);


                                            // follow ?????? ??? ??????
                                            if (Courses.getInstance().isItem_pick_check()) {

                                                if (MapSingleTon.getInstance().isQuarter_check()) {

                                                    if (Courses.getInstance().getRecord_quarter_locations().size() !=
                                                            Courses.getInstance().getUser_qua_index()) {

                                                        if (cur_latlng.distanceTo(
                                                                Courses.getInstance().getRecord_quarter_locations().get(
                                                                        Courses.getCoursesInstance().getUser_qua_index())) < 30) {

                                                            Courses.getInstance().speeching(
                                                                    Courses.getInstance().getUser_qua_index() + 1 + "????????? ????????? ????????????.");
                                                            // check
                                                            Courses.getInstance().PlusQuarter_index();
                                                            // ????????? ????????? ????????? ??? check_count ??? ???????????? ????????? ???????????? ????????? ???.
                                                        }
                                                    }
                                                }

                                                if (MapSingleTon.getInstance().isArrive_check()) {
                                                    LatLng finish_lng = new LatLng(Double.valueOf(MapSingleTon.getInstance().getFinish_lat()),
                                                            Double.valueOf(MapSingleTon.getInstance().getFinish_long()));

                                                    Courses.getInstance().guideChecking(cur_latlng, finish_lng);
                                                }
                                            }

                                            ///////////////////////////////////////////////////////////////////

                                            // ?????? ????????? ????????? ?????? ???????????? ?????????
                                            Courses.getInstance().addLatLngs(c_l);

                                            // ?????? ????????? ?????? ?????????
                                            Courses.getInstance().setPrev_loc(c_l);

                                            // ?????? ?????? ?????????
                                            Courses.getInstance().setPrev_time(Courses.getInstance().getCur_time());

                                            // ?????? ?????? ?????????
                                            Courses.getInstance().addTime(System.currentTimeMillis());

                                            // Courses Locations
                                            Courses.getInstance().addLocations(kalman_location);

                                            //
                                            Courses.getInstance().addTotal_distance(distance);
                                        }
                                    }
                                }
                            }
                            else {

                                Log.i("SERVICE NULL", "check!!");
                            }
                        })
                        .addOnFailureListener(e -> Log.i("OnFailureListener", "check!!"));


                //

                current_distance = Math.round(Courses.getInstance().getTotal_distance() * 100) / 100;
                int current_second = Courses.getInstance().getTotal_second();
                String regular_time = calculator.time_result(current_distance);
                current_kcal = Math.round(calculator.kcal_result(
                        Integer.valueOf(regular_time.split("[ ]")[0])) * 100) / 100;
                currentSec = calculator.format_time_num(current_second);

                // ?????? ?????? ????????? ?????? ?????? ??? ????????????
                notification = new NotificationCompat.Builder(getApplicationContext(), "position")
                        .setContentText("?????? : " + current_distance + "m \n"
                                + "?????? : " + currentSec + "\n"
                                + "????????? : " + current_kcal + "Kcal" + "TimeStamp : " + TimeStamp)
                        .setContentTitle("Runner 8")
                        .setSmallIcon(R.drawable.run1)
                        .setOngoing(true)
                        .build();


                notification.flags = Notification.FLAG_NO_CLEAR;

                notificationManager.notify(10, notification);


                startForeground(10, notification);


                Log.i("isForeground_check", "FALSE!!");
                /*
                timerTask.cancel();
                stopForeground(true);
                stopSelf();

                 */
            }
        };

        timer.schedule(timerTask, 0, 1000);

        // prev_loc, cur_loc ??? ??? ????????? Main ?????? ????????? ???
        // ?????? ???????????? ????????? ???????????? ??????
        // ?????? ?????? ????????????

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER,
                    null, getApplication().getMainExecutor(),
                    location -> {
                        if (location != null) {
                            this.location = location;

                            speed = String.valueOf(location.getSpeed());

                            // ?????? ?????? ?????? ?????????
                            LatLng c_l = new LatLng(location);
                            // ?????? ????????? ?????????
                            LatLng p_l = Courses.getInstance().getPrev_loc();
                            // ?????? ?????? ????????? ?????????
                            Courses.getInstance().setCur_loc(c_l);

                            if (location.getAccuracy() < 30) {

                                // ?????? ????????? ????????? ????????? ??????
                                if (c_l.distanceTo(p_l) != 0) {

                                    SimpleDateFormat sdf3 = new SimpleDateFormat(DATE_FORMAT);
                                    sdf3.setTimeZone(TimeZone.getTimeZone("GMT"));

                                    Courses.getInstance().setCur_time(System.currentTimeMillis());
                                    Date date = new Date();

                                    Log.i("date", date.toString());
                                    Log.i("time", String.valueOf(
                                            (Courses.getInstance().getCur_time() - Courses.getInstance().getPrev_time()) / 1000));
                                    Log.i("SERVICE DISTANCE", String.valueOf(c_l.distanceTo(p_l)));
                                    Log.i("SERVICE ACCURACY", String.valueOf(location.getAccuracy()));
                                    Log.i("SERVICE ATTITUDE", String.valueOf(location.getAltitude()));
                                    Log.i("SERVICE SPEED", String.valueOf(location.getSpeed()));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Log.i("SERVICE SPEED ACCURACY", String.valueOf(location.getSpeedAccuracyMetersPerSecond()));
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Log.i("SERVICE BEARING ACC", String.valueOf(location.getBearingAccuracyDegrees()));
                                        Log.i("SERVICE BEARING", String.valueOf(location.getBearing()));
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Log.i("SERVICE VERTICAL ACC", String.valueOf(location.getVerticalAccuracyMeters()));
                                    }
                                    Log.i("SERVICE TIME", sdf3.format(new Date(location.getTime())));
                                    Log.i("SERVICE LOCATION", location.getLatitude() + " " + location.getLongitude());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        Log.i("SERVICE TIME CHANGED", String.valueOf(TimeUnit.NANOSECONDS.toMillis(SystemClock.elapsedRealtimeNanos()
                                                - location.getElapsedRealtimeNanos())));
                                    }
                                    Log.i("SERVICE", "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");



                                    // KalmanFilter
                                    // TimeStamp, Speed, lat, long, accuracy

                                    // TimeStamp
                                    long TimeStamp = (System.currentTimeMillis() - Courses.getInstance().getPrev_time()) / 1000;

                                    // Distance
                                    LatLng cur_latlng = new LatLng(location.getLatitude(), location.getLongitude());
                                    double distance = Courses.getInstance().getPrev_loc().distanceTo(cur_latlng);

                                    // SPEED
                                    double speed = distance / TimeStamp;

                                    // ????????? ??????
                                    kalmanFilter.setState(location.getLatitude(), location.getLongitude(),
                                            TimeStamp, location.getAccuracy(), location.getAltitude());



                                    // ?????? ????????? ????????? ?????? ???????????? ?????????
                                    Courses.getInstance().addLatLngs(c_l);

                                    // ?????? ????????? ?????? ?????????
                                    Courses.getInstance().setPrev_loc(c_l);

                                    // ?????? ?????? ?????????
                                    Courses.getInstance().setPrev_time(Courses.getInstance().getCur_time());

                                    // ?????? ?????? ?????????
                                    Courses.getInstance().addTime(System.currentTimeMillis());

                                    // Courses Locations
                                    Courses.getInstance().addLocations(location);

                                    // SPEED
                                    // ?????? / ??????
                                    Log.i("SPEED", String.valueOf((c_l.distanceTo(p_l) /
                                            ((Courses.getInstance().getCur_time() - Courses.getInstance().getPrev_time()) / 1000))));

                                    Courses.getInstance().setSpeed((long) (c_l.distanceTo(p_l) /
                                            ((Courses.getInstance().getCur_time() - Courses.getInstance().getPrev_time()) / 1000)));
                                }
                            }
                        }

                        else{
                            Log.i("SERVICE NULL", "check!!");
                        }

                    });


         */


        /*
        ////////////////////////////////////////////////////////////////////////////
        Intent in = new Intent(this, MapAlarmService.class);
        startService(in);



        ////////////////////////////////////////////////////////////////////////////
        stopForeground(true);
        stopSelf();

         */



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //???????????? ????????? ??? ??????
        super.onDestroy();
    }

    private void createNotificationChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "position";

            NotificationChannel channel = new NotificationChannel("position", name,
                    NotificationManager.IMPORTANCE_MIN);
            channel.setDescription("current position");

            notificationManager.createNotificationChannel(channel);
        }
    }

}
