/*
 * OrcaTrip.java
 *
 * This file is part of FareBot.
 * Learn more at: https://codebutler.github.io/farebot/
 *
 * Copyright (C) 2014-2015 Eric Butler <eric@codebutler.com>
 * Copyright (C) 2014 Kramer Campbell <kramer@kramerc.com>
 * Copyright (C) 2015 Sean CyberKitsune McClenaghan <cyberkitsune09@gmail.com>
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

package com.codebutler.farebot.transit.orca;

import android.os.Parcel;

import com.codebutler.farebot.card.desfire.DesfireRecord;
import com.codebutler.farebot.transit.Station;
import com.codebutler.farebot.transit.Trip;
import com.codebutler.farebot.util.ImmutableMapBuilder;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class OrcaTrip extends Trip {
    private final long mTimestamp;
    private final long mCoachNum;
    private final long mFare;
    private final long mNewBalance;
    final long mAgency;
    final long mTransType;

    private static final Map<Long, Station> LINK_STATIONS = new ImmutableMapBuilder<Long, Station>()
            .put(10352L, new Station("Capitol Hill Station", "Capitol Hill", "47.6192", "-122.3202"))
            .put(10351L, new Station("University of Washington Station", "UW Station", "47.6496", "-122.3037"))
            .put(13193L, new Station("Westlake Station", "Westlake", "47.6113968", "-122.337502"))
            .put(13194L, new Station("University Station", "University", "47.6072502", "-122.335754"))
            .put(13195L, new Station("Pioneer Square Station", "Pioneer Sq", "47.6021461", "-122.33107"))
            .put(13196L, new Station("International District Station", "ID", "47.5976601", "-122.328217"))
            .put(13197L, new Station("Stadium Station", "Stadium", "47.5918121", "-122.327354"))
            .put(13198L, new Station("SODO Station", "SODO", "47.5799484", "-122.327515"))
            .put(13199L, new Station("Beacon Hill Station", "Beacon Hill", "47.5791245", "-122.311287"))
            .put(13200L, new Station("Mount Baker Station", "Mount Baker", "47.5764389", "-122.297737"))
            .put(13201L, new Station("Columbia City Station", "Columbia City", "47.5589523", "-122.292343"))
            .put(13202L, new Station("Othello Station", "Othello", "47.5375366", "-122.281471"))
            .put(13203L, new Station("Rainier Beach Station", "Rainier Beach", "47.5222626", "-122.279579"))
            .put(13204L, new Station("Tukwila International Blvd Station", "Tukwila", "47.4642754", "-122.288391"))
            .put(13205L, new Station("Seatac Airport Station", "Sea-Tac", "47.4445305", "-122.297012"))
            .build();

    private static Map<Integer, Station> sSounderStations = new ImmutableMapBuilder<Integer, Station>()
            .put(3, new Station("King Street Station", "King Street", "47.598445", "-122.330161"))
            .put(5, new Station("Kent Station", "Kent", "47.384257", "-122.233151"))
            .build();

    private static Map<Integer, Station> sWSFTerminals = new ImmutableMapBuilder<Integer, Station>()
            .put(10101, new Station("Seattle Terminal", "Seattle", "47.602722", "-122.338512"))
            .put(10103, new Station("Bainbridge Island Terminal", "Bainbridge", "47.62362", "-122.51082"))
            .build();

    public OrcaTrip(DesfireRecord record) {
        byte[] useData = record.getData();
        long[] usefulData = new long[useData.length];

        for (int i = 0; i < useData.length; i++) {
            usefulData[i] = ((long) useData[i]) & 0xFF;
        }

        mTimestamp = ((0x0F & usefulData[3]) << 28)
                | (usefulData[4] << 20)
                | (usefulData[5] << 12)
                | (usefulData[6] << 4)
                | (usefulData[7] >> 4);

        mCoachNum = ((usefulData[9] & 0xf) << 12) | (usefulData[10] << 4) | ((usefulData[11] & 0xf0) >> 4);

        if (usefulData[15] == 0x00 || usefulData[15] == 0xFF) {
            // FIXME: This appears to be some sort of special case for transfers and passes.
            mFare = 0;
        } else {
            mFare = (usefulData[15] << 7) | (usefulData[16] >> 1);
        }

        mNewBalance = (usefulData[34] << 8) | usefulData[35];
        mAgency = usefulData[3] >> 4;
        mTransType = (usefulData[17]);
    }

    public static final Creator<OrcaTrip> CREATOR = new Creator<OrcaTrip>() {
        @Override
        public OrcaTrip createFromParcel(Parcel parcel) {
            return new OrcaTrip(parcel);
        }

        @Override
        public OrcaTrip[] newArray(int size) {
            return new OrcaTrip[size];
        }
    };

    OrcaTrip(Parcel parcel) {
        mTimestamp = parcel.readLong();
        mCoachNum = parcel.readLong();
        mFare = parcel.readLong();
        mNewBalance = parcel.readLong();
        mAgency = parcel.readLong();
        mTransType = parcel.readLong();
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    @Override
    public long getExitTimestamp() {
        return 0;
    }

    @Override
    public String getAgencyName() {
        switch ((int) mAgency) {
            case OrcaTransitData.AGENCY_CT:
                return "Community Transit";
            case OrcaTransitData.AGENCY_KCM:
                return "King County Metro Transit";
            case OrcaTransitData.AGENCY_PT:
                return "Pierce Transit";
            case OrcaTransitData.AGENCY_ST:
                return "Sound Transit";
            case OrcaTransitData.AGENCY_WSF:
                return "Washington State Ferries";
            case OrcaTransitData.AGENCY_ET:
                return "Everett Transit";
        }
        return String.format("Unknown Agency: %s", mAgency);
    }

    @Override
    public String getShortAgencyName() {
        switch ((int) mAgency) {
            case OrcaTransitData.AGENCY_CT:
                return "CT";
            case OrcaTransitData.AGENCY_KCM:
                return "KCM";
            case OrcaTransitData.AGENCY_PT:
                return "PT";
            case OrcaTransitData.AGENCY_ST:
                return "ST";
            case OrcaTransitData.AGENCY_WSF:
                return "WSF";
            case OrcaTransitData.AGENCY_ET:
                return "ET";
        }
        return String.format("Unknown Agency: %s", mAgency);
    }

    @Override
    public String getRouteName() {
        if (isLink()) {
            return "Link Light Rail";
        } else if (isSounder()) {
            return "Sounder Train";
        } else {
            // FIXME: Need to find bus route #s
            if (mAgency == OrcaTransitData.AGENCY_ST) {
                return "Express Bus";
            } else if (mAgency == OrcaTransitData.AGENCY_KCM) {
                return "Bus";
            }
            return null;
        }
    }

    @Override
    public String getFareString() {
        return NumberFormat.getCurrencyInstance(Locale.US).format(mFare / 100.0);
    }

    @Override
    public boolean hasFare() {
        return true;
    }

    @Override
    public String getBalanceString() {
        return NumberFormat.getCurrencyInstance(Locale.US).format(mNewBalance / 100);
    }

    @Override
    public Station getStartStation() {
        if (isLink()) {
            return LINK_STATIONS.get(mCoachNum);
        } else if (isSounder()) {
            return sSounderStations.get((int) mCoachNum);
        } else if (mAgency == OrcaTransitData.AGENCY_WSF) {
            return sWSFTerminals.get((int) mCoachNum);
        }
        return null;
    }

    @Override
    public String getStartStationName() {
        if (isLink()) {
            if (LINK_STATIONS.containsKey(mCoachNum)) {
                return LINK_STATIONS.get(mCoachNum).getStationName();
            } else {
                return String.format("Unknown Station #%s", mCoachNum);
            }
        } else if (isSounder()) {
            int stationNumber = (int) mCoachNum;
            if (sSounderStations.containsKey(stationNumber)) {
                return sSounderStations.get(stationNumber).getStationName();
            } else {
                return String.format("Unknown Station #%s", stationNumber);
            }
        } else if (mAgency == OrcaTransitData.AGENCY_WSF) {
            int terminalNumber = (int) mCoachNum;
            if (sWSFTerminals.containsKey(terminalNumber)) {
                return sWSFTerminals.get(terminalNumber).getStationName();
            } else {
                return String.format("Unknown Terminal #%s", terminalNumber);
            }
        } else {
            return String.format("Coach #%s", String.valueOf(mCoachNum));
        }
    }

    @Override
    public String getEndStationName() {
        // ORCA tracks destination in a separate record
        return null;
    }

    @Override
    public Station getEndStation() {
        // ORCA tracks destination in a separate record
        return null;
    }

    @Override
    public Mode getMode() {
        if (isLink()) {
            return Mode.METRO;
        } else if (isSounder()) {
            return Mode.TRAIN;
        } else if (mAgency == OrcaTransitData.AGENCY_WSF) {
            return Mode.FERRY;
        } else {
            return Mode.BUS;
        }
    }

    @Override
    public boolean hasTime() {
        return true;
    }

    public long getCoachNumber() {
        return mCoachNum;
    }

    public long getTransType() {
        return mTransType;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mTimestamp);
        parcel.writeLong(mCoachNum);
        parcel.writeLong(mFare);
        parcel.writeLong(mNewBalance);
        parcel.writeLong(mAgency);
        parcel.writeLong(mTransType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private boolean isLink() {
        return (mAgency == OrcaTransitData.AGENCY_ST && mCoachNum > 10000);
    }

    private boolean isSounder() {
        return (mAgency == OrcaTransitData.AGENCY_ST && mCoachNum < 20);
    }
}
