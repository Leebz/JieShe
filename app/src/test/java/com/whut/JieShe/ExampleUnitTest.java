package com.whut.JieShe;

import com.whut.JieShe.utils.TimeUtils;

import org.junit.Test;

import java.util.Date;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        System.out.print(TimeUtils.getDayAndTimeName(new Date().getTime()));
    }
}