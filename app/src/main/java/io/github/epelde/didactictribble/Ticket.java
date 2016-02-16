package io.github.epelde.didactictribble;

import com.google.gson.annotations.SerializedName;

/**
 * Created by epelde on 03/02/2016.
 */
public class Ticket {

    @SerializedName("FechaHora")
    private String date;

    @SerializedName("NombreComercio")
    private String name;

    @SerializedName("DireccionComercio")
    private String address;

    @SerializedName("DescripcionOferta")
    private String description;

    @SerializedName("CodigoTicket")
    private String code;

    @SerializedName("UrlQr")
    private String codeURL;

    private byte[] imageFile;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeURL() {
        return codeURL;
    }

    public void setCodeURL(String codeURL) {
        this.codeURL = codeURL;
    }

    public byte[] getImageFile() {
        return imageFile;
    }

    public void setImageFile(byte[] imageFile) {
        this.imageFile = imageFile;
    }
}
