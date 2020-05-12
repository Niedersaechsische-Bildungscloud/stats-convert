/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert.model;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class APIData {

    @SerializedName("total")
    private int total;
    @SerializedName("limit")
    private int limit;
    @SerializedName("skip")
    private int skip;
    @SerializedName("data")
    private School[] schools;

    public int getTotal() {
        return total;
    }

    public int getLimit() {
        return limit;
    }

    public int getSkip() {
        return skip;
    }

    public School[] getSchools() {
        return schools;
    }
    
    
}
