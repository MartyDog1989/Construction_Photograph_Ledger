package jp.martydog.construction_photograph_ledger;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.internal.Table;

/**
 * Created by Akihiro on 2017/10/13.
 */

public class Migration implements RealmMigration {

    /*
    version 0
    class Picture
        String text
        byte[] bitmapArray
        int id

     version 1
     class Picture
        add
        String site
     */


    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            RealmObjectSchema pictureSchema = schema.get("Picture");

            pictureSchema.addField("site", String.class, FieldAttribute.REQUIRED);
            oldVersion++;
        }

        // Migrate from version 1 to version 2

    }

    private long getIndexForProperty(Table table, String name) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
