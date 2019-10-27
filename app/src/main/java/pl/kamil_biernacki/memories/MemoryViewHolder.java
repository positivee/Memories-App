package pl.kamil_biernacki.memories;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.net.URI;

public class MemoryViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public LinearLayout root;
    TextView textTitle, textTime, textContent;
    ImageView imageView;

    public MemoryViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;

        root = itemView.findViewById(R.id.list_root);

        textTitle = mView.findViewById(R.id.list_title);
        textTime = mView.findViewById(R.id.list_date);
        textContent = mView.findViewById(R.id.list_desc);
        imageView = mView.findViewById(R.id.list_img);


    }

    public void setMemoryImg(String img) {

            Picasso.get().load(img).into(imageView);

    }


    public void setMemoryTitle(String title){
        textTitle.setText(title);
    }

    public void setMemoryTime(String time){
        textTime.setText(time);
    }

    public void setMemoryContent(String content){
        textContent.setText(content);
    }




}
