package com.example.bios90.voicedemo11;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import com.sac.speech.Speech;
import com.sac.speech.SpeechDelegate;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SpeechClass implements SpeechDelegate,Speech.stopDueToDelay
{
    private static final String TAG = "SpeechClass";

    Context ctx;

    public static SpeechDelegate delegate;
    String text="";
    Locale locale;

    public SpeechClass(Context ctx)
    {
        this.ctx = ctx;
    }

    void startListen()
    {
        ((AudioManager) Objects.requireNonNull(ctx.getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);

        Toast.makeText(ctx, "Service Started", Toast.LENGTH_SHORT).show();
        Speech.init(ctx);
        delegate = this;
        Speech.getInstance().setListener(this);

        locale = new Locale("en");

        if (Speech.getInstance().isListening())
        {
            muteBeepSoundOfRecorder();
            Speech.getInstance().stopListening();
        } else
        {
            System.setProperty("rx.unsafe-disable", "True");
            try
            {
                Speech.getInstance().stopTextToSpeech();
                Speech.getInstance().setLocale(locale).startListening(null, this);
            } catch (Exception e)
            {

            }
            muteBeepSoundOfRecorder();
        }
    }

    @Override
    public void onSpecifiedCommandPronounced(String event)
    {
        try
        {
            Log.e(TAG, "onSpecifiedCommandPronounced: ");
            Speech.getInstance().setLocale(locale).startListening(null, this);
        }
        catch (Exception e)
        {

        }
    }

    @Override
    public void onStartOfSpeech()
    {
        Log.e(TAG, "onStartOfSpeech: " );
    }

    @Override
    public void onSpeechRmsChanged(float value)
    {

    }

    @Override
    public void onSpeechPartialResults(List<String> results)
    {
        Log.e(TAG, "onSpeechPartialResults: " );
    }

    @Override
    public void onSpeechResult(String result)
    {
        try
        {
            Log.e(TAG, "onSpeechResult: ");
            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
            Speech.getInstance().setLocale(locale).startListening(null, this);
        }
        catch (Exception e)
        {

        }
    }

    private void muteBeepSoundOfRecorder()
    {
        AudioManager amanager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (amanager != null)
        {
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }
}
