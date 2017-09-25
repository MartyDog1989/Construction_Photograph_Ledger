package jp.martydog.construction_photograph_ledger;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Akihiro on 2017/09/13.
 */

public class PicApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
