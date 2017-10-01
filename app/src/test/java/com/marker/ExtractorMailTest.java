package com.marker;

import com.marker.util.ExtractorMail;

import org.junit.Assert;
import org.junit.Test;

public class ExtractorMailTest {

    @Test
    public void extraerTest() {
        String mail = "Google <no-reply@accounts.google.com>";
        ExtractorMail extractor = new ExtractorMail();
        Assert.assertEquals("no-reply@accounts.google.com", extractor.extraer(mail));
    }
}
