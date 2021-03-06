/*
*    Light Controller, to Control wifi LED Lighting
*    Copyright (C) 2014  Eliot Stocker
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class controlCommands {
    public static final int DISCOVERED_DEVICE = 111;
    public static final int LIST_WIFI_NETWORKS = 802;
    public static final int COMMAND_SUCCESS = 222;

    private UDPConnection UDPC;
    public int LastOn = -1;
    public boolean sleeping = false;
    private Context mContext;
    private boolean measuring = false;
    private boolean candling = false;
    public final int[] tolerance = new int[1];
    public SaveState appState = null;

    private boolean going_to_sleep = false;
    public boolean paused = false;
    public boolean[] looping = {false, false, false, false, false};
    public boolean[] overlapping = {false, false, false, false, false};
    public int[] current_brightness = {100, 100, 100, 100, 100};
    public float BrightnessPercent = 1f;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    public controlCommands(Context context, Handler handler) {
        UDPC = new UDPConnection(context, handler);
        mContext = context;
        tolerance[0] = 25000;
        appState = new SaveState(context);

        powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
    }

    public void killUDPC() {
        UDPC.destroyUDPC();
    }

    public void discover() {
        Log.d("discovery", "Start Discovery");
        try {
            UDPC.sendAdminMessage("AT+Q\r".getBytes());
            Thread.sleep(100);
            UDPC.sendAdminMessage("Link_Wi-Fi".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getWifiNetworks() {
        try {
            UDPC.sendAdminMessage("+ok".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+WSCAN\r\n".getBytes(), true);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setWifiNetwork(String SSID, String Security, String Type, String Password) {
        try {
            UDPC.sendAdminMessage("+ok".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage(("AT+WSSSID="+SSID+"\r").getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage(("AT+WSKEY="+Security+","+Type+","+Password+"\r\n").getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+WMODE=STA\r\n".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+Z\r\n".getBytes(), true);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setWifiNetwork(String SSID) {
        try {
            UDPC.sendAdminMessage("+ok".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage(("AT+WSSSID="+SSID+"\r").getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+WMODE=STA\r\n".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+Z\r\n".getBytes(), true);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void LightsOn(int zone) {
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                messageBA[0] = 66;
                break;
            case 1:
                messageBA[0] = 69;
                break;
            case 2:
                messageBA[0] = 71;
                break;
            case 3:
                messageBA[0] = 73;
                break;
            case 4:
                messageBA[0] = 75;
                break;
            case 5:
                messageBA[0] = 56;
                break;
            case 6:
                messageBA[0] = 61;
                break;
            case 7:
                messageBA[0] = 55;
                break;
            case 8:
                messageBA[0] = 50;
                break;
            case 9:
                messageBA[0] = 53;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        LastOn = zone;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        appState.setOnOff(zone, true);
    }

    public void LightsOff(int zone) {
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                messageBA[0] = 65;
                break;
            case 1:
                messageBA[0] = 70;
                break;
            case 2:
                messageBA[0] = 72;
                break;
            case 3:
                messageBA[0] = 74;
                break;
            case 4:
                messageBA[0] = 76;
                break;
            case 5:
                messageBA[0] = 59;
                break;
            case 6:
                messageBA[0] = 51;
                break;
            case 7:
                messageBA[0] = 58;
                break;
            case 8:
                messageBA[0] = 54;
                break;
            case 9:
                messageBA[0] = 57;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
        appState.setOnOff(zone, false);
    }

    public void setToWhite(int zone) {
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                messageBA[0] = (byte)194;
                break;
            case 1:
                messageBA[0] = (byte)197;
                break;
            case 2:
                messageBA[0] = (byte)199;
                break;
            case 3:
                messageBA[0] = (byte)201;
                break;
            case 4:
                messageBA[0] = (byte)203;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
        appState.removeColor(zone);
    }

    public void setBrightnessUpOne() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 60;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setBrightnessDownOne() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 52;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setWarmthUpOne() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 62;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setWarmthDownOne() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 63;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setToFull(int zone) {
        LightsOn(zone);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 5:
                messageBA[0] = (byte)184;
                break;
            case 6:
                messageBA[0] = (byte)189;
                break;
            case 7:
                messageBA[0] = (byte)183;
                break;
            case 8:
                messageBA[0] = (byte)178;
                break;
            case 9:
                messageBA[0] = (byte)181;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setColorToNight(int zone) {
        LightsOff(zone);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                    messageBA[0] = (byte)193;
                    break;
            case 1:
                    messageBA[0] = (byte)198;
                    break;
            case 2:
                    messageBA[0] = (byte)200;
                    break;
            case 3:
                    messageBA[0] = (byte)202;
                    break;
            case 4:
                    messageBA[0] = (byte)204;
                    break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }
    public void setToNight(int zone) {
        LightsOn(zone);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 5:
                messageBA[0] = (byte)187;
                break;
            case 6:
                messageBA[0] = (byte)179;
                break;
            case 7:
                messageBA[0] = (byte)186;
                break;
            case 8:
                messageBA[0] = (byte)182;
                break;
            case 9:
                messageBA[0] = (byte)185;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    private int[] values = {2,3,4,5,8,9,10,11,13,14,15,16,17,18,19,20,21,23,24,25};
    private int LastBrightness = 20;
    private int LastZone = 0;
    private boolean finalSend = false;
    public boolean touching = false;
    public void setBrightness(int zoneid, int brightness) {
        if(brightness >= values.length) {
            brightness = values.length - 1;
        }
        if(brightness < 0) {
            brightness = 0;
        }
        if(!sleeping) {
            LightsOn(zoneid);
            byte[] messageBA = new byte[3];
            messageBA[0] = 78;
            messageBA[1] = (byte)(values[brightness]);
            messageBA[2] = 85;
            try {
                UDPC.sendMessage(messageBA);
            } catch (IOException e) {
                e.printStackTrace();
                //add alert to tell user we cant send command
            }
            appState.setBrighness(zoneid, brightness);
            if(finalSend) {
                finalSend = false;
            } else {
                sleeping = true;
                startTimeout();
            }
        }
        LastBrightness = brightness;
        LastZone = zoneid;
        current_brightness[zoneid] = (brightness + 1) * 5;
        BrightnessPercent = (float) brightness / (values.length - 1);
    }

    public void startTimeout() {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    sleep(100);
                    sleeping = false;
                    if(!touching) {
                        finalSend = true;
                        setBrightness(LastZone, LastBrightness);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void setColor(int zoneid, int color) {
        if(!sleeping) {
            float[] colors = new float[3];
            Color.colorToHSV(color, colors);
            Float deg = (float) Math.toRadians(-colors[0]);
            Float dec = (deg/((float)Math.PI*2f))*255f;
            if(LastOn != zoneid) {
                LightsOn(zoneid);
            }
            //rotation compensation
            dec = dec + 175;
            if(dec > 255) {
                dec = dec - 255;
            }

            byte[] messageBA = new byte[3];
            messageBA[0] = 64;
            messageBA[1] = (byte)dec.intValue();
            messageBA[2] = 85;
            try {
                UDPC.sendMessage(messageBA);
            } catch (IOException e) {
                e.printStackTrace();
                //add alert to tell user we cant send command
            }
            appState.setColor(zoneid, color);
            touching = true;
            sleeping = true;
            startTimeout();
        }
    }

    public void toggleDiscoMode(int zoneid) {
        LightsOn(zoneid);
        byte[] messageBA = new byte[3];
        messageBA[0] = 77;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void discoModeFaster() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 68;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void discoModeSlower() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 67;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    private String startCandleColor;
    private String endCandleColor;
    public void startCandleMode(final int zone) {
        candling = true;
        startCandleColor = "fffc00";
        endCandleColor = "ff4e00";

        final int startInt = Integer.parseInt(startCandleColor.substring(2,4),16);
        final int endInt = Integer.parseInt(endCandleColor.substring(2,4),16);

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while(candling) {
                        Random r = new Random();
                        String newColor = "#ff";
                        if(endInt - startInt == 0) {
                            newColor += Integer.toHexString(startInt);
                        } else {
                            if(endInt - startInt < 0) {
                                newColor += Integer.toHexString(r.nextInt(startInt - (endInt - startInt)));
                            } else {
                                newColor += Integer.toHexString(r.nextInt(endInt - startInt) + startInt);
                            }
                        }
                        if(newColor.length() < 5) {
                            newColor+="f";
                        }
                        newColor += "00";

                        try {
                            setColor(zone, Color.parseColor(newColor));
                        } catch(IllegalArgumentException e) {

                        }
                        int sleedTime = r.nextInt(150) + 50;
                        TimeUnit.MILLISECONDS.sleep(sleedTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void stopCandleMode() {
        candling = false;
    }

    private MediaRecorder mr;
    private FileOutputStream fd;
    private int[] strobeColors = new int[4];
    public void startMeasuringVol(final int zone) {
        strobeColors[0] = Color.parseColor("#FF7400");
        strobeColors[1] = Color.parseColor("#FFAA00");
        strobeColors[2] = Color.parseColor("#00FEFE");
        strobeColors[3] = Color.parseColor("#004DFE");
        measuring = true;
        try {
            fd = new FileOutputStream(new File(mContext.getCacheDir().getPath()+"/check"));
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mr.setOutputFile(fd.getFD());
            mr.prepare();
            mr.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while(measuring) {
                        if(getInputVolume() > tolerance[0]) {
                            i++;
                            if(i > 3) {
                                i = 0;
                            }
                            setColor(zone,strobeColors[i]);
                        }
                        TimeUnit.MILLISECONDS.sleep(50);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void stopMeasuringVol() {
        measuring = false;
        try {
            mr.stop();
            mr.reset();
            mr.release();
            fd.flush();
            fd.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private int getInputVolume() {
        try {
            //mr.getMaxAmplitude();
            int amplitude = mr.getMaxAmplitude();
            fd.flush();
            return amplitude;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void goToSleep(final int zoneid) {
        going_to_sleep = true;
        LightsOn(zoneid);
        wakeLock.acquire();

        Thread thread = new Thread(new Runnable()
        {
            public void run() {
                try {
                    int i = 0;

                    int current_zone = zoneid;

                    looping[current_zone] = true;

                    ArrayList<Integer> brightnesses = new ArrayList<Integer>();

                    int brightness_index = Math.min(19, Math.max(0, (int) Math.ceil(current_brightness[zoneid] / 5) - 1));

                    for(i = brightness_index; i > 0; i = i - 1) {
                        brightnesses.add(i);
                    }

                    final int split_interval = (int) 15000; // 5 minutes max if current brightness is 100%

                    for(Integer brightness: brightnesses){
                        if(!looping[current_zone]) {
                            going_to_sleep = false;
                            break;
                        }

                        try {
                            while(paused) {
                                TimeUnit.MILLISECONDS.sleep(10);
                            }

                            paused = true;
                            int last_on = LastOn; // We need to reset this after changing colors
                            if(last_on != current_zone) {
                                TimeUnit.MILLISECONDS.sleep(50);
                                LightsOn(current_zone);
                                TimeUnit.MILLISECONDS.sleep(50);
                            }

                            byte[] messageBA = new byte[3];
                            messageBA[0] = 78;
                            messageBA[1] = (byte)(values[brightness]);
                            messageBA[2] = 85;

                            try {
                                UDPC.sendMessage(messageBA);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if(last_on != current_zone) {
                                TimeUnit.MILLISECONDS.sleep(50);
                                LightsOn(last_on);
                                TimeUnit.MILLISECONDS.sleep(50);
                            }
                            paused = false;

                            current_brightness[current_zone] = (brightness + 1) * 5;

                        } catch(IllegalArgumentException e) {

                        }

                        TimeUnit.MILLISECONDS.sleep(split_interval);
                    }

                    while(paused) {
                        TimeUnit.MILLISECONDS.sleep(10);
                    }

                    paused = true;

                    TimeUnit.MILLISECONDS.sleep(100);

                    stopFadeEffect(current_zone, "night");

                    TimeUnit.MILLISECONDS.sleep(100);

                    LightsOff(current_zone);

                    paused = false;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void startFadeEffect(final int zoneid, final String effect) {
        if(effect == "gotosleep") {
            goToSleep(zoneid);
            return;
        }

        stopFadeEffect(zoneid);

        looping[zoneid] = true;
        LightsOn(zoneid);
        wakeLock.acquire();

        Thread thread = new Thread(new Runnable()
        {
            int fade_start;
            int fade_end = -1;
            int brightness_start;
            int brightness_end;
            int brightness;
            int prev_brightness_index = -1;
            int rand;

            int color_range_min = 120;
            int color_range_max = 210;
            int brightness_range_min = 10;
            int brightness_range_max = 100;
            int interval = 500;
            boolean smooth_brightness = true;
            int max_brightness_spread = 100;
            boolean overlap_effects = false;

            public void run() {
                try {
                    int i = 0;
                    if(effect == "aurora") {
                        color_range_min = 80;
                        color_range_max = 210;
                        brightness_range_min = 100;
                        brightness_range_max = 100;
                        interval = 300;
                        smooth_brightness = true;
                        max_brightness_spread = 0;
                        overlap_effects = false;
                    } else if (effect == "fire") {
                        color_range_min = 12;
                        color_range_max = 28;
                        brightness_range_min = 70;
                        brightness_range_max = 100;
                        interval = 100;
                        smooth_brightness = false;
                        max_brightness_spread = 100;
                        overlap_effects = false;
                    } else if (effect == "cherry") {
                        color_range_min = 270;
                        color_range_max = 300;
                        brightness_range_min = 95;
                        brightness_range_max = 100;
                        interval = 600;
                        smooth_brightness = true;
                        max_brightness_spread = 10;
                        overlap_effects = false;
                    } else if (effect == "forest") {
                        color_range_min = 55;
                        color_range_max = 130;
                        brightness_range_min = 80;
                        brightness_range_max = 100;
                        interval = 200;
                        smooth_brightness = true;
                        overlap_effects = false;
                    } else if (effect == "sunset") {
                        color_range_min = 15;
                        color_range_max = 45;
                        brightness_range_min = 100;
                        brightness_range_max = 100;
                        interval = 800;
                        smooth_brightness = true;
                        overlap_effects = false;
                    }

                    int current_zone = zoneid;

                    appState.setColor(zoneid, (int) ((color_range_min + color_range_max) / 2));

                    while(looping[current_zone]) {

                        rand = (int) Math.round(Math.random() * (color_range_max - color_range_min) + color_range_min);

                        // We create an array of colors
                        if(fade_end > 0) {
                            fade_start = fade_end;
                        } else {
                            fade_start = rand;
                        }

                        fade_end = rand;

                        if(fade_end == fade_start) {
                            fade_end = fade_end + 1;
                        }

                        int color_min = Math.min(fade_start, fade_end);
                        int color_max = Math.max(fade_start, fade_end);

                        int colors_length = (color_max - color_min + 1);

                        rand = (int) Math.round(Math.random() * (brightness_range_max - brightness_range_min) + brightness_range_min);

                        // We create an array of brightness levels
                        if(brightness_end > 0) {
                            brightness_start = brightness_end;
                        } else {
                            brightness_start = rand;
                        }

                        brightness_end = rand;

                        if(brightness_end == brightness_start) {
                            if(brightness_end < 100) {
                                brightness_end = brightness_end + 1;
                            } else {
                                brightness_start = brightness_start - 1;
                            }
                        }

                        if(smooth_brightness) {
                            max_brightness_spread = Math.min((int) Math.round(colors_length * 1.5), max_brightness_spread); // At most we want the brightness to fade 1.5 times as fast than colors

                            if (brightness_end > brightness_start) {
                                brightness_end = Math.min(brightness_end, brightness_start + max_brightness_spread);
                            } else {
                                brightness_end = Math.max(brightness_end, brightness_start - max_brightness_spread);
                            }
                        }

                        int brightness_min = Math.min(brightness_start, brightness_end);
                        int brightness_max = Math.max(brightness_start, brightness_end);

                        ArrayList<Integer> brightnesses = new ArrayList<Integer>();

                        float brightness_steps = (float) (brightness_max - brightness_min) / colors_length;

                        float b = (float) brightness_min;

                        ArrayList<Integer> colors = new ArrayList<Integer>();

                        for(i = color_min; i <= color_max; i += 1) {
                            colors.add(i);
                            brightnesses.add((int) Math.round(b));
                            b = Math.min(b + brightness_steps, (float) brightness_max);
                        }

                        if (fade_end < fade_start) {
                            Collections.reverse(colors);
                        }

                        if (brightness_end < brightness_start) {
                            Collections.reverse(brightnesses);
                        }


                        // We iterate through the color array and set colors + brightnesses
                        final int split_interval = interval / 2;

                        int index = 0;

                        if(overlap_effects && !overlapping[current_zone]) {
                            overlapping[current_zone] = true;
                            TimeUnit.MILLISECONDS.sleep(split_interval / 2);
                            startFadeEffect(current_zone, effect);
                        } else if (!overlap_effects) {
                            overlapping[current_zone] = false;
                        }

                        for(Integer color: colors){
                            if(!looping[current_zone]) {
                                break;
                            }

                            float hue = (float) color / 360;

                            // String newColor = HSBtoRGB(hue, 1.0f, 1.0f);

                            // String newColor = "#" + hsvToRgb((float) color, 1, 1);

                            try {
                                // setColor(current_zone, Color.parseColor(newColor));



                                Float deg = (float) Math.toRadians(-color);
                                Float dec = (deg/((float)Math.PI*2f))*255f;
                                //rotation compensation
                                dec = dec + 175;
                                if(dec > 255) {
                                    dec = dec - 255;
                                }

                                while(paused) {
                                    TimeUnit.MILLISECONDS.sleep(10);
                                }

                                paused = true;
                                int last_on = LastOn; // We need to reset this after changing colors
                                if(last_on != current_zone) {
                                    TimeUnit.MILLISECONDS.sleep(50);
                                    LightsOn(current_zone);
                                    TimeUnit.MILLISECONDS.sleep(50);
                                }

                                byte[] messageBA = new byte[3];
                                messageBA[0] = 64;
                                messageBA[1] = (byte) (float) (dec);
                                messageBA[2] = 85;

                                try {
                                    UDPC.sendMessage(messageBA);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    //add alert to tell user we cant send command
                                }

                                if(last_on != current_zone) {
                                    TimeUnit.MILLISECONDS.sleep(50);
                                    LightsOn(last_on);
                                    TimeUnit.MILLISECONDS.sleep(50);
                                }
                                paused = false;

                            } catch(IllegalArgumentException e) {

                            }

                            TimeUnit.MILLISECONDS.sleep(split_interval);

                            try {
                                int brightness_index = prev_brightness_index;

                                if(brightnesses.get(index) > 0) {
                                    brightness = (int) Math.round(BrightnessPercent * brightnesses.get(index)); // Brightness values are 1-100
                                    current_brightness[current_zone] = brightness;
                                    brightness_index = (int) Math.round(brightness / 5f) - 1;
                                }

                                if(prev_brightness_index != brightness_index && !going_to_sleep) {
                                    while(paused) {
                                        TimeUnit.MILLISECONDS.sleep(10);
                                    }

                                    paused = true;
                                    int last_on = LastOn; // We need to reset this after changing colors
                                    if(last_on != current_zone) {
                                        TimeUnit.MILLISECONDS.sleep(50);
                                        LightsOn(current_zone);
                                        TimeUnit.MILLISECONDS.sleep(50);
                                    }

                                    byte[] messageBA = new byte[3];
                                    messageBA[0] = 78;
                                    messageBA[1] = (byte)(values[brightness_index]);
                                    messageBA[2] = 85;

                                    try {
                                        UDPC.sendMessage(messageBA);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if(last_on != current_zone) {
                                        TimeUnit.MILLISECONDS.sleep(50);
                                        LightsOn(last_on);
                                        TimeUnit.MILLISECONDS.sleep(50);
                                    }
                                    paused = false;
                                }

                                prev_brightness_index = brightness_index;

                            } catch(IllegalArgumentException e) {

                            }

                            TimeUnit.MILLISECONDS.sleep(split_interval);

                            index = index + 1;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void stopFadeEffect(final int zoneid) {
        stopFadeEffect(zoneid, "white");
    }

    public void stopFadeEffect(final int zoneid, final String fade_to) {
        if(looping[zoneid] == true) {
            wakeLock.release();
            looping[zoneid] = false;
            overlapping[zoneid] = false;
            going_to_sleep = false;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(200);

            if(fade_to == "night") {
                setToNight(zoneid);
            } else if(fade_to == "white") {
                setToWhite(zoneid);

                TimeUnit.MILLISECONDS.sleep(200);

                int brightness_index = Math.min(19, Math.max(0, (int) Math.ceil(current_brightness[zoneid] / 5) - 1));

                byte[] messageBA = new byte[3];
                messageBA[0] = 78;
                messageBA[1] = (byte)(values[brightness_index]);
                messageBA[2] = 85;

                try {
                    UDPC.sendMessage(messageBA);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch(InterruptedException e) {

        }
    }
}