package org.eyeseetea.malariacare.strategies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.eyeseetea.malariacare.BaseActivity;
import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.authentication.AuthenticationManager;
import org.eyeseetea.malariacare.data.database.utils.ExportData;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IAuthenticationManager;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;

public class BaseActivityStrategy extends ABaseActivityStrategy {

    static String TAG = "BaseActivityStrategy";
    private static final int MENU_ITEM_LOGOUT = 99;
    private static final int MENU_ITEM_LOGOUT_ORDER = 106;
    private static final int DUMP_REQUEST_TREATMENT = 109;
    private static final String TREATMENT_TABLE_FILE_NAME = "TreatmentTable.csv";

    LogoutUseCase mLogoutUseCase;
    IAuthenticationManager mAuthenticationManager;

    public BaseActivityStrategy(BaseActivity baseActivity) {
        super(baseActivity);
    }

    @Override
    public void onCreate() {
        mAuthenticationManager = new AuthenticationManager(mBaseActivity);
        mLogoutUseCase = new LogoutUseCase(mAuthenticationManager);
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ITEM_LOGOUT, MENU_ITEM_LOGOUT_ORDER,
                mBaseActivity.getResources().getString(R.string.app_logout));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case MENU_ITEM_LOGOUT:
                new AlertDialog.Builder(mBaseActivity)
                        .setTitle(mBaseActivity.getString(R.string.app_logout))
                        .setMessage(mBaseActivity.getString(R.string.dashboard_menu_logout_message))
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        logout();
                                    }
                                })
                        .setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create().show();
                break;

            case R.id.export_treatment_table:
                mBaseActivity.debugMessage("Export treatment table");
                Intent emailIntent = ExportData.dumpAssetFileAndSendToIntent(mBaseActivity,
                        TREATMENT_TABLE_FILE_NAME);
                if (emailIntent != null) {
                    mBaseActivity.startActivityForResult(emailIntent, DUMP_REQUEST_TREATMENT);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void logout() {
        AlarmPushReceiver.cancelPushAlarm(mBaseActivity);
        mLogoutUseCase.execute(new LogoutUseCase.Callback() {
            @Override
            public void onLogoutSuccess() {
                mBaseActivity.finishAndGo(LoginActivity.class);
            }

            @Override
            public void onLogoutError(String message) {
                Log.d(TAG, message);
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!PreferencesState.getInstance().isDevelopOptionActive()
                || !BuildConfig.developerOptions) {
            MenuItem item = menu.findItem(R.id.export_treatment_table);
            item.setVisible(false);
        }
    }
}
