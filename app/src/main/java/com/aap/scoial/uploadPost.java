package com.aap.scoial;

public class uploadPost {
    public String name;
    public String imageURL;
    public  String caption;
    public  String time;
    public String location;

    public uploadPost() {
    }

    public uploadPost(String name, String imageURL, String caption, String time, String location) {
        this.name = name;
        this.imageURL = imageURL;
        this.caption = caption;
        this.time = time;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
