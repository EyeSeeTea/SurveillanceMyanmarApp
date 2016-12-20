package org.eyeseetea.malariacare.domain.usecase;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.views.TextCard;

import java.util.HashMap;

/**
 * Created by idelcano on 15/12/2016.
 */

public class ReviewUseCase {

    public static View createViewRow(ViewGroup rowView, Value value) {

        //Sets the value text in the row and add the question as tag.
        TextCard questionTextCard = (TextCard) rowView.findViewById(R.id.review_title_text);
        questionTextCard.setText((value.getQuestion() != null) ?
                value.getQuestion().getInternationalizedCodeDe_Name() + ": "
                : "");
        if (questionTextCard.getText().equals("")) {
            questionTextCard.setVisibility(View.GONE);
        }
        TextCard valueTextCard = (TextCard) rowView.findViewById(R.id.review_content_text);
        valueTextCard.setText(
                (value.getOption() != null) ? value.getOption().getInternationalizedCode()
                        : value.getValue());
        if ((value.getQuestion() != null)) {
            rowView.setTag(value.getQuestion());

            //Adds click listener to hide the fragment and go to the clicked question.
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Question question = (Question) v.getTag();
                    DashboardActivity.dashboardActivity.hideReview(question);
                }
            });

            HashMap<Tab, Integer> tabs = Session.getSurvey().getAnsweredTabs();
            if (tabs.get(value.getQuestion().getHeader().getTab()) % 2 == 0) {
                rowView.setBackgroundColor(
                        Color.parseColor("#9c7f9b"));
            }

        }
        return rowView;
    }
}
