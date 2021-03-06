package com.marker;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.marker.lugar.destino.DestinoActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LevantarLugaresTest {

    @Rule
    public ActivityTestRule<DestinoActivity> rule = new ActivityTestRule<>(
            DestinoActivity.class);

    @Test
    public void run() throws Exception {
        //Pequenio truco para levantar un test de ui automatizado pero que no se cierre al instante.
        Thread.sleep(500000);
    }
}
