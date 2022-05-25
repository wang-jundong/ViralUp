package com.qboxus.musictok.Models;

import java.io.Serializable;

public class MultipleAccountModel implements Serializable {

    String id,fName,lName,uName,uBio,uLink,gender,uPic,uWallet,uPayoutId,authToken,phoneNo,email,socialId
            ,verified,applyVerification;
    boolean isLogin,isCheck;


    public MultipleAccountModel() {
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getApplyVerification() {
        return applyVerification;
    }

    public void setApplyVerification(String applyVerification) {
        this.applyVerification = applyVerification;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuBio() {
        return uBio;
    }

    public void setuBio(String uBio) {
        this.uBio = uBio;
    }

    public String getuLink() {
        return uLink;
    }

    public void setuLink(String uLink) {
        this.uLink = uLink;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getuPic() {
        return uPic;
    }

    public void setuPic(String uPic) {
        this.uPic = uPic;
    }

    public String getuWallet() {
        return uWallet;
    }

    public void setuWallet(String uWallet) {
        this.uWallet = uWallet;
    }

    public String getuPayoutId() {
        return uPayoutId;
    }

    public void setuPayoutId(String uPayoutId) {
        this.uPayoutId = uPayoutId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
}