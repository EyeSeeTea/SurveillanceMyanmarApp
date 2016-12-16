/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.survey;

import static org.eyeseetea.malariacare.R.id.question;
import static org.eyeseetea.malariacare.database.model.Option.DOESNT_MATCH_POSITION;
import static org.eyeseetea.malariacare.database.model.Option.MATCH_POSITION;
import static org.eyeseetea.malariacare.database.utils.Session.getSurvey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.domain.usecase.ToastUseCase;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.navigation.NavigationBuilder;
import org.eyeseetea.malariacare.layout.adapters.survey.navigation.NavigationController;
import org.eyeseetea.malariacare.layout.adapters.survey.strategies.DynamicTabAdapterStrategy;
import org.eyeseetea.malariacare.layout.adapters.survey.strategies.IDynamicTabAdapterStrategy;
import org.eyeseetea.malariacare.layout.listeners.SwipeTouchListener;
import org.eyeseetea.malariacare.layout.utils.BaseLayoutUtils;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.factory.IQuestionViewFactory;
import org.eyeseetea.malariacare.presentation.factory.MultiQuestionViewFactory;
import org.eyeseetea.malariacare.presentation.factory.SingleQuestionViewFactory;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.GradleVariantConfig;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.EditCard;
import org.eyeseetea.malariacare.views.TextCard;
import org.eyeseetea.malariacare.views.option.ImageRadioButtonOption;
import org.eyeseetea.malariacare.views.question.AKeyboardQuestionView;
import org.eyeseetea.malariacare.views.question.AOptionQuestionView;
import org.eyeseetea.malariacare.views.question.IImageQuestionView;
import org.eyeseetea.malariacare.views.question.IMultiQuestionView;
import org.eyeseetea.malariacare.views.question.IQuestionView;
import org.eyeseetea.malariacare.views.question.singlequestion.ImageRadioButtonSingleQuestionView;
import org.eyeseetea.malariacare.views.question.singlequestion.strategies
        .ConfirmCounterSingleCustomViewStrategy;
import org.eyeseetea.malariacare.views.question.singlequestion.strategies
        .ReminderSingleCustomViewStrategy;

import java.util.ArrayList;
import java.util.List;

import utils.ProgressUtils;

public class DynamicTabAdapter extends BaseAdapter implements ITabAdapter {

    private final static String TAG = ".DynamicTabAdapter";
    /**
     * Flag that indicates if the actual question option is clicked to prevent multiple clicks.
     */
    public static boolean isClicked;
    /**
     * Flag that indicates the number of failed validations by the active screen in multiquestion
     * tabs
     */
    public static int failedValidations;
    /**
     * Flag that indicates the number of failed validations by the active screen in multiquestion
     * tabs
     */
    public static View navigationButtonHolder;
    private final Context context;
    public NavigationController navigationController;
    public boolean reloadingQuestionFromInvalidOption;
    Tab tab;
    LayoutInflater lInflater;
    TableLayout tableLayout = null;
    int id_layout;
    /**
     * View needed to close the keyboard in methods with view
     */
    View keyboardView;
    List<IMultiQuestionView> mMultiQuestionViews = new ArrayList<>();
    IDynamicTabAdapterStrategy mDynamicTabAdapterStrategy;
    /**
     * Flag that indicates if the current survey in session is already sent or not (it affects
     * readonly settings)
     */
    private boolean readOnly;
    /**
     * Flag that indicates if the swipe listener has been already added to the listview container
     */
    private boolean isSwipeAdded;
    /**
     * Flag that indicates the number of failed validations by the active screen in multiquestion
     * tabs
     * Listener that detects taps on buttons & swipe
     */
    private SwipeTouchListener swipeTouchListener;
    private boolean mReviewMode = false;

    public DynamicTabAdapter(Tab tab, Context context, boolean reviewMode) {
        mReviewMode = reviewMode;
        this.lInflater = LayoutInflater.from(context);
        this.context = context;
        this.id_layout = R.layout.form_without_score;

        this.navigationController = initNavigationController(tab);
        this.readOnly = getSurvey() != null && !getSurvey().isInProgress();
        this.isSwipeAdded = false;
        //On create dynamictabadapter, if is not readonly and has value not null it should come
        // from reviewFragment
        if (!readOnly) {
            Question question = navigationController.getCurrentQuestion();
            if (question.getValueBySession() != null) {
                if (DashboardActivity.moveToQuestion != null) {
                    goToQuestion(DashboardActivity.moveToQuestion);
                    DashboardActivity.moveToQuestion = null;
                } else {
                    goToLastQuestion();
                }
            }
        }

        int totalPages = navigationController.getCurrentQuestion().getTotalQuestions();
        if (readOnly) {
            if (Session.getSurvey() != null) {
                totalPages = Session.getSurvey().getMaxTotalPages();
            }
        }
        navigationController.setTotalPages(totalPages);
        isClicked = false;

        mDynamicTabAdapterStrategy = new DynamicTabAdapterStrategy(this);
    }

    /**
     * Returns the option selected for the given question and boolean value or by position
     */
    public static Option findSwitchOption(Question question, boolean isChecked) {
        //Search option by position
        return question.getAnswer().getOptions().get((isChecked) ? 0 : 1);
    }

    /**
     * Returns the boolean selected for the given question (by boolean value or position option,
     * position 1=true 0=false)
     */
    public static Boolean findSwitchBoolean(Question question) {
        Value value = question.getValueBySession();
        if (value.getValue().equals(question.getAnswer().getOptions().get(0).getCode())) {
            return true;
        } else if (value.getValue().equals(question.getAnswer().getOptions().get(1).getCode())) {
            return false;
        }
        return false;
    }

    private NavigationController initNavigationController(Tab tab) {
        NavigationController navigationController = NavigationBuilder.getInstance().buildController(
                tab);
        navigationController.next(null);
        return navigationController;
    }

    public void addOnSwipeListener(final ListView listView) {
        if (isSwipeAdded) {
            return;
        }

        swipeTouchListener = new SwipeTouchListener(context) {
            /**
             * Click listener for image option
             * @param view
             */
            public void onClick(final View view) {
                if (isClicked) {
                    Log.d(TAG, "onClick ignored to avoid double click");
                    return;
                }

                isClicked = true;
                Log.d(TAG, "onClick");
                navigationController.isMovingToForward = true;
                final Option selectedOption = (Option) view.getTag();
                final Question question = navigationController.getCurrentQuestion();
                Question counterQuestion = question.findCounterByOption(selectedOption);
                if (counterQuestion == null) {
                    saveOptionAndMove(view, selectedOption, question);
                } else {
                    showConfirmCounter(view, selectedOption, question, counterQuestion);
                }
            }

            /**
             * Swipe right listener moves to previous question
             */
            public void onSwipeRight() {
                if (!GradleVariantConfig.isSwipeActionActive()) {
                    return;
                }
                Log.d(TAG, "onSwipeRight(previous)");
                //Hide keypad
                hideKeyboard(listView.getContext(), listView);
                previous();
            }

            /**
             * Swipe left listener moves to next question
             */
            public void onSwipeLeft() {
                if (!GradleVariantConfig.isSwipeActionActive()) {
                    return;
                }
                Log.d(TAG, "onSwipeLeft(next)");
                if (readOnly || navigationController.isNextAllowed()) {
                    //Hide keypad
                    hideKeyboard(listView.getContext(), listView);
                    next();
                }
            }

            /**
             * Adds a clickable view
             * @param view
             */
            public void addScrollView(ScrollView view) {
                super.addScrollView(scrollView);
                scrollView = view;
            }
        };

        listView.setOnTouchListener(swipeTouchListener);
    }

    private void showConfirmCounter(final View view, final Option selectedOption,
            final Question question, Question questionCounter) {

        ConfirmCounterSingleCustomViewStrategy confirmCounterStrategy =
                new ConfirmCounterSingleCustomViewStrategy(this);
        confirmCounterStrategy.showConfirmCounter(view, selectedOption, question, questionCounter);

        isClicked = false;
    }

    public void showStandardConfirmCounter(final View view, final Option selectedOption,
            final Question question,
            Question questionCounter) {
        //Change question x confirm message
        View rootView = view.getRootView();
        final TextCard questionView = (TextCard) rootView.findViewById(R.id.question);
        questionView.setText(questionCounter.getInternationalizedForm_name());
        ProgressUtils.setProgressBarText(rootView, "");
        //cancel
        ImageView noView = (ImageView) rootView.findViewById(R.id.confirm_no);
        noView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Leave current question as it was
                removeConfirmCounter(v);
                notifyDataSetChanged();
                isClicked = false;
            }
        });

        //confirm
        ImageView yesView = (ImageView) rootView.findViewById(R.id.confirm_yes);
        yesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationController.increaseCounterRepetitions(selectedOption);
                removeConfirmCounter(v);
                saveOptionAndMove(view, selectedOption, question);
            }
        });

        //Show confirm on full screen
        rootView.findViewById(R.id.no_scrolled_table).setVisibility(View.GONE);
        rootView.findViewById(R.id.scrolled_table).setVisibility(View.GONE);
        rootView.findViewById(R.id.confirm_table).setVisibility(View.VISIBLE);

        //Show question image in counter alert
        if (questionCounter.getPath() != null && !questionCounter.getPath().equals("")) {
            ImageView imageView = (ImageView) rootView.findViewById(R.id.questionImageRow);
            BaseLayoutUtils.putImageInImageView(questionCounter.getInternationalizedPath(),
                    imageView);
            imageView.setVisibility(View.VISIBLE);
        }

        //Question "header" is in the first option in Options.csv
        List<Option> questionOptions = questionCounter.getAnswer().getOptions();
        if (questionOptions.get(0) != null) {
            TextCard textCard = (TextCard) rootView.findViewById(R.id.questionTextRow);
            textCard.setText(questionOptions.get(0).getInternationalizedCode());
            textCard.setTextSize(questionOptions.get(0).getOptionAttribute().getText_size());
        }
        //Question "confirm button" is in the second option in Options.csv
        if (questionOptions.get(1) != null) {
            TextCard confirmTextCard = (TextCard) rootView.findViewById(R.id.textcard_confirm_yes);
            confirmTextCard.setText(questionOptions.get(1).getInternationalizedCode());
            confirmTextCard.setTextSize(questionOptions.get(1).getOptionAttribute().getText_size());
        }
        //Question "no confirm button" is in the third option in Options.csv
        if (questionOptions.get(2) != null) {
            TextCard noConfirmTextCard = (TextCard) rootView.findViewById(R.id.textcard_confirm_no);
            noConfirmTextCard.setText(questionOptions.get(2).getInternationalizedCode());
            noConfirmTextCard.setTextSize(questionOptions.get(
                    2).getOptionAttribute().getText_size());
        }
    }

    public void removeConfirmCounter(View view) {
        view.getRootView().findViewById(R.id.dynamic_tab_options_table).setVisibility(View.VISIBLE);
        view.getRootView().findViewById(R.id.confirm_table).setVisibility(View.GONE);
    }

    public void saveOptionAndMove(View view, Option selectedOption, Question question) {
        Value value = question.getValueBySession();
        //set new totalpages if the value is not null and the value change
        if (value != null && !readOnly) {
            navigationController.setTotalPages(question.getTotalQuestions());
        }

        ReadWriteDB.saveValuesDDL(question, selectedOption, value);

        if (question.getOutput().equals(Constants.IMAGE_3_NO_DATAELEMENT) ||
                question.getOutput().equals(Constants.IMAGE_RADIO_GROUP_NO_DATAELEMENT)) {
            switchHiddenMatches(question, selectedOption);
        }

        if (view instanceof ImageView) {
            darkenNonSelected(view, selectedOption);
            LayoutUtils.highlightSelection(view, selectedOption);
        }

        finishOrNext();
    }

    private void darkenNonSelected(View view, Option selectedOption) {
        swipeTouchListener.clearClickableViews();
        //A Warning or Reminder (not a real option)
        if (selectedOption == null) {
            return;
        }
        //A question with real options -> darken non selected
        ViewGroup vgTable = (ViewGroup) view.getParent().getParent();
        for (int rowPos = 0; rowPos < vgTable.getChildCount(); rowPos++) {
            ViewGroup vgRow = (ViewGroup) vgTable.getChildAt(rowPos);
            for (int itemPos = 0; itemPos < vgRow.getChildCount(); itemPos++) {
                View childItem = vgRow.getChildAt(itemPos);
                if (childItem instanceof ImageView) {
                    //We dont want the user to click anything else
                    Option otherOption = (Option) childItem.getTag();
                    if (selectedOption.getId_option() != otherOption.getId_option()) {
                        LayoutUtils.overshadow((FrameLayout) childItem);
                    }
                }
            }
        }
    }

    /**
     * switch the matches of a no dataelement question with his hidden dataelements.
     * Only applies to question with options and matches the option position (0)/(1) Match position
     * 1 no match position 0
     */
    public void switchHiddenMatches(Question question, Option option) {
        if (!question.hasOutputWithOptions() || (!question.getOutput().equals(
                Constants.IMAGE_3_NO_DATAELEMENT) && !question.getOutput().equals(
                Constants.IMAGE_RADIO_GROUP_NO_DATAELEMENT))) {
            return;
        }
        //Find QuestionOptions
        for (QuestionOption questionOption : question.getQuestionOption()) {
            if (questionOption.getMatch().getQuestionRelation().getOperation()
                    != QuestionRelation.MATCH) {
                continue;
            }

            Option matchOption = questionOption.getOption();
            Question matchQuestion = questionOption.getMatch().getQuestionRelation().getQuestion();

            switchHiddenMatch(question, option, matchQuestion, matchOption);
        }
    }

    private void switchHiddenMatch(Question question, Option option, Question matchQuestion,
            Option matchOption) {
        int optionPosition = (option.getCode().equals(matchOption.getCode())) ? MATCH_POSITION
                : DOESNT_MATCH_POSITION;

        ReadWriteDB.saveValuesDDL(matchQuestion,
                matchQuestion.getAnswer().getOptions().get(optionPosition),
                matchQuestion.getValueBySession());
    }


    public Tab getTab() {
        return this.tab;
    }

    @Override
    public BaseAdapter getAdapter() {
        return this;
    }

    @Override
    public int getLayout() {
        return id_layout;
    }

    @Override
    public Float getScore() {
        return 0F;
    }

    /**
     * No scores required
     */
    @Override
    public void initializeSubscore() {
    }

    @Override
    public String getName() {
        return tab.getName();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return this.navigationController.getCurrentQuestion();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mMultiQuestionViews.clear();
        //init validation control(used only in multiquestions tabs)
        failedValidations = 0;
        //Inflate the layout
        View rowView = lInflater.inflate(R.layout.dynamic_tab_grid_question, parent, false);

        rowView.getLayoutParams().height = parent.getHeight();
        rowView.requestLayout();
        Question questionItem = (Question) this.getItem(position);
        // We get values from DB and put them in Session
        if (getSurvey() != null) {
            getSurvey().getValuesFromDB();
        } else {
            //The survey in session is null when the user closes the surveyFragment, but the
            // getView is called.
            return convertView;
        }

        //Question
        TextCard headerView = (TextCard) rowView.findViewById(question);

        //Load a font which support Khmer character
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "fonts/" + context.getString(R.string.specific_language_font));
        headerView.setTypeface(tf);
        int tabType = questionItem.getHeader().getTab().getType();
        if (isMultipleQuestionTab(tabType)) {
            headerView.setText(questionItem.getHeader().getTab().getInternationalizedName());
        } else {
            headerView.setText(questionItem.getInternationalizedForm_name());
        }

        //question image
        if (questionItem.getPath() != null && !questionItem.getPath().equals("")
                && mDynamicTabAdapterStrategy.HasQuestionImageVisibleInHeader(
                questionItem.getOutput())) {
            ImageView imageView = (ImageView) rowView.findViewById(R.id.questionImage);
            BaseLayoutUtils.putImageInImageView(questionItem.getInternationalizedPath(), imageView);
            imageView.setVisibility(View.VISIBLE);
        }

        //Progress
        ProgressUtils.updateProgressBarStatus(rowView, navigationController.getCurrentPage(),
                navigationController.getCurrentTotalPages());

        TableRow tableRow = null;
        TableRow tableButtonRow = null;
        List<Question> screenQuestions = new ArrayList<>();

        swipeTouchListener.clearClickableViews();

        if (isTabScrollable(questionItem, tabType)) {
            tableLayout = (TableLayout) rowView.findViewById(R.id.multi_question_options_table);
            (rowView.findViewById(R.id.scrolled_table)).setVisibility(View.VISIBLE);
            (rowView.findViewById(R.id.no_scrolled_table)).setVisibility(View.GONE);
            screenQuestions = questionItem.getQuestionsByTab(questionItem.getHeader().getTab());
            swipeTouchListener.addScrollView((ScrollView) (rowView.findViewById(
                    R.id.scrolled_table)).findViewById(R.id.table_scroll));
        } else {
            tableLayout = (TableLayout) rowView.findViewById(R.id.dynamic_tab_options_table);
            (rowView.findViewById(R.id.no_scrolled_table)).setVisibility(View.VISIBLE);
            (rowView.findViewById(R.id.scrolled_table)).setVisibility(View.GONE);
            screenQuestions.add(questionItem);
        }
        //this method removes all the clicable views
        swipeTouchListener.clearClickableViews();
        navigationButtonHolder = rowView.findViewById(R.id.snackbar);
        if (GradleVariantConfig.isButtonNavigationActive()) {
            createNavigationButtonsBackButton(navigationButtonHolder);
            isClicked = false;
        }
        Log.d(TAG, "Questions in actual tab: " + screenQuestions.size());
        for (Question screenQuestion : screenQuestions) {

            IQuestionViewFactory questionViewFactory;

            if (isMultipleQuestionTab(tabType)) {
                questionViewFactory = new MultiQuestionViewFactory();
            } else {
                questionViewFactory = new SingleQuestionViewFactory();
            }

            // Se get the value from Session
            int visibility = View.GONE;
            if (!screenQuestion.isHiddenBySurveyAndHeader(getSurvey())
                    || !isMultipleQuestionTab(tabType)) {
                visibility = View.VISIBLE;
            }
            Value value = screenQuestion.getValueBySession();
            int typeQuestion = screenQuestion.getOutput();
            switch (typeQuestion) {
                case Constants.IMAGES_2:
                case Constants.IMAGES_4:
                case Constants.IMAGES_6:
                    List<Option> options = screenQuestion.getAnswer().getOptions();
                    for (int i = 0; i < options.size(); i++) {
                        Option currentOption = options.get(i);
                        int optionID = R.id.option2;
                        int counterID = R.id.counter2;
                        int mod = i % 2;
                        //First item per row requires a new row
                        if (mod == 0) {
                            tableRow = (TableRow) lInflater.inflate(R.layout.dynamic_tab_row,
                                    tableLayout, false);
                            tableLayout.addView(tableRow);
                            optionID = R.id.option1;
                            counterID = R.id.counter1;
                        }
                        //Add counter value if possible
                        addCounterValue(screenQuestion, currentOption, tableRow, counterID);

                        FrameLayout frameLayout = (FrameLayout) tableRow.getChildAt(mod);
                        TextCard textOption = (TextCard) frameLayout.getChildAt(1);
                        setTextSettings(textOption, currentOption);
                        frameLayout.setBackgroundColor(
                                Color.parseColor("#" + currentOption.getBackground_colour()));

                        initOptionButton(frameLayout, currentOption, value);
                    }
                    break;
                case Constants.IMAGES_3:
                case Constants.IMAGE_3_NO_DATAELEMENT:
                    List<Option> opts = screenQuestion.getAnswer().getOptions();
                    for (int i = 0; i < opts.size(); i++) {

                        Option currentOption = opts.get(i);

                        tableRow = (TableRow) lInflater.inflate(R.layout.dynamic_tab_row_singleitem,
                                tableLayout, false);
                        tableLayout.addView(tableRow);

                        //Add counter value if possible
                        addCounterValue(screenQuestion, currentOption, tableRow, R.id.counter1);

                        FrameLayout frameLayout = (FrameLayout) tableRow.getChildAt(0);
                        TextCard textOption = (TextCard) frameLayout.getChildAt(1);
                        setTextSettings(textOption, currentOption);

                        frameLayout.setBackgroundColor(
                                Color.parseColor("#" + currentOption.getBackground_colour()));

                        initOptionButton(frameLayout, currentOption, value);
                    }
                    break;
                case Constants.IMAGES_5:
                    List<Option> answerOptions = screenQuestion.getAnswer().getOptions();
                    for (int i = 0; i < answerOptions.size(); i++) {
                        Option currentOption = answerOptions.get(i);
                        int counterID = R.id.counter2;

                        int mod = i % 2;
                        //First item per row requires a new row
                        if (mod == 0) {
                            //Every new row admits 2 options
                            tableRow = (TableRow) lInflater.inflate(R.layout.dynamic_tab_row,
                                    tableLayout, false);
                            tableLayout.addView(tableRow);
                            counterID = R.id.counter1;
                        }

                        //Add counter value if possible
                        addCounterValue(screenQuestion, currentOption, tableRow, counterID);

                        FrameLayout frameLayout = (FrameLayout) tableRow.getChildAt(mod);
                        if (i == 4) {
                            TableRow.LayoutParams params = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.MATCH_PARENT, 1f);
                            //remove the unnecessary second imageview.
                            tableRow.removeViewAt(mod + 1);
                            frameLayout.setLayoutParams(params);
                        }
                        frameLayout.setBackgroundColor(
                                Color.parseColor("#" + currentOption.getBackground_colour()));

                        TextCard textOption = (TextCard) frameLayout.getChildAt(1);
                        setTextSettings(textOption, currentOption);


                        initOptionButton(frameLayout, currentOption, value);
                    }
                    break;
                case Constants.REMINDER:
                case Constants.WARNING:
                    View rootView = rowView.getRootView();

                    ProgressUtils.setProgressBarText(rowView, "");


                    ReminderSingleCustomViewStrategy reminderStrategy =
                            new ReminderSingleCustomViewStrategy(this);

                    reminderStrategy.showAndHideViews(rootView);

                    reminderStrategy.showQuestionInfo(rootView, questionItem);

                    break;
                case Constants.INT:
                    tableRow = (TableRow) lInflater.inflate(R.layout.multi_question_tab_int_row,
                            tableLayout, false);
                    addTagQuestion(screenQuestion, tableRow.findViewById(R.id.answer));
                    initIntValue(tableRow, value, tabType);
                    setVisibilityAndAddRow(tableRow, screenQuestion, visibility);
                    break;
                case Constants.LONG_TEXT:
                    tableRow = (TableRow) lInflater.inflate(
                            R.layout.multi_question_tab_long_text_row, tableLayout, false);
                    ((TextCard) tableRow.findViewById(R.id.row_header_text)).setText(
                            Utils.getInternationalizedString(screenQuestion.getForm_name()));
                    addTagQuestion(screenQuestion, tableRow.findViewById(R.id.answer));
                    initLongTextValue(tableRow, value, tabType);
                    setVisibilityAndAddRow(tableRow, screenQuestion, visibility);
                    break;
                case Constants.SHORT_TEXT:
                case Constants.PHONE:
                case Constants.POSITIVE_INT:
                case Constants.RADIO_GROUP_HORIZONTAL:
                case Constants.IMAGE_RADIO_GROUP:
                case Constants.IMAGE_RADIO_GROUP_NO_DATAELEMENT:
                    //TODO: swipeTouchListener.addClickableView(button)

                    tableRow = new TableRow(context);

                    IQuestionView questionView = questionViewFactory.getView(context,
                            screenQuestion.getOutput());

                    if (questionView instanceof IMultiQuestionView) {
                        mMultiQuestionViews.add((IMultiQuestionView) questionView);
                        ((IMultiQuestionView) questionView).setHeader(
                                Utils.getInternationalizedString(screenQuestion.getForm_name()));
                    }

                    if (questionView instanceof AKeyboardQuestionView) {
                        ((AKeyboardQuestionView) questionView).setHint(
                                Utils.getInternationalizedString(screenQuestion.getHelp_text()));
                    }

                    configureLayoutParams(tabType, tableRow, (LinearLayout) questionView);

                    questionView.setEnabled(!readOnly);

                    if (questionView instanceof IImageQuestionView) {
                        ((IImageQuestionView) questionView).setImage(
                                screenQuestion.getInternationalizedPath());
                    }

                    if (questionView instanceof AOptionQuestionView) {
                        ((AOptionQuestionView) questionView).setQuestion(screenQuestion);
                        ((AOptionQuestionView) questionView).setOptions(
                                screenQuestion.getAnswer().getOptions());
                    }

                    if (reloadingQuestionFromInvalidOption) {
                        reloadingQuestionFromInvalidOption = false;
                    } else {
                        questionView.setValue(value);
                    }

                    configureAnswerChangedListener(questionViewFactory, questionView);

                    addTagQuestion(screenQuestion, ((View) questionView).findViewById(R.id.answer));

                    tableRow.addView((View) questionView);

                    setVisibilityAndAddRow(tableRow, screenQuestion, visibility);
                    break;
                case Constants.QUESTION_LABEL:
                    tableRow = (TableRow) lInflater.inflate(R.layout.multi_question_tab_label_row,
                            tableLayout, false);
                    TextCard textCard = (TextCard) tableRow.findViewById(R.id.row_header_text);
                    ImageView rowImageLabelView = ((ImageView) tableRow.findViewById(
                            R.id.question_image_row));
                    textCard.setText(
                            Utils.getInternationalizedString(screenQuestion.getForm_name()));
                    if (screenQuestion.hasAssociatedImage()) {
                        LayoutUtils.makeImageVisible(screenQuestion.getInternationalizedPath(),
                                rowImageLabelView);
                    } else {
                        adaptLayoutToTextOnly(tableRow.findViewById(R.id.question_text_container),
                                rowImageLabelView);
                    }

                    ((TextCard) tableRow.findViewById(R.id.row_header_text)).setText(
                            Utils.getInternationalizedString(screenQuestion.getForm_name()));

                    if (!screenQuestion.getHelp_text().isEmpty()) {
                        ((TextCard) tableRow.findViewById(R.id.row_help_text)).setText(
                                Utils.getInternationalizedString(screenQuestion.getHelp_text()));
                    }

                    setVisibilityAndAddRow(tableRow, screenQuestion, visibility);
                    break;
                case Constants.DROPDOWN_LIST:
                case Constants.DROPDOWN_OU_LIST:
                    tableRow = (TableRow) lInflater.inflate(
                            R.layout.multi_question_tab_dropdown_row, tableLayout, false);
                    ((TextCard) tableRow.findViewById(R.id.row_header_text)).setText(
                            Utils.getInternationalizedString(screenQuestion.getForm_name()));

                    ImageView rowImageView = ((ImageView) tableRow.findViewById(
                            R.id.question_image_row));
                    if (screenQuestion.hasAssociatedImage()) {
                        LayoutUtils.makeImageVisible(screenQuestion.getInternationalizedPath(),
                                rowImageView);
                    } else {
                        rowImageView.setVisibility(View.GONE);
                    }

                    addTagQuestion(screenQuestion, tableRow.findViewById(R.id.answer));
                    tableRow = populateSpinnerFromOptions(tableRow, screenQuestion);
                    initDropdownValue(tableRow, value);
                    setVisibilityAndAddRow(tableRow, screenQuestion, visibility);
                    break;
                case Constants.SWITCH_BUTTON:
                    tableRow = (TableRow) lInflater.inflate(R.layout.multi_question_tab_switch_row,
                            tableLayout, false);

                    ((TextCard) tableRow.findViewById(R.id.row_header_text)).setText(
                            Utils.getInternationalizedString(screenQuestion.getForm_name()));

                    if (!screenQuestion.getHelp_text().isEmpty()) {
                        ((TextCard) tableRow.findViewById(R.id.row_help_text)).setText(
                                Utils.getInternationalizedString(screenQuestion.getHelp_text()));
                    }

                    if (screenQuestion.hasAssociatedImage()) {
                        rowImageView = ((ImageView) tableRow.findViewById(
                                R.id.question_image_row));
                        LayoutUtils.makeImageVisible(screenQuestion.getInternationalizedPath(),
                                rowImageView);
                    }

                    ((TextCard) tableRow.findViewById(R.id.row_switch_true)).setText(
                            Utils.getInternationalizedString(
                                    screenQuestion.getAnswer().getOptions().get(0).getCode()));
                    ((TextCard) tableRow.findViewById(R.id.row_switch_false)).setText(
                            Utils.getInternationalizedString(
                                    screenQuestion.getAnswer().getOptions().get(1).getCode()));

                    Switch switchView = (Switch) tableRow.findViewById(R.id.answer);
                    addTagQuestion(screenQuestion, tableRow.findViewById(R.id.answer));
                    initSwitchOption(screenQuestion, switchView);
                    setVisibilityAndAddRow(tableRow, screenQuestion, visibility);
                    showOrHideChildren(screenQuestion);
                    break;
            }
            setBottomLine(tabType, screenQuestions, screenQuestion);
        }
        rowView.requestLayout();
        reloadingQuestionFromInvalidOption = false;
        return rowView;
    }

    private void setBottomLine(int tabType, List<Question> screenQuestions,
            Question screenQuestion) {
        if (isMultipleQuestionTab(tabType) && screenQuestion.getId_question().equals(
                screenQuestions.get(screenQuestions.size() - 1).getId_question())) {
            LinearLayout view = (LinearLayout) lInflater.inflate(R.layout.bottom_screen_view,
                    tableLayout, false);
            tableLayout.addView(view);
        }
    }

    private void setVisibilityAndAddRow(TableRow tableRow, Question screenQuestion,
            int visibility) {
        tableRow.setVisibility(visibility);
        showCompulsory(tableRow, screenQuestion);
        tableLayout.addView(tableRow);
    }

    private void showCompulsory(TableRow tableRow, Question screenQuestion) {
        if (screenQuestion.isCompulsory()) {
            ImageView rowCompulsoryView = ((ImageView) tableRow.findViewById(
                    R.id.row_header_compulsory));
            if (rowCompulsoryView != null) {
                rowCompulsoryView.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isTabScrollable(Question questionItem, int tabType) {
        return isMultipleQuestionTab(tabType)
                || questionItem.getOutput() == Constants.IMAGE_RADIO_GROUP
                || questionItem.getOutput() == Constants.IMAGE_RADIO_GROUP_NO_DATAELEMENT;
    }

    private void adaptLayoutToTextOnly(View viewWithText, ImageView rowImageLabelView) {
        //Modify the text weight if the label don't have a image.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 0f);
        rowImageLabelView.setLayoutParams(params);
        params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        viewWithText.setLayoutParams(params);
    }

    private void configureAnswerChangedListener(IQuestionViewFactory questionViewFactory,
            IQuestionView questionView) {
        if (questionView instanceof AKeyboardQuestionView) {
            ((AKeyboardQuestionView) questionView).setOnAnswerChangedListener(
                    questionViewFactory.getStringAnswerChangedListener(tableLayout, this));
        } else {
            ((AOptionQuestionView) questionView).setOnAnswerChangedListener(
                    questionViewFactory.getOptionAnswerChangedListener(tableLayout, this));
        }
    }

    private void configureLayoutParams(int tabType, TableRow tableRow, LinearLayout questionView) {
        if (isMultipleQuestionTab(tabType)) {

            tableRow.setLayoutParams(
                    new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT, 1));

            questionView.setLayoutParams(
                    new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT, 0.5f));
        } else {
            tableRow.setLayoutParams(
                    new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.MATCH_PARENT, 1));

            questionView.setLayoutParams(
                    new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT, 1));
        }
    }

    /**
     * Populat the dropdown (spinners) from question answer options
     */
    private TableRow populateSpinnerFromOptions(TableRow tableRow, Question question) {
        Spinner dropdown_list = (Spinner) tableRow.findViewById(R.id.answer);
        // In case the option is selected, we will need to show num/dems
        List<Option> optionList = new ArrayList<>(question.getAnswer().getOptions());
        optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
        dropdown_list.setAdapter(new OptionArrayAdapter(context, optionList));
        return tableRow;
    }

    /**
     * Create a buttons for navigate.
     */
    private View createNavigationButtonsBackButton(View navigationButtonsHolder) {
        ImageButton button = (ImageButton) navigationButtonsHolder.findViewById(R.id.next_btn);
        //Save the numberpicker value in the DB, and continue to the next screen.
        ((LinearLayout) button.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClicked) {
                    Log.d(TAG, "onClick ignored to avoid double click");
                    return;
                }
                Log.d(TAG, "onClicked");

                isClicked = true;
                boolean questionsWithError = false;

                for (IMultiQuestionView multiquestionView : mMultiQuestionViews) {
                    if (multiquestionView.hasError()) {
                        questionsWithError = true;
                        break;
                    }
                }

                Log.d(TAG, "Questions with failed validation " + failedValidations);
                if (failedValidations == 0 && !questionsWithError) {

                    TableRow currentRow = (TableRow) tableLayout.getChildAt(0);

                    if (!readOnly && currentRow != null && currentRow.getChildAt(
                            0) instanceof ImageRadioButtonSingleQuestionView) {

                        navigationController.isMovingToForward = true;

                        ImageRadioButtonSingleQuestionView imageRadioButtonSingleQuestionView =
                                (ImageRadioButtonSingleQuestionView) currentRow.getChildAt(0);

                        ImageRadioButtonOption selectedOptionView =
                                imageRadioButtonSingleQuestionView.getSelectedOptionView();

                        if (selectedOptionView != null) {
                            final Question question = navigationController.getCurrentQuestion();

                            Option selectedOption = selectedOptionView.getOption();

                            Question counterQuestion = question.findCounterByOption(
                                    selectedOption);
                            if (counterQuestion == null || (mReviewMode
                                    && isCounterValueEqualToMax(question, selectedOption))) {
                                saveOptionAndMove(selectedOptionView,
                                        selectedOptionView.getOption(),
                                        question);
                            } else {
                                showConfirmCounter(selectedOptionView,
                                        selectedOptionView.getOption(),
                                        question, counterQuestion);
                            }
                        } else {
                            isClicked = false;
                        }
                    } else {
                        finishOrNext();
                    }
                } else if (navigationController.getCurrentQuestion().hasCompulsoryNotAnswered()) {
                    ToastUseCase.showCompulsoryUnansweredToast();
                    isClicked = false;
                    return;
                } else {
                    isClicked = false;
                }
            }
        });
        button = (ImageButton) navigationButtonsHolder.findViewById(R.id.back_btn);
        //Save the numberpicker value in the DB, and continue to the next screen.
        ((LinearLayout) button.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
            }
        });
        return navigationButtonsHolder;
    }

    private boolean isCounterValueEqualToMax(Question question, Option selectedOption) {

        Float counterValue = Session.getSurvey().getCounterValue(question, selectedOption);

        Float maxCounter = selectedOption.getFactor();

        return counterValue.equals(maxCounter);
    }

    private void setTextSettings(TextCard textOption, Option currentOption) {
        //Fixme To show a text in laos language: change "KhmerOS.ttf" to the new laos font in
        // donottranslate laos file.
        if (currentOption.getOptionAttribute().hasHorizontalAlignment()
                && currentOption.getOptionAttribute().hasVerticalAlignment()) {
            textOption.setText(currentOption.getInternationalizedCode());
            textOption.setGravity(currentOption.getOptionAttribute().getGravity());
        } else {
            textOption.setVisibility(View.GONE);
        }
        textOption.setTextSize(currentOption.getOptionAttribute().getText_size());
    }

    public void initWarningValue(View rootView, Option option) {
        ImageView errorImage = (ImageView) rootView.findViewById(R.id.confirm_yes);
        errorImage.setImageResource(R.drawable.option_button);
        //Add button to listener
        swipeTouchListener.addClickableView(errorImage);
        //Add text into the button
        TextView okText = (TextView) rootView.findViewById(R.id.textcard_confirm_yes);
        okText.setText(option.getInternationalizedCode());
        okText.setTextSize(option.getOptionAttribute().getText_size());
    }

    public void initWarningText(View rootView, Option option) {
        TextView okText = (TextView) rootView.findViewById(R.id.questionTextRow);
        okText.setText(option.getInternationalizedCode());
        okText.setTextSize(option.getOptionAttribute().getText_size());
    }

    /**
     * Adds current Counter value to image option
     *
     * @param question      Current question
     * @param currentOption Current option
     * @param tableRow      Row where the counter is gonna be added
     */
    private void addCounterValue(Question question, Option currentOption, TableRow tableRow,
            int counterID) {
        Question optionCounter = question.findCounterByOption(currentOption);
        if (optionCounter == null) {
            return;
        }
        String counterValue = ReadWriteDB.readValueQuestion(optionCounter);
        if (counterValue == null || counterValue.isEmpty()) {
            return;
        }

        TextView counterText = (TextView) tableRow.findViewById(counterID);
        String counterTextValue = context.getResources().getString(R.string.option_counter);

        //Repetitions: 3
        counterText.setText(counterTextValue + counterValue);
        counterText.setVisibility(View.VISIBLE);
    }

    /**
     * Used to set the text widht like the framelayout size
     * to prevent a resize of the frameLayout if the textoption is more bigger.
     */
    private void resizeTextWidth(FrameLayout frameLayout, TextCard textOption) {
        textOption.setWidth(frameLayout.getWidth());
    }

    private void showKeyboard(Context c, View v) {
        Log.d(TAG, "KEYBOARD SHOW ");
        keyboardView = v;
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(v, 0);
    }

    /**
     * hide keyboard using a provided view
     */
    private void hideKeyboard(Context c, View v) {
        Log.d(TAG, "KEYBOARD HIDE ");
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (v != null) {
            keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * hide keyboard using a keyboardView variable view
     */
    private void hideKeyboard(Context c) {
        Log.d(TAG, "KEYBOARD HIDE ");
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (keyboardView != null) {
            keyboard.hideSoftInputFromWindow(keyboardView.getWindowToken(), 0);
        }
    }

    /**
     * Checks if a tab is a multiple question Tab
     */
    private boolean isMultipleQuestionTab(int tabType) {
        return tabType == Constants.TAB_MULTI_QUESTION;
    }

    /**
     * Adds listener to the Editcard and sets the default or saved value
     */
    private void initIntValue(TableRow row, Value value, int tabType) {
        final EditCard numberPicker = (EditCard) row.findViewById(R.id.answer);

        //Has value? show it
        if (value != null) {
            numberPicker.setText(value.getValue());
        }

        if (!readOnly) {
            numberPicker.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //Save the numberpicker value in the DB, and continue to the next screen.
                    saveValue(numberPicker);
                }
            });
        } else {
            numberPicker.setEnabled(false);
        }
        if (!isMultipleQuestionTab(tabType)) {
            //Take focus and open keyboard
            openKeyboard(numberPicker);
        }
    }

    /**
     * Adds question as tag in a view to identify the answers
     */
    private void addTagQuestion(Question question, View viewById) {
        viewById.setTag(question);
    }

    /**
     * Adds listener to the Editcard and sets the default or saved value
     */
    private void initLongTextValue(TableRow row, Value value, int tabType) {
        final EditCard editCard = (EditCard) row.findViewById(R.id.answer);

        //Has value? show it
        if (value != null) {
            editCard.setText(value.getValue());
        }

        if (!readOnly) {
            editCard.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //Save the editCard value in the DB, and continue to the next screen.
                    Question question = (Question) editCard.getTag();
                    ReadWriteDB.saveValuesText(question, String.valueOf(s));
                }
            });
        } else {
            editCard.setEnabled(false);
        }
        if (!isMultipleQuestionTab(tabType)) {
            //Take focus and open keyboard
            openKeyboard(editCard);
        }
    }

    /**
     * Adds listener to the dropdown and sets the default or saved value
     */
    private void initDropdownValue(TableRow row, Value value) {
        Spinner dropdown = (Spinner) row.findViewById(R.id.answer);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Option option = (Option) parent.getItemAtPosition(position);
                Question question = (Question) parent.getTag();
                if (question.getOutput().equals(Constants.IMAGE_3_NO_DATAELEMENT)) {
                    switchHiddenMatches(question, option);
                } else {
                    ReadWriteDB.saveValuesDDL(question, option, question.getValueBySession());
                }
                showOrHideChildren(question);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (value != null && value.getValue() != null) {
            for (int i = 0; i < dropdown.getAdapter().getCount(); i++) {
                Option option = (Option) dropdown.getItemAtPosition(i);
                if (option.equals(value.getOption())) {
                    dropdown.setSelection(i);
                    break;
                }
            }
        }

        //Readonly (not clickable, enabled)
        if (readOnly) {
            dropdown.setEnabled(false);
            return;
        }
    }

    /**
     * Hide or show the childen question from a given question,  if is necessary  it reloads the
     * children questions values or refreshing the children questions answer component
     *
     * TODO: Duplicate code in AQuestionAnswerChangedListener line 43
     * this code will be delete when DynamicTabAdapter refactoring will be completed
     *
     * @param question is the parent question
     */
    private void showOrHideChildren(Question question) {
        if (!question.hasChildren()) {
            return;
        }

        for (int i = 0, j = tableLayout.getChildCount(); i < j; i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                View answerView = view.findViewById(R.id.answer);
                if (answerView == null) {
                    continue;
                }
                Question rowQuestion = (Question) answerView.getTag();
                if (rowQuestion == null) {
                    continue;
                }
                List<Question> questionChildren = question.getChildren();
                if (questionChildren != null && questionChildren.size() > 0) {
                    for (Question childQuestion : questionChildren) {
                        //if the table row question is child of the modified question...
                        toggleChild(row, rowQuestion, childQuestion);
                    }
                }
            }
        }
    }

    /**
     * find and toggle the child question
     *
     * @param row           is the child question view
     * @param rowQuestion   is the question in the view
     * @param childQuestion is the posible child
     */
    private boolean toggleChild(TableRow row, Question rowQuestion, Question childQuestion) {
        if (childQuestion.getId_question().equals(rowQuestion.getId_question())) {
            if (rowQuestion.isHiddenBySurveyAndHeader(getSurvey())) {
                row.setVisibility(View.GONE);
                hideDefaultValue(rowQuestion);
            } else {
                row.setVisibility(View.VISIBLE);
                showDefaultValue(row, rowQuestion);
            }
            return true;
        }
        return false;
    }

    /**
     * removes or modify the value with a correct value when the question is hide
     *
     * @param rowQuestion is the question in the view
     */
    private void hideDefaultValue(Question rowQuestion) {
        switch (rowQuestion.getOutput()) {
            case Constants.PHONE:
            case Constants.POSITIVE_INT:
            case Constants.INT:
            case Constants.LONG_TEXT:
            case Constants.SHORT_TEXT:
            case Constants.DROPDOWN_LIST:
            case Constants.DROPDOWN_OU_LIST:
                ReadWriteDB.deleteValue(rowQuestion);
                break;
            case Constants.SWITCH_BUTTON:
                //the 0 option is the left option and is false in the switch, the 1 option is the
                // right option and is true
                boolean isChecked = false;
                if (rowQuestion.getAnswer().getOptions().get(
                        1).getOptionAttribute().getDefaultOption() == 1) {
                    isChecked = true;
                }
                saveSwitchOption(rowQuestion, isChecked);
                break;
        }
    }

    /**
     * when a question is shown this method set the correct value.
     *
     * @param rowQuestion is the question in the view
     */
    private void showDefaultValue(TableRow tableRow, Question rowQuestion) {
        if (rowQuestion.getValueBySession() != null) {
            return;
        }
        switch (rowQuestion.getOutput()) {
            case Constants.PHONE:
            case Constants.POSITIVE_INT:
            case Constants.INT:
            case Constants.LONG_TEXT:
            case Constants.SHORT_TEXT:
                final EditCard editCard = (EditCard) tableRow.findViewById(R.id.answer);
                editCard.setText("");
                break;
            case Constants.DROPDOWN_LIST:
            case Constants.DROPDOWN_OU_LIST:
                Spinner dropdown = (Spinner) tableRow.findViewById(R.id.answer);
                dropdown.setSelection(0);
                break;
            case Constants.SWITCH_BUTTON:
                Switch switchView = (Switch) tableRow.findViewById(R.id.answer);
                Option selectedOption = rowQuestion.getOptionBySession();
                if (selectedOption == null) {
                    //the 0 option is the left option and is false in the switch, the 1 option is
                    // the right option and is true
                    boolean isChecked = false;
                    if (rowQuestion.getAnswer().getOptions().get(
                            1).getOptionAttribute().getDefaultOption() == 1) {
                        isChecked = true;
                    }
                    saveSwitchOption(rowQuestion, isChecked);
                    switchView.setChecked(isChecked);
                    break;
                }
                switchView.setChecked(findSwitchBoolean(rowQuestion));
                break;
        }
    }

    /**
     * Save value in DB and check the children
     */
    private void saveValue(EditCard editCard) {
        Question question = (Question) editCard.getTag();
        ReadWriteDB.saveValuesText(question, editCard.getText().toString());
        showOrHideChildren(question);
    }

    /**
     * Open keyboard and add listeners to click/next option.
     */
    private void openKeyboard(final EditText editText) {
        if (!readOnly) {
            editText.requestFocus();
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Show keypad
                    Question question = (Question) editText.getTag();
                    if (isMultipleQuestionTab(question.getHeader().getTab().getType())) {
                        return;
                    }
                    showKeyboard(context, editText);
                }
            }, 300);
        }
    }

    /**
     * Attach an option with its button in view, adding the listener
     */
    private void initOptionButton(FrameLayout button, Option option, Value value) {
        // value = null --> first time calling initOptionButton
        //Highlight button
        if (value != null && value.getValue().equals(option.getName())) {
            LayoutUtils.highlightSelection(button, option);
        } else if (value != null) {
            LayoutUtils.overshadow(button);
        }

        //the button is a framelayout that contains a imageview
        ImageView imageView = (ImageView) button.getChildAt(0);
        //Put image
        BaseLayoutUtils.putImageInImageView(option.getInternationalizedPath(), imageView);
        //Associate option
        button.setTag(option);

        //Readonly (not clickable, enabled)
        if (readOnly) {
            button.setEnabled(false);
            return;
        }

        //Add button to listener
        swipeTouchListener.addClickableView(button);

        resizeTextWidth(button, (TextCard) button.getChildAt(1));
    }

    /**
     * Advance to the next question with delay applied or finish survey according to question and
     * value.
     */
    public void finishOrNext() {
        if (navigationController.getCurrentQuestion().hasCompulsoryNotAnswered()) {
            ToastUseCase.showCompulsoryUnansweredToast();
            isClicked = false;
            return;
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Question question = navigationController.getCurrentQuestion();
                Value value = question.getValueBySession();
                if (isDone(value)) {
                    navigationController.isMovingToForward = false;
                    if (!wasPatientTested() || !BuildConfig.reviewScreen) {
                        showDone();
                    } else {
                        DashboardActivity.dashboardActivity.showReviewFragment();
                        hideKeyboard(PreferencesState.getInstance().getContext());
                        isClicked = false;
                    }
                    return;
                }
                next();
            }
        }, 750);
    }

    public boolean wasPatientTested() {
        return getSurvey().isRDT() || BuildConfig.patientTestedByDefault;
    }

    /**
     * Show a final dialog to announce the survey is over without reviewfragment.
     */
    private void showDone() {
        final Activity activity = (Activity) context;
        AlertDialog.Builder msgConfirmation = new AlertDialog.Builder(context)
                .setTitle(R.string.survey_completed)
                .setMessage(R.string.survey_completed_text)
                .setCancelable(false)
                .setPositiveButton(R.string.survey_send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        hideKeyboard(PreferencesState.getInstance().getContext());
                        DashboardActivity.dashboardActivity.closeSurveyFragment();
                        isClicked = false;
                    }
                });
        msgConfirmation.setNegativeButton(R.string.survey_review,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        hideKeyboard(PreferencesState.getInstance().getContext());
                        review();
                        isClicked = false;
                    }
                });

        msgConfirmation.create().show();
    }

    /**
     * Checks if there are more questions to answer according to the given value + current status.
     */
    private boolean isDone(Value value) {
        return !navigationController.hasNext(value != null ? value.getOption() : null);
    }

    /**
     * Changes the current question moving forward
     */
    private void next() {
        Question question = navigationController.getCurrentQuestion();
        Value value = question.getValueBySession();
        if (isDone(value)) {
            navigationController.isMovingToForward = false;
            return;
        }
        navigationController.next(value != null ? value.getOption() : null);
        notifyDataSetChanged();
        hideKeyboard(PreferencesState.getInstance().getContext());

        question = navigationController.getCurrentQuestion();
        value = question.getValueBySession();
        //set new page number if the value is null
        if (value == null && !readOnly) {
            navigationController.setTotalPages(
                    navigationController.getCurrentQuestion().getTotalQuestions());
        }
        navigationController.isMovingToForward = false;
        isClicked = false;
    }

    /**
     * Changes the current question moving backward
     */
    private void previous() {
        if (!navigationController.hasPrevious()) {
            return;
        }
        navigationController.previous();
        notifyDataSetChanged();
        isClicked = false;
    }

    /**
     * Back to initial question to review questions
     */
    private void review() {
        navigationController.first();
        notifyDataSetChanged();
    }

    /**
     * When the user click in a value in the review fragment the navigationController should go to
     * related question
     */
    private void goToQuestion(Question isMoveToQuestion) {
        navigationController.first();

        Question currentQuestion;
        boolean isQuestionFound = false;

        //it is compared by uid because comparing by question it could be not equal by the same
        // question.
        while (!isQuestionFound) {

            currentQuestion = navigationController.getCurrentQuestion();

            int tabType = currentQuestion.getHeader().getTab().getType();
            if (isMultipleQuestionTab(tabType)) {
                List<Question> screenQuestions = currentQuestion.getQuestionsByTab(
                        currentQuestion.getHeader().getTab());

                for (Question question : screenQuestions) {
                    if (isMoveToQuestion.getUid().equals(question.getUid())) {
                        isQuestionFound = true;
                    }
                }
            } else {
                if (isMoveToQuestion.getUid().equals(currentQuestion.getUid())) {
                    isQuestionFound = true;
                }
            }


            if (!isQuestionFound) {
                next();
                skipReminder();
            }
        }

        notifyDataSetChanged();
    }

    /**
     * When the user swip back from review fragment the navigationController should go to the last
     * question
     */
    private void goToLastQuestion() {
        navigationController.first();
        Value value = null;
        do {
            next();
            Question question = navigationController.getCurrentQuestion();
            value = question.getValueBySession();
            skipReminder();
        } while (value != null && !isDone(value));
        notifyDataSetChanged();
    }

    /**
     * Skips the reminder question in the navigation
     */
    private void skipReminder() {
        for (QuestionRelation relation : navigationController.getCurrentQuestion()
                .getQuestionRelations()) {
            if (relation.isAReminder()) {
                next();
            }
        }
    }

    /**
     * Initialize the default switch value or load the saved value
     *
     * @param question       is the question in the view
     * @param switchQuestion is the switch view
     */
    private void initSwitchOption(Question question, Switch switchQuestion) {

        //Take option
        Option selectedOption = question.getOptionBySession();
        if (selectedOption == null) {
            //the 0 option is the right option and is true in the switch, the 1 option is the
            // left option and is false
            boolean isDefaultOption = false;
            boolean switchValue = false;
            if (question.getAnswer().getOptions().get(0).getOptionAttribute().getDefaultOption()
                    == 1) {
                selectedOption = question.getAnswer().getOptions().get(0);
                isDefaultOption = true;
                switchValue = true;
            } else if (question.getAnswer().getOptions().get(
                    1).getOptionAttribute().getDefaultOption() == 1) {
                selectedOption = question.getAnswer().getOptions().get(1);
                isDefaultOption = true;
                switchValue = false;
            }
            if (isDefaultOption) {
                switchQuestion.setChecked(switchValue);
                ReadWriteDB.saveValuesDDL(question, selectedOption, null);
            }
        } else {
            switchQuestion.setChecked(findSwitchBoolean(question));
        }
        switchQuestion.setOnCheckedChangeListener(
                new SwitchButtonListener(question, switchQuestion));
    }

    /**
     * Save the switch option and check children questions
     *
     * @param question  is the question in the view
     * @param isChecked is the value to be saved
     */
    private void saveSwitchOption(Question question, boolean isChecked) {
        //Take option
        Option selectedOption = findSwitchOption(question, isChecked);
        if (selectedOption == null) {
            return;
        }
        ReadWriteDB.saveValuesDDL(question, selectedOption, question.getValueBySession());
        showOrHideChildren(question);
    }

    public class SwitchButtonListener implements CompoundButton.OnCheckedChangeListener {

        private Question question;
        private Switch switchButton;

        public SwitchButtonListener(Question question, Switch switchButton) {
            this.question = question;
            this.switchButton = switchButton;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!buttonView.isShown()) {
                return;
            }
            saveSwitchOption(question, isChecked);
        }
    }
}