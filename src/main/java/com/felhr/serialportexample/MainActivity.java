package com.felhr.serialportexample;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.serialportexample.listeners.PictureCapturingListener;
import com.felhr.serialportexample.services.APictureCapturingService;
import com.felhr.serialportexample.services.PictureCapturingServiceImpl;
import com.felhr.serialportexample.core.TensorflowClassifier;
import com.felhr.serialportexample.entity.Classifier;
import com.felhr.serialportexample.entity.Recognition;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    private ImageView imgObject;
    private EditText edtUrl;
    private Button btnPredict;

    private SurfaceHolder sHolder;
    private SurfaceView sv;

    //a bitmap to display the captured image
    private Bitmap bmp;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Camera.Parameters parameters;

    private static String MODEL = "mobilenet_quant_v1_224.tflite";
    private static String LABEL = "labels.txt";
    private static int IMG_SIZE = 224;
    int a1=0,a2=0,a3=0,a4=0;

    private Recognition recognition;
    private Executor executor = Executors.newSingleThreadExecutor();

    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;
    private TextView   teksColor;
    private TextView   status;
    private RelativeLayout latar;
    public SharedPreferences pref;
    public String selections="";
    private Button cancel;
    private Button oke;


    //The capture service
    private APictureCapturingService pictureService;

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;


    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    MediaPlayer sound1;
    MediaPlayer sound2;
    MediaPlayer sound3;
    MediaPlayer sound4;
    MediaPlayer soundCoba;
    MediaPlayer soundGagal;
    //aaaa

    TextView namabarang;
    TextView golongan;

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        imgObject = findViewById(R.id.imageBotol);
        btnPredict = findViewById(R.id.cekBotol);

        sv = (SurfaceView) findViewById(R.id.surfaceView);
        cancel =(Button) findViewById(R.id.cancel);
        oke =(Button) findViewById(R.id.oke);
        oke.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        sound1 = MediaPlayer.create(this, R.raw.sound1);
        sound2 = MediaPlayer.create(this, R.raw.sound3);
        sound3 = MediaPlayer.create(this, R.raw.sound2);
        sound4 = MediaPlayer.create(this, R.raw.sound4);
        soundCoba = MediaPlayer.create(this, R.raw.berhasil);
        soundGagal = MediaPlayer.create(this, R.raw.cobalagi);
        webView = (WebView) findViewById(R.id.webku);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Tiga baris di bawah ini agar laman yang dimuat dapat
        // melakukan zoom.
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        // Baris di bawah untuk menambahkan scrollbar di dalam WebView-nya
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    recognition = (Recognition) TensorflowClassifier.init(
                            getAssets(),
                            MODEL,
                            LABEL,
                            IMG_SIZE
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sendCommand('a');

                CapturePhoto();
//                foto();
            }
        });



        if (pref.getInt("data1", -1)<0) {
            editor.putInt("data1", 1);
            editor.apply();
        }
        if (pref.getInt("data2", -1)<0) {
            editor.putInt("data2", 1);
            editor.apply();
        }
        if (pref.getInt("data3", -1)<0) {
            editor.putInt("data3", 1);
            editor.apply();
        }
        if (pref.getInt("data4", -1)<0) {
            editor.putInt("data4", 1);
            editor.apply();
        }
        if (pref.getInt("data5", -1)<0) {
            editor.putInt("data5", 1);
            editor.apply();
        }



        mHandler = new MyHandler(this);

        display = (TextView) findViewById(R.id.textView1);
        status =(TextView) findViewById(R.id.status);
        editText = (EditText) findViewById(R.id.editText1);
        namabarang = (TextView) findViewById(R.id.namabarang);
        namabarang.setText("Nama : ");
        golongan = (TextView) findViewById(R.id.golongan);
        golongan.setVisibility(View.GONE);
        Button sendButton = (Button) findViewById(R.id.buttonSend);
        Button setting= (Button) findViewById(R.id.sett);
        Button kalibrasi = (Button) findViewById(R.id.kalibrasi);


        kalibrasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent kalibrasi = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(kalibrasi);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goMode = new Intent(MainActivity.this, Main3Activity.class);
                startActivity(goMode);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    String data = editText.getText().toString();
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        usbService.write(data.getBytes());
                    }
                }
            }
        });




        checkPermissions();
        uploadBackPhoto = (ImageView) findViewById(R.id.backIV);
        uploadFrontPhoto = (ImageView) findViewById(R.id.frontIV);
        teksColor = (TextView) findViewById(R.id.warna);
        latar = (RelativeLayout) findViewById(R.id.backgroundwar);
        final Button btn = (Button) findViewById(R.id.startCaptureBtn);
        // getting instance of the Service from PictureCapturingServiceImpl
        pictureService = PictureCapturingServiceImpl.getInstance(this);
        btn.setOnClickListener(v -> {
            showToast("Starting capture!");
            pictureService.startCapturing(this);
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_to("fn");
            }
        });

        oke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!selections.equals("kn")) {
                    send_to(selections);
                }
            }
        });

        //showToast(String.valueOf(pref.getInt("data1", -1)));
    }

    public void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show()
        );
    }
    public void foto() {
        pictureService.startCapturing(this);
    }
    public void buka() {
        //pictureService.startCapturing(this);
        status.setText("Masukan Sampah nya !!");
    }
    public void logam() {
        //pictureService.startCapturing(this);
        status.setText("ini Sampah Logam kesukaan ku");
    }
    public void nonlogam() {
        status.setText("ini Sampah Nonlogam yang aku ingin");
    }

    public void tutup() {
        status.setText("Aku lapar Beri aku sampah");
    }

    public void kena() {
        status.setText("Anda belum Beruntung Silahkan Coba Lagi");
    }

    public void coba() {
        status.setText("Mulai !!!");
    }

    public void finish() {
        status.setText("Selamat Anda Berhasil ");
    }

    public void sendL()  {
        //pictureService.startCapturing(this);
       // status.setText("ini Sampah Logam kesukaan ku");
        webView.loadUrl("https://api.telegram.org/bot820071304:AAFTMdl67nUUKD_e7fT1YGyUNgPknTI0Ojg/sendMessage?chat_id=-1001457907512&text=Tempat Sampah Logam Telah Penuh");
//        Toast toast = Toast.makeText(getApplicationContext(), "text", Toast.LENGTH_SHORT);
//        toast.show();


    }
    public void sendN() {
        webView.loadUrl("https://api.telegram.org/bot820071304:AAFTMdl67nUUKD_e7fT1YGyUNgPknTI0Ojg/sendMessage?chat_id=-1001457907512&text=Tempat Sampah Non Logam Telah Penuh");
        //pictureService.startCapturing(this);
        //status.setText("ini Sampah Logam kesukaan ku");
    }
    ///////////////////////////////////////////////////////////////////////funtion


    public void upload(String Url, String method, String imageString) {
        new AsyncTask<String, String, String>() {
            String method, imageString;
            int tmp;
            String data="";

            protected void onPreExecute() {
            }
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    method = params[1];
                    String urlParams = params[2];

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod(method);

                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setAllowUserInteraction(false);

                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(urlParams.getBytes());
                    os.flush();
                    os.close();

                    InputStream is = httpURLConnection.getInputStream();
                    while((tmp=is.read())!=-1){
                        data+= (char)tmp;
                    }

//                    namabarang.setText("Nama : "+ data);

                    is.close();
                    httpURLConnection.disconnect();

                    return data;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "Exception: "+e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Exception: "+e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String msg) {
//                Toast.makeText(MainActivity.this.getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                namabarang.setText(data);
                if(data.length()>6 && data.indexOf("t")>0 && data.indexOf("h")>0 && data.indexOf("a")>0) {
                    webView.loadUrl("http://192.168.43.216/absen/upload/rfid.php?rfid=12358595&time="+data);
                }
            }
        }.execute(Url, method, imageString);
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] byteFormat = stream.toByteArray();
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        return imgString;
    }




    private void CapturePhoto() {


        // Toast.makeText(getApplicationContext(), "Image snapshot   Started",Toast.LENGTH_SHORT).show();
        // here below "this" is activity context.
        //SurfaceView surface = new SurfaceView(this);
        Camera camera = Camera.open(1);
        // Toast.makeText(MainActivity.this, "ceki", Toast.LENGTH_SHORT).show();
        try {
            camera.setPreviewDisplay(sv.getHolder());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // camera.autoFocus();
        Camera.Parameters params = camera.getParameters();
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
////        params.setZoom(1);
////        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        params.setRotation(270);
        camera.setParameters(params);
        camera.startPreview();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        camera.takePicture(null,null,jpegCallback);
    }


    /** picture call back */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            FileOutputStream outStream = null;
            try {
                String dir_path = "storage/emulated/0/";// set your directory path here
                outStream = new FileOutputStream(dir_path+ File.separator+"aku.jpg");
                outStream.write(data);
                outStream.close();
                // Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally
            {
                camera.stopPreview();
                camera.release();
                camera = null;
                //Toast.makeText(getApplicationContext(), "Image snapshot Done",Toast.LENGTH_LONG).show();
                File imgFile = new  File(Environment.getExternalStorageDirectory()+ "/aku.jpg");

                if(imgFile.exists()){
                    //  Toast.makeText(MainActivity.this, "cek", Toast.LENGTH_SHORT).show();
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    Bitmap bitmap = Bitmap.createScaledBitmap(
                            myBitmap,
                            144,
                            240,
                            true
                    );
                    imgObject.setImageBitmap(bitmap);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    byte[] byte_arr = stream.toByteArray();
                    String encodedString = Base64.encodeToString(byte_arr, 0);

                    upload("http://192.168.43.216/absen/upload/ImageUpload.php","POST",getEncoded64ImageStringFromBitmap(bitmap));


//                    Bitmap bitmap1 = ((BitmapDrawable)imgObject.getDrawable()).getBitmap();
//                    String label = "";
//                    List<Classifier> resultPredict = recognition.recognize(bitmap1);
//                    for (Classifier result: resultPredict) {
//                        label+= result.getTitle() + " ";
//                    }

                    //Toast.makeText(MainActivity.this, label, Toast.LENGTH_SHORT).show();


                    // Toast.makeText(MainActivity.this,imgFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                   // Log.v("Tamvan", label);
//                    if(label.indexOf("water")>0){
//                        send_to("bn");
//                    }else if(label.indexOf("bottle")>0) {
//                        send_to("bn");
//                    }else {
//                        send_to("tn");
//                    }







//                    String lab ="";
//                    if( label.contains("jug")){a1=a1+1;}if( label.contains("tup")){a1=a1+1;}if (label.contains("tray")){a1=a1+1;}
//                    if (label.contains("towel")){a1=a1+1;}if (label.contains("loaf")){a1=a1+1;}if (label.contains("spatula")){a1=a1+1;}
//                    if (label.contains("rubber")){a1=a1+1;}if (label.contains("eraser")){a1=a1+1;}if (label.contains("bathtup")){a1=a1+1;}
//                    if (label.contains("dough")){a1=a1+1;}if (label.contains("band aid")){a1=a1+1;}if (label.contains("tub")){a1=a1+1;}
//                    if (label.contains("towel")){a1=a1+1;}
////                    if( label.contains("jug")){a1=a1+1;}
//
//                    if( label.contains("jug")){a2=a2+1;}if( label.contains("lotion")){a2=a2+1;}if( label.contains("spray")){a2=a2+1;}
//                    if( label.contains("bottle")){a2=a2+1;}if( label.contains("cup")){a2=a2+1;}if( label.contains("bubble")){a2=a2+1;}
//                    if( label.contains("water")){a2=a2+1;}if( label.contains("paint")){a2=a2+1;}
//
//                    if( label.contains("plastic")){a3=a3+1;}if( label.contains("bag")){a3=a3+1;}
//
//                    if( label.contains("tissue")){a4=a4+1;}if( label.contains("towel")){a4=a4+1;} if( label.contains("quilt")){a4=a4+1;}
//                    if( label.contains("paper")){a4=a4+1;}
//                    if( label.contains("menu")){a4=a4+1;}if( label.contains("pack")){a4=a4+1;}if( label.contains("book")){a4=a4+1;}
//                    if( label.contains("diaper")){a4=a4+1;}if( label.contains("hand")){a4=a4+1;}if( label.contains("envelope")){a4=a4+1;}
//                    if( label.contains("carton")){a4=a4+1;}if( label.contains("puzzle")){a4=a4+1;}if( label.contains("spatula")){a4=a4+1;}
//                    if( label.contains("rule")){a4=a4+1;}
//                    if( label.contains("hoopskirt")){a4=a4+1;}
//
//                    label = label + "" + a1 + "" + a2 + "" + a3 + "" + a4;
//
////                    if(a1==a2 || a1==a3 || a1==a4 || a2==a3 || a2==a4 || a3==a4) {
////                        CapturePhoto();
////                    }Toast.makeText(MainActivity.this, lab, Toast.LENGTH_SHORT).show();
////                    }
////                    else{
//
//                        if (a1 > a2 && a1 > a3 && a1 > a4) {
//                            selections = "an";
//                            lab = "Golongan : 1 (Gabus)";
//                            a1=0;a2=0;a3=0;a4=0;
//                        } else if (a2 > a1 && a2 > a3 && a2 > a4) {
//                            selections = "bn";
//                            lab = "Golongan : 2 (Botol)";
//                            a1=0;a2=0;a3=0;a4=0;
//                        } else if (a3 > a1 && a3 > a2 && a3 > a4) {
//                            selections = "cn";
//                            lab = "Golongan : 3 (Plastik)";
//                            a1=0;a2=0;a3=0;a4=0;
//                        } else if (a4 > a1 && a4 > a2 && a4 > a3) {
//                            selections = "dn";
//                            lab = "Golongan : 4 (Kertas/tisue)";
//
//                            a1=0;a2=0;a3=0;a4=0;
//                        } else {
//                            if (a1>0 || a2>0 || a3>0 || a4>0 ){
//                                selections = "kn";
//                                lab = "Foto Lagi";
//                                //oke.setText("foto lagi");
////                                foto();
//                                CapturePhoto();
//                            }else{
//
//                                selections = "en";
//                                lab = "Golongan : 5 (Lain - Lain)";
//                                a1=0;a2=0;a3=0;a4=0;
//                            }
//
//
//                        }
//                        namabarang.setText("Nama Barang : " + label);
//                        golongan.setText(lab);





                    //////////////////////
                       // Toast.makeText(MainActivity.this, lab, Toast.LENGTH_SHORT).show();

//                    }



                }

            }
            // Log.d(TAG, "onPictureTaken - jpeg");
        }




    };

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            showToast("Done capturing all photos!");
            return;
        }
        showToast("No camera detected!");
    }

    /**
     * Displaying the pictures taken.
     */
    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {

        if (pictureData != null && pictureUrl != null) {

            runOnUiThread(() -> {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                if (pictureUrl.contains("0_pic.jpg")) {
                    uploadBackPhoto.setImageBitmap(scaled);
                    //imgObject.setImageBitmap(scaled);
                    Bitmap bitmap1 = ((BitmapDrawable)uploadBackPhoto.getDrawable()).getBitmap();
//                    String label="";
//                    List<Classifier> resultPredict = recognition.recognize(bitmap1);
//                    for (Classifier result: resultPredict) {
//                        label+= result.getTitle() + " ( "+ result.getConfidence() + " ) " + "\n";
//                    }
//
//                    Toast.makeText(MainActivity.this, label, Toast.LENGTH_SHORT).show();
//                    // Toast.makeText(MainActivity.this,imgFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//                    Log.v("Tamvan", label);
//                    if(label.indexOf("water")>0){
//                        send_to("bn");
//                    }else if(label.indexOf("bottle")>0) {
//                        send_to("bn");
//                    }else {
//                        send_to("tn");
//                    }

                    int pixel1 = bitmap1.getPixel(256,341);
                    int pixel2 = bitmap1.getPixel(200,341);
                    int pixel3 = bitmap1.getPixel(300,341);
                    int redValue = (Color.red(pixel1)+Color.red(pixel2)+Color.red(pixel3))/3;
                    int blueValue = (Color.blue(pixel1)+Color.blue(pixel2)+Color.blue(pixel3))/3;
                    int greenValue = (Color.green(pixel1)+Color.green(pixel2)+Color.green(pixel3))/3;
                    float[] hsv = new float[3];
                    Color.RGBToHSV(redValue, greenValue, blueValue, hsv);
                    showToast("Ru: " + hsv[0] + " G: " + hsv[1] + " B: " + hsv[2] );
                    //showToast("Ru: " + redValue + " G: " + greenValue + " B: " + blueValue );
                    if(hsv[0] <= pref.getInt("data2", -1)){
                        teksColor.setText("Merah");
                        latar.setBackgroundColor(Color.RED);
                        send_to("mn");

                    } else if (hsv[0] <= pref.getInt("data1", -1)){
                        teksColor.setText("Brown");
                        latar.setBackgroundColor(Color.RED);
                        send_to("cn");
                    }
                    else if (hsv[0] <= pref.getInt("data3", -1)){
                        teksColor.setText("Kuning");
                        latar.setBackgroundColor(Color.YELLOW);
                        send_to("kn");
                    }
                    else if (hsv[0] <= pref.getInt("data4", -1)){
                        teksColor.setText("Hijau");
                        latar.setBackgroundColor(Color.GREEN);
                        send_to("hn");
                    }
                   else if (hsv[0] <= pref.getInt("data5", -1)){
                        teksColor.setText("Biru");
                        latar.setBackgroundColor(Color.BLUE);
                        send_to("bn");
                    }
                    else if (hsv[0] <= 360){
                        teksColor.setText("Ungu");
                        latar.setBackgroundColor(Color.MAGENTA);
                        send_to("un");
                    }
                    ////////////////////////////////////////

                    //
                } else if (pictureUrl.contains("1_pic.jpg")) {
                    // uploadFrontPhoto.setImageBitmap(scaled);
                    Toast.makeText(MainActivity.this, "aaaa", Toast.LENGTH_SHORT).show();
                }
            });
            // showToast("Picture saved to " + pictureUrl);
        }
    }

    public void send_to(String string){
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(string.getBytes());
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */

    private class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String data="";
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    data = (String) msg.obj;
                    mActivity.get().display.append(data);

                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
            showToast(data);
            if(data.contains("c")){
                //showToast("Starting capture!");
                foto();
                // pictureService.startCapturing((PictureCapturingListener) this);
            }

            if(data.contains("b")){
                //showToast("Starting capture!");
                buka();
                //sendL();
//                try {
//
//                } catch (IOException e) {
//                    Toast toast = Toast.makeText(getApplicationContext(), "gagal periksa koneksi", Toast.LENGTH_SHORT);
//                    toast.show();
//                }

                sound1.start();
                // pictureService.startCapturing((PictureCapturingListener) this);
            }
            if(data.contains("l")){
                //showToast("Starting capture!");
                //foto();
                logam();
                sound3.start();
                // pictureServi
                // ce.startCapturing((PictureCapturingListener) this);
            }
            if(data.contains("n")){
                //showToast("Starting capture!");
               // foto();
                nonlogam();
                sound2.start();
                // pictureService.startCapturing((PictureCapturingListener) this);
            }
            if(data.contains("t")){
                //showToast("Starting capture!");
                // foto();
                tutup();
                sound4.start();
                 }
            if(data.contains("L")){
                //showToast("Starting capture!");
                // foto();
                sendL();

            }

            if(data.contains("N")){

                sendN();
//
            }

            if(data.contains("p")){

                CapturePhoto();
//                foto();
//
            }
            if(data.contains("k")){

               kena();
               soundGagal.start();
//
            }
            if(data.contains("i")){

                finish();
                soundCoba.start();
//
            }

            if(data.contains("I")){

                coba();
//
            }

        }
    }
}