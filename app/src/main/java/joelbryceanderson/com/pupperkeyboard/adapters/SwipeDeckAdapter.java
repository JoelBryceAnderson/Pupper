package joelbryceanderson.com.pupperkeyboard.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import joelbryceanderson.com.pupperkeyboard.R;

/**
 * Created by JAnderson on 3/3/17.
 */

public class SwipeDeckAdapter extends BaseAdapter {

    private List<String> data;
    private Context context;

    public SwipeDeckAdapter(List<String> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addPupper(String pupper) {
        data.add(pupper);
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // normally use a viewholder
            v = inflater.inflate(R.layout.card_view, parent, false);
        }

        ImageView pupperPicture = (ImageView) v.findViewById(R.id.pupper_picture);
        String pupperUrl = data.get(position);
        showPupperImage(pupperUrl, pupperPicture);

        return v;
    }

    private void showPupperImage(String pupperUrl, ImageView imageView) {
        Picasso.with(context)
                .load(pupperUrl)
                .into(imageView);

        Log.e("Pupper URL", pupperUrl);
    }
}
