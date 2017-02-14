package org.eyeseetea.malariacare.database.migrations;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.utils.populatedb.TreatmentTable;

import java.io.IOException;

@Migration(version = 27, databaseName = AppDatabase.NAME)
public class Migration27GenerateTreatmentFromTable extends BaseMigration {
    private static String TAG = ".Migration25";

    private static Migration27GenerateTreatmentFromTable instance;
    private boolean postMigrationRequired;

    public Migration27GenerateTreatmentFromTable() {
        instance = this;
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
    public void migrate(SQLiteDatabase database) {
        postMigrationRequired = true;
    }
}
