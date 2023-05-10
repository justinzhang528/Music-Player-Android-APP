package com.example.musicplayer;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicplayer.R;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<String> mMusicList;

    MusicAdapter(List<String> data) {
        mMusicList = data;
    }

    // 建立ViewHolder
    class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // 宣告元件
        private TextView musicTitleItem;
        final MusicAdapter mAdapter;

        MusicViewHolder(View itemView, MusicAdapter mAdapter) {
            super(itemView);
            musicTitleItem = (TextView) itemView.findViewById(R.id.music_title);
            this.mAdapter = mAdapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            MainActivity.musicIndex = getLayoutPosition();
            MainActivity.PrepareMusicPlayer();
            MainActivity.btn_play.setImageResource(R.drawable.ic_pause);
            MainActivity.headTitleTextView.setText("Now Playing");
            MainActivity.mp.start();
            MainActivity.rotateTheDisk();
            Snackbar.make(itemView,"\" "+MainActivity.musicTitle_vector.get(MainActivity.musicIndex)+" \"  is Playing Now", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 連結項目布局檔list_item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new MusicViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        // 設置txtItem要顯示的內容
        holder.musicTitleItem.setText(mMusicList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }
}
