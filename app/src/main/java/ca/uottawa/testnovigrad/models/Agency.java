package ca.uottawa.testnovigrad.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Agency {

    private String id;

    private String name;

    private String address;

    private Date openedAt;

    private Date closedAt;

    private List<ServiceDelivery> servicesDelivery;

    public Agency(){
        servicesDelivery = new ArrayList<>();
    }

    public Agency(String id, String name, String address, Date openedAt, Date closedAt) {
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

    public Date getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Date openedAt) {
        this.openedAt = openedAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
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
