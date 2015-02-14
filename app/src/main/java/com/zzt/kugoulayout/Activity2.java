package com.zzt.kugoulayout;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.zzt.library.KugouLayout;

/**
 * Created by zzt on 2015/2/14.
 */
public class Activity2 extends ActionBarActivity {

    private KugouLayout kugouLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);

        kugouLayout = (KugouLayout)findViewById(R.id.kugoulayout);

        kugouLayout.setContentView(this.getLayoutInflater().inflate(R.layout.activity_main, null));
        kugouLayout.setAnimType(KugouLayout.REBOUND_ANIM);
        kugouLayout.addHorizontalScrollableView(kugouLayout.findViewById(R.id.horizontalScrollView));
        kugouLayout.setLayoutCloseListener(new KugouLayout.LayoutCloseListener() {
            @Override
            public void onLayoutClose() {
                System.out.println("you do something when layout close");
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
