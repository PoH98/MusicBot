/*
 * Copyright 2021 John Grosh <john.a.grosh@gmail.com>.
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class RequestMetadata
{
    public static final RequestMetadata EMPTY = new RequestMetadata((User) null, null);
    
    public final UserInfo user;
    public final RequestInfo requestInfo;
    
    public RequestMetadata(User user, RequestInfo requestInfo)
    {
        this.user = user == null ? null : new UserInfo(user.getIdLong(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
        this.requestInfo = requestInfo;
    }

    public RequestMetadata(AudioTrack track, CommandEvent event)
    {
        User user = event.getAuthor();
        this.user = new UserInfo(user.getIdLong(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
        this.requestInfo = new RequestInfo(event.getArgs(), track.getInfo().uri);
    }
    public long getOwner()
    {
        return user == null ? 0L : user.id;
    }



    public class RequestInfo
    {
        public final String query, url;

        public final long startTimestamp;
        private final Pattern youtubeTimestampPattern = Pattern.compile("youtu(?:\\.be|be\\..+)/.*\\?.*(?!.*list=)t=([\\dhms]+)");
        public RequestInfo(String query, String url)
        {
            this.url = url;
            this.query = query;
            this.startTimestamp = tryGetTimeStamp(query);
        }
        private RequestInfo(String query, String url, long startTimestamp)
        {
            this.url = url;
            this.query = query;
            this.startTimestamp = startTimestamp;
        }

        private long tryGetTimeStamp(String query){
            Matcher matcher = youtubeTimestampPattern.matcher(query);
            long timeStamp = matcher.find() ? TimeUtil.parseUnitTime(matcher.group(1)) : 0;
            return timeStamp;
        }
    }
    
    public class UserInfo
    {
        public final long id;
        public final String username, discrim, avatar;
        
        private UserInfo(long id, String username, String discrim, String avatar)
        {
            this.id = id;
            this.username = username;
            this.discrim = discrim;
            this.avatar = avatar;
        }
    }
}
