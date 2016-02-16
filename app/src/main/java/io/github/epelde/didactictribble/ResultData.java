package io.github.epelde.didactictribble;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by epelde on 12/02/2016.
 */
public class ResultData implements Serializable {

    @SerializedName("FechaHora")
    private String date;

    @SerializedName("OfertaValida")
    private String valid;

    @SerializedName("TextoError")
    private String error;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
