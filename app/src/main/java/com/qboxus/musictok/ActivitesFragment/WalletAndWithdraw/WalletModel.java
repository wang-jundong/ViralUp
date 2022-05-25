package com.qboxus.musictok.ActivitesFragment.WalletAndWithdraw;

public class WalletModel {

    String image ;
    String coins ;
    String price ;

    public WalletModel(String image, String coins, String price) {
        this.image = image;
        this.coins = coins;
        this.price = price;
    }

    public WalletModel() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCoins() {
        return coins;
    }

    public void setCoins(String coins) {
        this.coins = coins;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
