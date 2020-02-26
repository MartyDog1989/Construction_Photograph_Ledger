package jp.martydog.construction_photograph_ledger;


import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Akihiro on 2017/10/17.
 */

public class RealmManager {
    public static Realm getRealm() {
        return Realm.getInstance(getConfig());
    }

    private static RealmConfiguration getConfig() {
        RealmConfiguration defaultConfig = new RealmConfiguration.Builder()
                .name("default1.realm")
                .schemaVersion(0)
                .migration(new Migration())
                .build();
        return defaultConfig;
    }
}
