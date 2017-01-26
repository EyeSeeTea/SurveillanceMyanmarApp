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

package org.eyeseetea.malariacare.data.sync.importer;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.Option;
import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.database.model.Value;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.sync.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.sync.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.sync.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.sync.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.network.PushClient;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    private final static String TAG = ".ConvertFromSDKVisitor";


    Program program;
    Map<String, Object> appMapObjects;
    List<Survey> surveys;
    List<Value> values;


    public ConvertFromSDKVisitor() {
        program = Program.getFirstProgram();
        appMapObjects = new HashMap();
        surveys = new ArrayList<>();
        values = new ArrayList<>();
    }

    public Map<String, Object> getAppMapObjects() {
        return appMapObjects;
    }

    public void setAppMapObjects(Map<String, Object> appMapObjects) {
        this.appMapObjects = appMapObjects;
    }

    public List<Survey> getSurveys() {
        return surveys;
    }

    public void setSurveys(List<Survey> surveys) {
        this.surveys = surveys;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    /**
     * Turns a sdk organisationUnit into an app OrgUnit
     */
    @Override
    public void visit(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        //Create and save OrgUnitLevel
        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
        if (!appMapObjects.containsKey(String.valueOf(sdkOrganisationUnitExtended.getLevel()))) {
            orgUnitLevel.setName(
                    PreferencesState.getInstance().getContext().getResources().getString(
                            R.string.create_info_zone));
            orgUnitLevel.save();
            appMapObjects.put(String.valueOf(sdkOrganisationUnitExtended.getLevel()), orgUnitLevel);
        }
        //create the orgUnit
        OrgUnit appOrgUnit = new OrgUnit();
        //Set name
        appOrgUnit.setName(sdkOrganisationUnitExtended.getLabel());
        //Set uid
        appOrgUnit.setUid(sdkOrganisationUnitExtended.getId());
        //Set orgUnitLevel
        appOrgUnit.setOrgUnitLevel(
                (OrgUnitLevel) appMapObjects.get(
                        String.valueOf(sdkOrganisationUnitExtended.getLevel())));
        //Set the parent
        //At this moment, the parent is a UID of a not pulled Org_unit , without the full
        // org_unit the OrgUnit.orgUnit(parent) is null.
        String parent_id = null;
        parent_id = sdkOrganisationUnitExtended.getParent();
        if (parent_id != null && !parent_id.equals("")) {
            appOrgUnit.setOrgUnit((OrgUnit) appMapObjects.get(String.valueOf(parent_id)));
        } else {
            appOrgUnit.setOrgUnit((OrgUnit) null);
        }
        appOrgUnit.save();
        //Annotate built orgunit
        appMapObjects.put(sdkOrganisationUnitExtended.getId(), appOrgUnit);
    }

    /**
     * Turns a sdk userAccount into a User
     */
    @Override
    public void visit(UserAccountExtended sdkUserAccountExtended) {
        User appUser = new User();
        appUser.setUid(sdkUserAccountExtended.getUid());
        appUser.setName(sdkUserAccountExtended.getName());
        appUser.save();
    }

    /**
     * Turns an event into a sent survey
     */
    @Override
    public void visit(EventExtended sdkEventExtended) {
        OrgUnit orgUnit = (OrgUnit) appMapObjects.get(sdkEventExtended.getOrganisationUnitId());

        Survey survey = new Survey();
        //Any survey that comes from the pull has been sent
        survey.setStatus(Constants.SURVEY_SENT);
        //Set dates
        survey.setCreationDate(sdkEventExtended.getCreationDate());
        survey.setCompletionDate(sdkEventExtended.getEventDate());
        survey.setEventDate(sdkEventExtended.getEventDate());
        survey.setScheduledDate(sdkEventExtended.getScheduledDate());
        //Set fks
        survey.setOrgUnit(orgUnit);
        survey.setProgram(program);
        surveys.add(survey);

        //Annotate object in map
        appMapObjects.put(sdkEventExtended.getUid(), survey);
    }

    @Override
    public void visit(DataValueExtended sdkDataValueExtended) {
        Survey survey = (Survey) appMapObjects.get(sdkDataValueExtended.getEvent());
        String questionUID = sdkDataValueExtended.getDataElement();

        //Data value is a value from compositeScore -> ignore
        if (appMapObjects.get(questionUID) instanceof CompositeScore) {
            return;
        }

        //Phone metadata -> ignore
        if (PushClient.PHONEMETADA_UID.equals(questionUID)) {
            return;
        }

        //Datavalue is a value from a question
        Question question = Question.findByUID(questionUID);

        Value value = new Value();
        value.setQuestion(question);
        value.setSurvey(survey);

        Option option =
                sdkDataValueExtended.findOptionByQuestion(question);
        value.setOption(option);
        //No option -> text question (straight value)
        if (option == null) {
            value.setValue(sdkDataValueExtended.getValue());
        } else {
            //Option -> extract value from code
            value.setValue(sdkDataValueExtended.getDataValue().getValue());
        }
        values.add(value);
    }

}
