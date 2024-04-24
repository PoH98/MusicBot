package com.jagrosh.jmusicbot.audio;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;

import java.util.Collections;
import java.util.List;

public class EffectManager {
    private TimescalePcmAudioFilter audioSpeedManager;
    public List<AudioFilter> setAudioSpeedManager(double speed, UniversalPcmAudioFilter filter, AudioDataFormat format){
        audioSpeedManager = new TimescalePcmAudioFilter(filter, format.channelCount, format.sampleRate);
        audioSpeedManager.setSpeed(speed);
        return Collections.singletonList(audioSpeedManager);
    }

    public double getSpeed(){
        return audioSpeedManager.getSpeed();
    }
}
