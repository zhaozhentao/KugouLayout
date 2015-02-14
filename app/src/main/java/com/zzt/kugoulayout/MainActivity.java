package com.zzt.kugoulayout;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.zzt.library.CircleSwipeLayout;
import com.zzt.library.CircleSwipeLayout2;


public class MainActivity extends ActionBarActivity {

    CircleSwipeLayout2 circleSwipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleSwipeLayout = CircleSwipeLayout2.attach(this);
        circleSwipeLayout.addHorizontalScrollableView(findViewById(R.id.horizontalScrollView));
        circleSwipeLayout.setLayoutCloseListener(new CircleSwipeLayout2.LayoutCloseListener() {
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
                circleSwipeLayout.setAnimType(CircleSwipeLayout2.NORMAL_ANIM);
                return true;
            case R.id.action_rebound_anim:
                circleSwipeLayout.setAnimType(CircleSwipeLayout2.REBOUND_ANIM);
                return true;
            case R.id.action_always_rebound_anim:
                circleSwipeLayout.setAnimType(CircleSwipeLayout2.ALWAYS_REBOUND);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
