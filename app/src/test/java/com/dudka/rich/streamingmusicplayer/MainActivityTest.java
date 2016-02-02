package com.dudka.rich.streamingmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by rich on 1/12/16.
 */

@RunWith(MockitoJUnitRunner.class)
public class MainActivityTest {

    @Mock
    Activity mMockActivity;
    View mMockView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProgressBar() throws Exception
    {
    }

    @Test
    public void testHandleVolley() throws Exception
    {

    }

    @Test
    public void testHandleNetworkError() throws Exception
    {

    }
}