package com.marker.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractorMail {
    public String extraer(String mail) {
        Pattern pattern = Pattern.compile(".*<([^']*)>.*");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return mail;
    }
}
