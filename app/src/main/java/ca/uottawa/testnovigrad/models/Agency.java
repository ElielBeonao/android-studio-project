package ca.uottawa.testnovigrad.models;

import com.google.firebase.Timestamp;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.uottawa.testnovigrad.fwk.ApplicationUtils;

public class Agency {

    private String id;

    private String name;

    private String address;

//    @JsonAdapter(ApplicationUtils.DateDeserializer.class)
    private Timestamp openedAt;

//    @JsonAdapter(ApplicationUtils.DateDeserializer.class)
    private Timestamp closedAt;

    private List<ServiceDelivery> servicesDelivery;

    public Agency(){
        servicesDelivery = new ArrayList<>();
    }

    public Agency(String id, String name, String address, Timestamp openedAt, Timestamp closedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    public Agency(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public Agency(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Timestamp getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Timestamp openedAt) {
        this.openedAt = openedAt;
    }

    public Timestamp getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Timestamp closedAt) {
        this.closedAt = closedAt;
    }

    public List<ServiceDelivery> getServicesDelivery() {
        return servicesDelivery;
    }

    public void setServicesDelivery(List<ServiceDelivery> servicesDelivery) {
        this.servicesDelivery = servicesDelivery;
    }

    @Override
    public String toString() {
        return "Agency{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", openedAt=" + openedAt +
                ", closedAt=" + closedAt +
                ", servicesDelivery=" + servicesDelivery +
                '}';
    }
}
