/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Survelliance App App.
 *
 *  QIS Survelliance App App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Survelliance App App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Survelliance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test.utils;

import android.view.View;

import org.eyeseetea.malariacare.views.TextCard;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.android.apps.common.testing.deps.guava.base.Preconditions.checkNotNull;

/**
 * Created by arrizabalaga on 25/05/15.
 */
public class TextCardScaleMatcher extends TypeSafeMatcher<View> {
    private final String scale;

    private TextCardScaleMatcher(String scale) {
        this.scale = checkNotNull(scale);
    }

    @Override
    public boolean matchesSafely(View view) {
        if (!(view instanceof TextCard)) {
            return false;
        }
        TextCard textCard = (TextCard) view;
        return scale.equals(textCard.getmScale());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with scale: " + scale);
    }

    public static Matcher<? super View> hasTextCardScale(String scale) {
        return new TextCardScaleMatcher(scale);
    }
}