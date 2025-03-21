/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.audio;

import com.dunctebot.sourcemanagers.DuncteBotSources;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.jagrosh.jmusicbot.Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.entities.Guild;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlayerManager extends DefaultAudioPlayerManager
{
    private final Bot bot;
    private final static Logger LOGGER = LoggerFactory.getLogger(PlayerManager.class);
    public PlayerManager(Bot bot)
    {
        this.bot = bot;
    }

    public void init()
    {
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true, new Tv(), new TvHtml5Embedded(), new Web());
        yt.setPlaylistPageCount(10);
        if (bot.getConfig().useYoutubeOauth2())
        {
            String token = null;
            try
            {
                token = Files.readString(OtherUtil.getPath("youtubetoken.txt"));
            }
            catch (NoSuchFileException e)
            {
                /* ignored */
            }
            catch (IOException e)
            {
                LOGGER.warn("Failed to read YouTube OAuth2 token file: {}",e.getMessage());
            }
            do{
                try
                {
                    if(token != null){
                        yt.useOauth2(token, true);
                    }
                    else {
                        yt.useOauth2(null, false);
                    }
                    break;
                }
                catch (Exception e)
                {
                    LOGGER.warn("Failed to authorize with YouTube. Removing token file.", e);
                    try{
                        Files.deleteIfExists(OtherUtil.getPath("youtubetoken.txt"));
                    }
                    catch (IOException ex) {
                        /* ignored */
                    }
                }
            }
            while(true);
        }
        registerSourceManager(yt);

        registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        registerSourceManager(new BandcampAudioSourceManager());
        registerSourceManager(new VimeoAudioSourceManager());
        registerSourceManager(new TwitchStreamAudioSourceManager());
        registerSourceManager(new BeamAudioSourceManager());
        registerSourceManager(new GetyarnAudioSourceManager());
        registerSourceManager(new NicoAudioSourceManager());
        registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));

        AudioSourceManagers.registerLocalSource(this);

        DuncteBotSources.registerAll(this, "en-US");
    }

    public Bot getBot()
    {
        return bot;
    }

    public boolean hasHandler(Guild guild)
    {
        return guild.getAudioManager().getSendingHandler()!=null;
    }

    public AudioHandler setUpHandler(Guild guild)
    {
        AudioHandler handler;
        if(guild.getAudioManager().getSendingHandler()==null)
        {
            AudioPlayer player = createPlayer();
            player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
            var speed = bot.getSettingsManager().getSettings(guild).getPlaybackSpeed();
            player.setFilterFactory((track, format, output) ->
                    getBot().getEffectManager().setAudioEffect(speed, output, format)
            );
            player.setFrameBufferDuration(500);
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        }
        else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }
}