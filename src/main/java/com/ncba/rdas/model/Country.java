package com.ncba.rdas.model;

import java.util.List;

public class Country {
    private String isoCode;
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyIsoCode;
    private String flagUrl;
    private List<String> languages;

    public Country() {}

    public String getIsoCode() { return isoCode; }
    public String getName() { return name; }
    public String getCapitalCity() { return capitalCity; }
    public String getPhoneCode() { return phoneCode; }
    public String getContinentCode() { return continentCode; }
    public String getCurrencyIsoCode() { return currencyIsoCode; }
    public String getFlagUrl() { return flagUrl; }
    public List<String> getLanguages() { return languages; }

    public void setIsoCode(String v) { this.isoCode = v; }
    public void setName(String v) { this.name = v; }
    public void setCapitalCity(String v) { this.capitalCity = v; }
    public void setPhoneCode(String v) { this.phoneCode = v; }
    public void setContinentCode(String v) { this.continentCode = v; }
    public void setCurrencyIsoCode(String v) { this.currencyIsoCode = v; }
    public void setFlagUrl(String v) { this.flagUrl = v; }
    public void setLanguages(List<String> v) { this.languages = v; }
}
