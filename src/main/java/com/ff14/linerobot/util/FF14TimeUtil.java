package com.ff14.linerobot.util;

import java.time.Instant;
import java.time.LocalTime;

public class FF14TimeUtil {

    /**
     * Converts Earth time (in seconds since the Unix epoch) to Eorzean time.
     *
     * @param earthEpochSeconds The Earth time in seconds since the Unix epoch.
     * @return LocalTime representing the equivalent time in Eorzea.
     */
    public static LocalTime convertEarthTimeToEorzeanTime(long earthEpochSeconds) {

        double eorzeanEpochHours = getEorzeanEpochHours(earthEpochSeconds);
        double eorzeanHours = (eorzeanEpochHours % 24);
        double eorzeanMinutes = (eorzeanHours - (int) eorzeanHours) * 60;

        return LocalTime.of(((int) (eorzeanHours)), ((int) (eorzeanMinutes)));
    }

    public static void main(String[] args) {
        System.out.println(convertEarthTimeToEorzeanTime(Instant.now().getEpochSecond()));

    }

    public static double getEorzeanEpochHours(long earthEpochSeconds){
        return  (double) earthEpochSeconds / 175;
    }
}
