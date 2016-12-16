package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableRow;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.views.TextCard;

import java.util.List;

/**
 * Created by idelcano on 13/10/2016.
 */

public class ReviewScreenAdapter extends BaseAdapter implements IDashboardAdapter {

    List<Value> items;
    private LayoutInflater lInflater;
    private Context context;
    private Integer headerLayout;
    private Integer subHeaderLayout;
    private Integer footerLayout;
    private Integer recordLayout;
    private String title;

    public ReviewScreenAdapter(List<Value> items, LayoutInflater inflater, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = inflater;
        this.headerLayout = R.layout.review_header;
        this.subHeaderLayout = R.layout.review_sub_header;
        this.recordLayout = R.layout.review_item_row;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void setItems(List items) {
        this.items = (List<Value>) items;
    }

    public Integer getSubHeaderLayout() {
        return this.subHeaderLayout;
    }

    public void setSubHeaderLayout(Integer subHeaderLayout) {
        this.subHeaderLayout = subHeaderLayout;
    }

    @Override
    public Integer getHeaderLayout() {
        return this.headerLayout;
    }

    @Override
    public Integer getFooterLayout() {
        return footerLayout;
    }

    @Override
    public Integer getRecordLayout() {
        return this.recordLayout;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void remove(Object item) {
        this.items.remove(item);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Value value = (Value) getItem(position);

        // Get the row layout
        TableRow rowView = (TableRow) this.lInflater.inflate(getRecordLayout(), parent, false);

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

            if (value.getOption() != null && value.getOption().getBackground_colour() != null) {
                rowView.setBackgroundColor(
                        Color.parseColor("#" + value.getOption().getBackground_colour()));
            }

        }
        return rowView;
    }
}
