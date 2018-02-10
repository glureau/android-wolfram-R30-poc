package com.glureau.wolfram30.encryption;

import org.junit.Test;

/**
 * Created by greg on 09/02/2018.
 */

public class TestMe {
    @Test
    public void  doubleErrors() {
        double d = 5.6 + 5.8;
        System.out.println(d + "$");
        d *= 100;
        System.out.println("prepare to Localytics with *100d: " + d);
        System.out.println("cast as long: " + (long) d);
        System.out.println("round as long: " + Math.round(d));
    }

}
