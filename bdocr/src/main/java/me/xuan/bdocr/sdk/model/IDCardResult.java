package me.xuan.bdocr.sdk.model;

import android.text.TextUtils;

/**
 * Author: xuan
 * Created on 2019/10/23 15:08.
 * <p>
 * Describe:
 */
public class IDCardResult extends ResponseResult {
    private int direction;
    private int wordsResultNumber;
    private Word address;
    private Word idNumber;
    private Word birthday;
    private Word name;
    private Word gender;
    private Word ethnic;
    private String idCardSide;
    private String riskType;
    private String imageStatus;
    private Word signDate;
    private Word expiryDate;
    private Word issueAuthority;

    public IDCardResult() {
    }

    public int getDirection() {
        return this.direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getWordsResultNumber() {
        return this.wordsResultNumber;
    }

    public void setWordsResultNumber(int wordsResultNumber) {
        this.wordsResultNumber = wordsResultNumber;
    }

    public Word getAddress() {
        return this.address;
    }

    public void setAddress(Word address) {
        this.address = address;
    }

    public Word getIdNumber() {
        return this.idNumber;
    }

    public void setIdNumber(Word idNumber) {
        this.idNumber = idNumber;
    }

    public Word getBirthday() {
        return this.birthday;
    }

    public void setBirthday(Word birthday) {
        this.birthday = birthday;
    }

    public Word getName() {
        return this.name;
    }

    public void setName(Word name) {
        this.name = name;
    }

    public Word getGender() {
        return this.gender;
    }

    public void setGender(Word gender) {
        this.gender = gender;
    }

    public Word getEthnic() {
        return this.ethnic;
    }

    public void setEthnic(Word ethnic) {
        this.ethnic = ethnic;
    }

    public String getIdCardSide() {
        return this.idCardSide;
    }

    public void setIdCardSide(String idCardSide) {
        this.idCardSide = idCardSide;
    }

    public Word getSignDate() {
        return this.signDate;
    }

    public void setSignDate(Word signDate) {
        this.signDate = signDate;
    }

    public Word getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(Word expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Word getIssueAuthority() {
        return this.issueAuthority;
    }

    public void setIssueAuthority(Word issueAuthority) {
        this.issueAuthority = issueAuthority;
    }

    public String getRiskType() {
        return this.riskType;
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    public String getImageStatus() {
        return this.imageStatus;
    }

    public void setImageStatus(String imageStatus) {
        this.imageStatus = imageStatus;
    }

    public String toString() {
        if (TextUtils.isEmpty(this.idCardSide)) {
            return "";
        } else if (this.idCardSide.equals(IDCardParams.ID_CARD_SIDE_FRONT)) {
            return "IDCardResult front{direction=" + this.direction + ", wordsResultNumber=" + this.wordsResultNumber + ", address=" + this.address + ", idNumber=" + this.idNumber + ", birthday=" + this.birthday + ", name=" + this.name + ", gender=" + this.gender + ", ethnic=" + this.ethnic + '}';
        } else {
            return this.idCardSide.equals(IDCardParams.ID_CARD_SIDE_BACK) ? "IDCardResult back{, signDate=" + this.signDate + ", expiryDate=" + this.expiryDate + ", issueAuthority=" + this.issueAuthority + '}' : "";
        }
    }
}