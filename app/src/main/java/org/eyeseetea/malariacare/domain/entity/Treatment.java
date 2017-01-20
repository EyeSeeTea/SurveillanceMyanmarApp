package org.eyeseetea.malariacare.domain.entity;

import android.util.Log;

import org.eyeseetea.malariacare.database.model.Drug;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionThreshold;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class Treatment {

    private static final String TAG = ".Treatment";
    private Survey mSurvey;
    private List<Question> mQuestions;
    private org.eyeseetea.malariacare.database.model.Treatment mTreatment;

    public Treatment(Survey survey) {
        mSurvey = survey;
    }

    public boolean hasTreatment() {
        mTreatment = getTreatmentFromSurvey();
        if (mTreatment != null) {
            mQuestions = getQuestionsForTreatment(mTreatment);
        }

        return mTreatment != null;
    }


    private org.eyeseetea.malariacare.database.model.Treatment getTreatmentFromSurvey() {

        List<Value> values = mSurvey.getValues();

        List<Match> ageMatches = new ArrayList<>();
        List<Match> pregnantMatches = new ArrayList<>();
        List<Match> severeMatches = new ArrayList<>();
        List<Match> rdtMatches = new ArrayList<>();
        for (Value value : values) {
            Question question = value.getQuestion();
            if (question.getUid().equals("2XX1JoHmO94")) {
                ageMatches =
                        QuestionThreshold.getMatchesWithQuestionValue(
                                question.getId_question(), Integer.parseInt(value.getValue()));
            } else if (question.getUid().equals("6VV1JoHmO94")) {
                pregnantMatches = QuestionOption.getMatchesWithQuestionOption(
                        question.getId_question(),
                        value.getId_option());
            } else if (question.getUid().equals("11V1JoHmO94")) {
                severeMatches = QuestionOption.getMatchesWithQuestionOption(
                        question.getId_question(),
                        value.getId_option());
            } else if (question.getUid().equals("12V1JoHmO94")) {
                rdtMatches = QuestionOption.getMatchesWithQuestionOption(question.getId_question(),
                        value.getId_option());
            }
        }
        Log.d(TAG, "matches obtained");
        Match treatmentMatch = null;
        for (Match match : ageMatches) {
            if (pregnantMatches.contains(match) && severeMatches.contains(match)
                    && rdtMatches.contains(match)) {
                treatmentMatch = match;
                break;
            }
        }

        org.eyeseetea.malariacare.database.model.Treatment treatment = null;
        if (treatmentMatch != null) {
            Log.d(TAG, "match: " + treatmentMatch.toString());
            treatment = treatmentMatch.getTreatment();
            Log.d(TAG, "treatment: " + treatment.toString());
        }
        return treatment;
    }

    private List<Question> getQuestionsForTreatment(
            org.eyeseetea.malariacare.database.model.Treatment treatment) {
        List<Question> questions = new ArrayList<>();
        List<Drug> drugs = treatment.getDrugsForTreatment();

        Question treatmentQuestion = new Question();
        treatmentQuestion.setOutput(Constants.QUESTION_LABEL);
        treatmentQuestion.setForm_name(treatment.getDiagnosis());
        treatmentQuestion.setHelp_text(treatment.getMessage());
        treatmentQuestion.setCompulsory(0);
        treatmentQuestion.setHeader((long) 7);
        questions.add(treatmentQuestion);

        for (Drug drug : drugs) {
            Question question = Question.findByUID(drug.getQuestion_code());
            if (question != null) {
                if (isPq(question)) {
                    question.setForm_name(getPqTitleDose(drug.getDose()));
                } else if (isCq(question)) {
                    question.setForm_name(getCqTitleDose(drug.getDose()));
                }
                questions.add(question);
            }
            if (!questions.isEmpty()) {
                Log.d(TAG, "Question: " + questions.get(questions.size() - 1) + "\n");
            }
        }
        questions.add(Question.findByUID("9fV1JoHmO94"));

        return questions;
    }


    public List<Question> getQuestions() {
        return mQuestions;
    }

    public org.eyeseetea.malariacare.database.model.Treatment getTreatment() {
        return mTreatment;
    }


    private boolean isPq(Question question) {
        if (question.getUid().equals("Sttahtf0iHZ")) {
            return true;
        }
        return false;
    }

    private boolean isCq(Question question) {
        if (question.getUid().equals("jZvZ4Q39J6s")) {
            return true;
        }
        return false;
    }

    private String getPqTitleDose(int dose) {
        switch (dose) {
            case 2:
                return "drugs_2_of_Pq_review_title";
            case 4:
                return "drugs_4_of_Pq_review_title";
            case 6:
                return "drugs_6_of_Pq_review_title";
            case 16:
                return "drugs_16_of_Pq_review_title";
            case 32:
                return "drugs_32_of_Pq_review_title";
            case 48:
                return "drugs_48_of_Pq_review_title";
        }
        return "drugs_referral_Pq_review_title";
    }

    private String getCqTitleDose(int dose) {
        switch (dose) {
            case 4:
                return "drugs_4_of_Cq_review_title";
            case 5:
                return "drugs_5_of_Cq_review_title";
            case 7:
                return "drugs_7_of_Cq_review_title";
            case 10:
                return "drugs_10_of_Cq_review_title";
        }
        return "drugs_referral_Cq_review_title";
    }

    private void setAnswerYesOptionFactor(int dose, Question question) {
        List<Option> options = question.getAnswer().getOptions();
        for (Option option : options) {
            if (option.getName().equals("Yes")) {
                option.setFactor((float) dose);
            }
        }
    }

}
