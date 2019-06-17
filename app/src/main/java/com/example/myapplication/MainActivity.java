package com.example.myapplication;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.lab1_map)).getMapAsync(this);


    }



 /*   //현재 주소 가져오기
    public String getCurrentAddress(LatLng latlng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가",Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용 불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if(addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }*/





    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


        if (map != null) {
            LatLng latLng = new LatLng(37.566643, 126.978279);
            CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(16f).build();

            map.moveCamera(CameraUpdateFactory.newCameraPosition(position));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
            markerOptions.position(latLng);
            markerOptions.title("서울시청");
            markerOptions.snippet("Tel:02-120");

            map.addMarker(markerOptions);

            MyGeocodingThread thread = new MyGeocodingThread(latLng);
            thread.start();

            String address = "서울 특별시 중고 서소문동 37-9";
            MyReverseGeocodingThread reverseThread = new MyReverseGeocodingThread(address);
            reverseThread.start();
        }
    }

        class MyReverseGeocodingThread extends  Thread {
            String address;
            public MyReverseGeocodingThread(String address){
                this.address=address;
            }

            public void run(){
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try{
                    List<Address> result = geocoder.getFromLocationName(address,1);
                    Address resultAddress=result.get(0);
                    LatLng latLng = new LatLng(resultAddress.getLatitude(),resultAddress.getLongitude());

                    Message msg = new Message();
                    msg.what = 200;
                    msg.obj = latLng;
                    handler.sendMessage(msg);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        class MyGeocodingThread extends  Thread {
            LatLng latLng;

            public MyGeocodingThread(LatLng latLng){
                this.latLng = latLng;
            }

            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(MainActivity.this);

                List<Address> addresses = null;
                String addressText ="";
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,2);
                    Thread.sleep(500);

                    if(addresses != null && addresses.size()>0) {
                        Address address = addresses.get(0);
                        addressText = address.getAdminArea() + "" + (address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : address.getLocality()) + " ";

                        String txt = address.getSubLocality();

                        if (txt != null)
                            addressText += txt + "";

                        addressText += address.getThoroughfare() + " " + address.getSubThoroughfare();

                        Message msg = new Message();
                        msg.what = 100;
                        msg.obj = addressText;
                        handler.sendMessage(msg);
                    }
                    } catch(IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            Handler handler = new Handler(){
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case 100: {
                            Toast toast = Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_SHORT);
                            toast.show();
                            break;
                        }
                        case 200:{
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location));
                            markerOptions.position((LatLng) msg.obj);
                            markerOptions.title("서울시립미술관");
                            map.addMarker(markerOptions);
                        }
                    }
                };
            };
        }

