package org.eyeseetea.malariacare.layout.utils;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

/**
 * Created by idelcano on 01/11/2016.
 */

public class LayoutUtils extends BaseLayoutUtils {

    public static void setActionBar(android.support.v7.app.ActionBar actionBar) {
        Program program = Program.getFirstProgram();
        if (program != null && !PreferencesState.getInstance().getOrgUnit().equals("")) {
            LayoutUtils.setActionBarWithOrgUnit(actionBar);
        } else {
            LayoutUtils.setActionBarLogo(actionBar);
        }
    }

    public static void setTabHosts(DashboardActivity dashboardActivity) {
        dashboardActivity.setTabHostsWithText();
    }

    public static void setDivider(DashboardActivity dashboardActivity) {
        return;
    }

}
