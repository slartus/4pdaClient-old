package org.softeg.slartus.forpdaapi;

/**
 * Created by slartus on 03.06.2014.
 */
public class FavTopic extends Topic {
    private String tid;// идентификатор избранного

    public FavTopic(){

    }
    public FavTopic(String id, String title) {
        super(id,title);
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    private String trackType;

    public String getTrackType() {
        return trackType;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }
}
