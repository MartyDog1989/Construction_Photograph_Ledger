package jp.martydog.construction_photograph_ledger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akihiro on 2017/09/11.
 */

public class ListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<Picture> pictureList;

    public ListAdapter(Context context) {
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return pictureList.size();
    }

    @Override
    public Object getItem(int i) {
        return pictureList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return pictureList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = layoutInflater.inflate(R.layout.list_pictures, viewGroup, false);
        }
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(pictureList.get(i).getText());

        byte[] bytes = pictureList.get(i).getBitmapArray();
        if(bytes != null) {
            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }

        return view;
    }
    public void setPictureList(List<Picture> list) {
        pictureList = list;
    }
}
