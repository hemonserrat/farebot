/*
 * ListItem.java
 *
 * This file is part of FareBot.
 * Learn more at: https://codebutler.github.io/farebot/
 *
 * Copyright (C) 2012, 2014-2015 Eric Butler <eric@codebutler.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.farebot.ui;

import com.codebutler.farebot.util.Utils;

public class ListItem {
    private final String mText1;
    private final String mText2;

    public ListItem(int nameResource, int valueResource) {
        this(nameResource, Utils.localizeString(valueResource));
    }

    public ListItem(String name, int valueResource) {
        this(name, Utils.localizeString(valueResource));
    }

    public ListItem(int nameResource, String value) {
        this(Utils.localizeString(nameResource), value);
    }

    public ListItem(String name, String value) {
        mText1 = name;
        mText2 = value;
    }

    public String getText1() {
        return mText1;
    }

    public String getText2() {
        return mText2;
    }
}
