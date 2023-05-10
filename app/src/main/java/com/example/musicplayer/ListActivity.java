package com.example.musicplayer;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.musicplayer.R;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    public static ImageView list_view_backgroundImage;
    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private ArrayList<String> mData = new ArrayList<>();
    private View listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        list_view_backgroundImage = findViewById(R.id.list_view_backgroundImage);
        switch(MainActivity.backgroundImageNumber){
            case 0:
                list_view_backgroundImage.setImageResource(R.drawable.background);
                break;
            case 1:
                list_view_backgroundImage.setImageResource(R.drawable.metal);
                break;
            case 2:
                list_view_backgroundImage.setImageResource(R.drawable.fracture);
                break;
            case 3:
                list_view_backgroundImage.setImageResource(R.drawable.universe);
                break;
            case 4:
                list_view_backgroundImage.setImageResource(R.drawable.science);
        }

        for(int i = 0;i<MainActivity.musicTitle_vector.size();i++){
            mData.add(Integer.toString(i+1)+". "+MainActivity.musicTitle_vector.get(i));
        }

        // 連結元件
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        listView = findViewById(R.id.list_view);
        // 設置RecyclerView為列表型態
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 設置格線
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 將資料交給adapter
        adapter = new MusicAdapter(mData);
        // 設置adapter給recycler_view
        recyclerView.setAdapter(adapter);
    }

    public void ShowSelectedSong(View view) {
        Snackbar.make(listView,"Could not load audio file.", Snackbar.LENGTH_LONG).show();
    }

    public void GoBack(View view) {
        finish();
    }
}
