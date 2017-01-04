package com.example.testing.readbug;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import Bean.Article;

/**
 * Created by lin on 2016/12/29.
 */

public class ArticleAdapter extends ArrayAdapter<Article> {
    private int id;

    public ArticleAdapter(Context context,int textViewResourceId, List<Article> objects) {
        super(context,textViewResourceId, objects);
        id = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Article article = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(id,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view.findViewById(R.id.art_icon);
            viewHolder.title = (TextView) view.findViewById(R.id.art_title);
            viewHolder.pageView = (TextView) view.findViewById(R.id.art_pageView);
            viewHolder.type = (TextView)view.findViewById(R.id.art_type);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.title.setText(article.getTitle());
        viewHolder.type.setText(article.getType());
        viewHolder.imageView.setImageResource(getImageById.get(article.getId()));
        viewHolder.pageView.setText(article.getPageView()+"阅读");
        return view;
    }

    class ViewHolder{
        ImageView imageView;
        TextView title;
        TextView pageView;
        TextView type;
    }
}
