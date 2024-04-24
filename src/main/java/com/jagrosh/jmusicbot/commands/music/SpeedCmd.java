package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;

public class SpeedCmd extends MusicCommand {
    public SpeedCmd(Bot bot)
    {
        super(bot);
        this.name = "speed";
        this.help = "Tweak the playback speed of the bot";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
    }
    @Override
    public void doCommand(CommandEvent event) {
        String args = event.getArgs();
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        double speed = settings.getPlaybackSpeed();
        if (args.isEmpty()) {
            event.reply(" Current volume is `" + speed + "`");
            return;
        }
        double nspeed;
        try {
            nspeed = Double.parseDouble(event.getArgs());
        } catch (NumberFormatException e) {
            nspeed = -1;
        }
        if (nspeed < 0.25 || nspeed > 2)
            event.reply(event.getClient().getError() + " Volume must be a valid integer between 0.25 and 2!");
        else {
            settings.setPlaybackSpeed(nspeed);
            StringBuilder sb = new StringBuilder();
            sb.append(" Playback speed set from `" + speed + "` to `" + nspeed + "`");
            if(bot.getPlayerManager().hasHandler(event.getGuild())){
                sb.append("\nSpeed will be applied after stopped playing.");
            }
            event.reply(sb.toString());
        }
    }
}
