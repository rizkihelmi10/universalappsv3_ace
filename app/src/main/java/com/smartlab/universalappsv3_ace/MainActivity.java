package com.smartlab.universalappsv3_ace;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    //private String systemUrl = "https://corporate.usm.my/booking/";

    private String systemUrl = " https://aceedventure.smartlab.com/";
    WebView myWebView;
    Dbhelper DB;

    Button buttonnewpage;
    ProgressDialog progressDialog;
    private static ValueCallback<Uri[]> mUploadMessageArr;
    private String notytoken;
    private boolean isLoggedIn = false;
    private boolean checkLO = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        askNotificationPermission();
        // Get the Firebase token and store it as a String variable
        getFirebaseToken(new FirebaseTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                notytoken = token;
                Log.d("Firebase T", "Firebase token: " + notytoken);
            }
        });

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        progressDialog=new ProgressDialog(MainActivity.this); //replace CONTEXT with YOUR_ACTIVITY_NAME.CLASS`
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading..."); //you can set your custom message here
        progressDialog.show();
        myWebView=(WebView) findViewById(R.id.webview);
        WebSettings webSettings=myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(new WebView(this).getSettings().getUserAgentString());
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);

        //myWebView.getSettings().setJavaScriptEnabled(true); // true/false to enable disable JavaScript support
        //myWebView.getSettings().setUserAgentString(new WebView(this).getSettings().getUserAgentString()); //set default user agent as of Chrome
        myWebView.setWebViewClient(new WebViewClient()); //we would be overriding WebViewClient() with custom methods

        myWebView.setWebChromeClient(new chromeView()); //we would be overriding WebChromeClient() with custom methods.

        //myWebView.setWebChromeClient(new chromeView());
        myWebView.loadUrl(systemUrl);

        myWebView.addJavascriptInterface(new MyJavascriptInterface (this), "android");
        myWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                progressDialog.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

        });

        //  openExternalWebView();
    }

   /* private void openExternalWebView() {
        buttonnewpage = (Button) findViewById(R.id.button);
        buttonnewpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.samsung.com/my/"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.android.chrome");
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    // Chrome is probably not installed
                    // Try with the default browser
                    i.setPackage(null);
                    startActivity(i);
                }
            }
        });
    }*/

    class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressDialog.show(); //showing the progress bar once the page has started loading
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressDialog.dismiss(); // hide the progress bar once the page has loaded
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            myWebView.loadData("","text/html","utf-8"); // replace the default error page with plan content
            progressDialog.dismiss(); // hide the progress bar on error in loading
            Toast.makeText(getApplicationContext(),"Internet issue",Toast.LENGTH_SHORT).show();


        }
    }

    public  class chromeView extends WebChromeClient{
        @SuppressLint("NewApi")
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
            return MainActivity.this.startFileChooserIntent(valueCallback, fileChooserParams.createIntent());
        }
    }

    @SuppressLint({"NewApi", "RestrictedApi"})
    public boolean startFileChooserIntent(ValueCallback<Uri[]> valueCallback, Intent intent) {
        if (mUploadMessageArr != null) {
            mUploadMessageArr.onReceiveValue(null);
            mUploadMessageArr = null;
        }
        mUploadMessageArr = valueCallback;
        try {
            startActivityForResult(intent, 1001, new Bundle());
            return true;
        } catch (Throwable valueCallback2) {
            valueCallback2.printStackTrace();
            if (mUploadMessageArr != null) {
                mUploadMessageArr.onReceiveValue(null);
                mUploadMessageArr = null;
            }
            return Boolean.parseBoolean(null);
        }
    }
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1001 && Build.VERSION.SDK_INT >= 21) {
            mUploadMessageArr.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(i2, intent));
            mUploadMessageArr = null;
        }
    }
    @Override
    public void onBackPressed() {
        if(myWebView.canGoBack()){
            myWebView.goBack();
        } else {
            finish();
        }
    }
    private void processData(String myVars) {
        //PARSE JSON data
        try {
            JSONObject jObject = new JSONObject(myVars);
            String tName = jObject.getString("Name");
            String tUser = jObject.getString("User");
            String tPass = jObject.getString("Pass");
            String tDate = jObject.getString("Signed");
            Toast.makeText(this, "User:"+tUser,Toast.LENGTH_LONG).show();

            if(tUser!="" && tPass!="") {
                Boolean loginUser = DB.insertData(tName,tUser,tPass,tDate);
                if(loginUser==true) {
                    isLoggedIn = true;
                    Toast.makeText(MainActivity.this, "Login Saved:"+tUser,Toast.LENGTH_LONG).show();
                    //processaccuser.php
                    AuthUser(tUser,tPass);
                } else {
                    Toast.makeText(MainActivity.this, "Login error:"+tUser,Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException e) {
            //some exception handler code.
            //nama.setText("JSON ERROR");
            e.printStackTrace();
        }

    }
    private void commenceLogout(String tUser) {
        checkLO = DB.deletedata(tUser);
        if(checkLO == true) {
            isLoggedIn = false;
            myWebView.setWebViewClient(new android.webkit.WebViewClient());
            myWebView.loadUrl(systemUrl);
        }
        Toast.makeText(MainActivity.this, "Logout:"+tUser+" loggedin:"+checkLO,Toast.LENGTH_LONG).show();
    }
    private void checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage("You need to enable your GPS location");
            dialog.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }
    private void AuthUser(String tUser, String tPass) {
        myWebView.setWebViewClient(new android.webkit.WebViewClient());
        myWebView.loadUrl(systemUrl+"processaccuser.php?Mobile=android&User="+tUser+"&Pass="+tPass);
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    public void getFirebaseToken(FirebaseTokenCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            Log.d("tokennn:", token);
                            Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                            callback.onTokenReceived(token);
                        } else {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        }
                    }
                });
    }
    public interface FirebaseTokenCallback {
        void onTokenReceived(String token);
    }


    public class MyJavascriptInterface {
        private Context context;
        public MyJavascriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public String getFirebaseToken() {
            return notytoken;
        }

        @JavascriptInterface
        public void invokeExternalChromeWebview(String url) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setPackage("com.android.chrome");
            try {
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                // Chrome is probably not installed
                // Try with the default browser
                i.setPackage(null);
                startActivity(i);
            }
        }
        public void showToast(String toast) {
          //  Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }

        /** retrieve the ids */
        @JavascriptInterface
        public void getVars(final String myVars) {

            //Do somethings with the Ids
            //showToast(myVars);
            if(isLoggedIn == false) {
                processData(myVars);
            }
        }

        @JavascriptInterface
        public void logout(String tUser) {
            Toast.makeText(MainActivity.this, "Logout:"+tUser+" loggedin:"+checkLO,Toast.LENGTH_LONG).show();
            if(isLoggedIn == true) {
                commenceLogout(tUser);
            }
        }

        @JavascriptInterface
        public String choosePhoto(String A, String F,String thisURL) {
            //AlertDialogPic(A, F,thisURL);

            return l;
        }

        // TODO Auto-generated method stub
        String l = "";

        @JavascriptInterface
        public String getGPSCoor() {
            String l = "";
            // TODO Auto-generated method stub
            checkGPSStatus();

          //  l = LongLatitude();

            return l;
        }
    }

}
