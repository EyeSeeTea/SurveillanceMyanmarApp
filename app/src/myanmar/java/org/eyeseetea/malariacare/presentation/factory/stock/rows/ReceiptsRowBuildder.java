package org.eyeseetea.malariacare.presentation.factory.stock.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.presentation.factory.stock.utils.SurveyStock;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.Date;

public class ReceiptsRowBuildder extends CounterRowBuilder {
    public ReceiptsRowBuildder(Context context) {
        super(context.getResources().getString(R.string.receipts), context);
    }

    @Override
    protected int incrementCount(SurveyStock surveyStock, int newValue) {
        Survey survey = surveyStock.getSurvey();
        Date maxBalanceDate = Survey.getLastDateForSurveyType(Constants.SURVEY_RESET);
        if ((maxBalanceDate == null || Utils.dateGreaterOrEqualsThanDate(maxBalanceDate,
                survey.getEventDate())) && survey.getType().equals(Constants.SURVEY_RECEIPT)) {
            return newValue;
        }
        return 0;
    }
}
