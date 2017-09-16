package com.marker.history;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class History {
    String location;
    Date date;

    History(String location, Date date){
        this.location = location;
        this.date = date;
    }

    public static final List<History> initializeData(){
        List<History> histories = new ArrayList<>();
        histories.add(new History("Udaondo 307 - CABA", new GregorianCalendar(2017, Calendar.JULY, 11).getTime()));
        histories.add(new History("Medrano 951 - CABA", new GregorianCalendar(2017, Calendar.AUGUST, 16).getTime()));
        return histories;
    }
}
