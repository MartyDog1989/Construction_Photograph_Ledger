package jp.martydog.construction_photograph_ledger;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akihiro on 2017/09/12.
 */

public class Picture extends RealmObject implements Serializable {
    private String text;
    private byte[] bitmapArray;
    @PrimaryKey
    private int id;

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public byte[] getBitmapArray() {
        return bitmapArray;
    }
    public void setBitmapArray(byte[] bitmapArray) {
        this.bitmapArray = bitmapArray;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
