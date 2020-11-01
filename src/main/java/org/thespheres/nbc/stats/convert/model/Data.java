/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert.model;

import com.google.gson.annotations.SerializedName;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class Data {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @SerializedName(value = "Sessions.schoolId", alternate = {"RawEvents.schoolId"})
    private String schoolId;
    @SerializedName(value = "Events.timeStamp", alternate = {"RawEvents.timeStamp"})
    private String timeStamp;
    @SerializedName("Sessions.count")
    private int count;
    @SerializedName(value = "Events.activeUsers", alternate = {"RawEvents.activeUsers"})
    private int activeUsers;
    @SerializedName("RawEvents.page")
    private String page;
    @SerializedName("RawEvents.count")
    private int eventsCount;

    public String getSchoolId() {
        return schoolId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public int getSessions() {
        return count;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public String getPage() {
        return page;
    }

    public int getEventsCount() {
        return eventsCount;
    }

}
