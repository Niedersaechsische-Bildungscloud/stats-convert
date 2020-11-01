/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class Config {

    @SerializedName("schools-file")
    private String schoolsFile;
    @SerializedName("api-url")
    private String api;
    @SerializedName("cubejs-url")
    private String cubeJS;
    @SerializedName("output-dir")
    private String outDir;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getSchoolsFile() {
        return schoolsFile;
    }

    public void setSchoolsFile(String schoolsFile) {
        this.schoolsFile = schoolsFile;
    }

    public String getCubeJS() {
        return cubeJS;
    }

    public void setCubeJS(String cubeJS) {
        this.cubeJS = cubeJS;
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

}
