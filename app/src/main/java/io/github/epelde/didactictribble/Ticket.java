package io.github.epelde.didactictribble;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by epelde on 29/12/2015.
 */
public class Ticket implements Serializable {

    @SerializedName("CodigoTicket")
    private String code;

    @SerializedName("FechaHora")
    private Date date;

    @SerializedName("NombreComercio")
    private String businessName;

    @SerializedName("DireccionComercio")
    private String businessAddress;

    @SerializedName("DescripcionOferta")
    private String description;

    @SerializedName("UrlQr")
    private String qrCodeUrl;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
