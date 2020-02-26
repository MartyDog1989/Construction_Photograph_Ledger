package jp.martydog.construction_photograph_ledger;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import io.realm.Realm;

/**
 * Created by Akihiro on 2017/09/13.
 */

public class PicApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
