package com.zzt.kugoulayout;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.zzt.KugouLayout.KugouLayout;


public class Activity1 extends ActionBarActivity {

    KugouLayout kugouLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kugouLayout = new KugouLayout(this);
        kugouLayout.attach(this);
        kugouLayout.addHorizontalScrollableView(findViewById(R.id.horizontalScrollView));
        kugouLayout.setLayoutCloseListener(new KugouLayout.LayoutCloseListener() {
            @Override
            public void onLayoutClose() {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_normal_anim:
                kugouLayout.setAnimType(KugouLayout.NORMAL_ANIM);
                return true;
            case R.id.action_rebound_anim:
                kugouLayout.setAnimType(KugouLayout.REBOUND_ANIM);
                return true;
            case R.id.action_always_rebound_anim:
                kugouLayout.setAnimType(KugouLayout.ALWAYS_REBOUND);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
