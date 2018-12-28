package za.co.we_party.weparty.Models;

public class ClubModel {

    int clubID;
    String clubName;
    double latitude;
    double longitude;
    String suburb;
    String cuisine;
    String drinks;
    String logoUrl;
    String backgroungImgUrl;
    String type;
    String galleryUrl;

    public ClubModel(int clubID, String clubName, double latitude, double longitude, String suburb, String cuisine, String drinks, String logoUrl,String backgroungImgUrl, String type, String galleryUrl) {
        this.clubID = clubID;
        this.clubName = clubName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.suburb = suburb;
        this.cuisine = cuisine;
        this.drinks = drinks;
        this.logoUrl = logoUrl;
        this.backgroungImgUrl = backgroungImgUrl;
        this.type = type;
        this.galleryUrl = galleryUrl;
    }


    public int getClubID() {
        return clubID;
    }

    public String getClubName() {
        return clubName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getDrinks() {
        return drinks;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getBackgroungImgUrl() {
        return backgroungImgUrl;
    }

    public String getType() {
        return type;
    }

    public String getGalleryUrl() {
        return galleryUrl;
    }

    //empty constructor
    public ClubModel(){}
}
