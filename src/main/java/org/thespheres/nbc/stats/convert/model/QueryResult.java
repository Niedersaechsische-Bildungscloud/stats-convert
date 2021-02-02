/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert.model;

import java.util.List;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class QueryResult {

    public static final String ERROR_CONTINUE_WAIT = "Continue wait";

    private List<Data> data;
    private String error;

    public List<Data> getQueryData() {
        return data;
    }

    public String getError() {
        return error;
    }
    
    public boolean isContinueWait() {
        return error != null && error.equals(ERROR_CONTINUE_WAIT);
    }

}
