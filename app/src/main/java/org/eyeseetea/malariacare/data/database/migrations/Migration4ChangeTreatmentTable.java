package org.eyeseetea.malariacare.data.database.migrations;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.utils.populatedb.TreatmentTable;

import java.io.IOException;

@Migration(version = 4, database = AppDatabase.class)
public class Migration4ChangeTreatmentTable extends BaseMigration {
    private static String TAG = ".Migration4";

    private static Migration4ChangeTreatmentTable instance;
    private boolean postMigrationRequired;

    public Migration4ChangeTreatmentTable() {
        instance = this;
        postMigrationRequired = false;
    }

    public static void postMigrate() {
        //Migration NOT required -> done
        Log.d(TAG, "Post migrate");
        if (!instance.postMigrationRequired) {
            return;
        }
        TreatmentTable treatmentTable = new TreatmentTable();
        try {
            treatmentTable.generateTreatmentMatrix();
        } catch (IOException e) {
            Log.e(TAG, "Error generating treatment Matrix " + e.getMessage());
            e.printStackTrace();
        }
        instance.postMigrationRequired = false;
    }


    @Override
    public void migrate(DatabaseWrapper database) {
        postMigrationRequired = true;
    }
}
