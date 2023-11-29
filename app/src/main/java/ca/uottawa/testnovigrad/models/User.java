package ca.uottawa.testnovigrad.models;

import java.io.Serializable;

public class User implements Serializable {

    private String uid;
    private String email;
    private String firstName;
    private String lastName;
    private String userAuthority;

    private String userCompany;

    public User(String email, String firstName, String lastName, String userAuthority){
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userAuthority = userAuthority;
    }

    public User(String uid, String email, String firstName, String lastName, String userAuthority){
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userAuthority = userAuthority;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserAuthority() {
        return userAuthority;
    }

    public void setUserAuthority(String userAuthority) {
        this.userAuthority = userAuthority;
    }

    public String getUserCompany() {
        return userCompany;
    }

    public void setUserCompany(String userCompany) {
        this.userCompany = userCompany;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userAuthority='" + userAuthority + '\'' +
                ", userCompany='" + userCompany + '\'' +
                '}';
    }
}
