package io.github.epelde.didactictribble;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by epelde on 12/02/2016.
 */
public class Results {

    @SerializedName("datos")
    private List<ResultData> data;

    public Results() {
        data = new ArrayList<>();
    }

    public List<ResultData> getData() {
        return data;
    }

    public void setData(List<ResultData> data) {
        this.data = data;
    }
}
