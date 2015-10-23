/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Survelliance App.
 *
 *  QIS Survelliance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Survelliance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Survelliance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.score;

import org.eyeseetea.malariacare.database.model.CompositeScore;

import java.util.List;

public class CompositeNumDenRecord extends ANumDenRecord{


    public List<Float> readNumDen(CompositeScore compositeScore, List<Float> numDenTotal){
        numDenTotal = this.calculateNumDenTotal(numDenTotal);

        if (compositeScore.hasChildren()){
            for (CompositeScore compositeScoreChild : compositeScore.getCompositeScoreChildren()){
                numDenTotal = readNumDen(compositeScoreChild, numDenTotal);
            }
        }

        return numDenTotal;
    }
}
