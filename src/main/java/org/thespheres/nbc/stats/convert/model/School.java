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
public class School {

    @SerializedName("_id")
    private String id;
    @SerializedName("name")
    private String name;

    public School() {
    }

    public School(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
