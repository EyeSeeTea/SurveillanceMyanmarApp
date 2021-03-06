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

package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

@Table(database = AppDatabase.class)
public class OrgUnitLevel extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_org_unit_level;
    @Column
    String name;


    List<OrgUnit> orgUnits;

    public OrgUnitLevel() {
    }

    public OrgUnitLevel(String name) {
        this.name = name;
    }


    public OrgUnitLevel(String uid, String name) {
        this.name = name;
    }

    public Long getId_org_unit_level() {
        return id_org_unit_level;
    }

    public void setId_org_unit_level(Long id_org_unit_level) {
        this.id_org_unit_level = id_org_unit_level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @OneToMany(methods = {OneToMany.Method.SAVE,
            OneToMany.Method.DELETE}, variableName = "orgUnits")
    public List<OrgUnit> getOrgUnits() {
        this.orgUnits = new Select().from(OrgUnit.class)
                .where(OrgUnit_Table.id_org_unit_parent.eq(
                        this.getId_org_unit_level())).queryList();
        return orgUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitLevel that = (OrgUnitLevel) o;

        if (id_org_unit_level != that.id_org_unit_level) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_org_unit_level ^ (id_org_unit_level >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "id=" + id_org_unit_level +
                ", name='" + name + '\'' +
                '}';
    }
}
