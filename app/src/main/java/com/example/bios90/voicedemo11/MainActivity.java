package com.example.bios90.voicedemo11;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements RecognitionListener
{
    private static final String TAG = "MainActivity";
    WebView webView;
    TextView textView;

    ImageView imgLoc;
    RelativeLayout laLock;
    Boolean locked=false;

    Vibrator vibrator;

    SpeechClass speechClass;

    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";
    /* Keyword we are looking for to activate recognition */
    private static final String KEYPHRASE = "oh mighty computer";

    /* Recognition object */
    private SpeechRecognizer recognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        webView = findViewById(R.id.webView);
        textView = findViewById(R.id.tvForText);

        laLock = findViewById(R.id.laLock);
        laLock.setVisibility(View.GONE);
        laLock.setOnClickListener(null);
        imgLoc = findViewById(R.id.imgLock);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://bestbanksapp.ru/index.html");

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},123);
        }

        runRecognizerSetup();

    }

    private void runRecognizerSetup()
    {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>()
        {
            @Override
            protected Exception doInBackground(Void... params)
            {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Exception result)
            {
                if (result != null)
                {
                    System.out.println(result.getMessage());
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException
    {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Disable this line if you don't want recognizer to save raw
                // audio files to app's storage
                //.setRawLogDir(assetsDir)
                .getRecognizer();
        recognizer.addListener(this);
        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create your custom grammar-based search
        File menuGrammar = new File(assetsDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }


    void startRecognition()
    {
        speechClass=new SpeechClass(this);
        speechClass.startListen();
    }

    void stopRecognition()
    {

    }

    @Override
    public void onBeginningOfSpeech()
    {
        Log.e(TAG, "onBeginningOfSpeech: ");
    }

    @Override
    public void onEndOfSpeech()
    {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis)
    {
        Log.e(TAG, "onPartialResult: " );
        if (hypothesis == null)
        {
            return;
        }
        String text = hypothesis.getHypstr();
        Log.e(TAG, "onPartialResult: not Nul!!!!!" );
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        if (text.equals(KEYPHRASE))
        {
            switchSearch(MENU_SEARCH);
        }
        else if (text.equals("hello"))
        {
        System.out.println("Hello to you too!");
        }
        else if (text.equals("good morning"))
        {
        System.out.println("Good morning to you too!");
        }
        else
            {
                System.out.println(hypothesis.getHypstr());
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
//        if (text.equals(KEYPHRASE))
//            switchSearch(MENU_SEARCH);
//        else {
//            System.out.println(hypothesis.getHypstr());
//        }
    }

    @Override
    public void onResult(Hypothesis hypothesis)
    {
        Log.e(TAG, "onResult: " );
        if (hypothesis != null)
        {

        }
    }

    @Override
    public void onError(Exception e)
    {
        System.out.println(e.getMessage());
    }

    @Override
    public void onTimeout()
    {
        switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName)
    {
        recognizer.stop();
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
    }


    //region Custom JavaScript Interface
    private class WebAppInterface
    {
        Context mContext;

        WebAppInterface(Context c)
        {
            mContext = c;
        }

        @android.webkit.JavascriptInterface
        public void showToast(String str)
        {
            try
            {
                textView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        startRecognition();
                    }
                });
            } catch (Exception e)
            {

            }
        }

        @android.webkit.JavascriptInterface
        public void showToast1(String str)
        {
            try
            {
                textView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {

                    }
                });
            } catch (Exception e)
            {

            }
        }
    }
    //endregion
}
