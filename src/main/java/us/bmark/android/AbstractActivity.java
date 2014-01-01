package us.bmark.android;

import android.app.Activity;
import android.os.Bundle;

public class AbstractActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
