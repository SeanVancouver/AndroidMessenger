package at.vcity.androidim;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by luoxiao on 2016-05-01.
 */
public class FullImage extends AppCompatActivity {
    boolean isImageFitToScreen;
    ImageView ImageHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image);
        ImageHolder = (ImageView) findViewById(R.id.FullImage);
       String Concac = getIntent().getExtras().getString("photo_id");
        new DisplayImage(Concac).execute();
    }

    private class DisplayImage extends AsyncTask<Void,Void, Bitmap> {
        String name;
        public DisplayImage(String name){
            this.name = name;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap;
            try {
                URL url = new URL("http://secondaccountjfk.comxa.com/pictures/" + name + ".JPG");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(1000 * 30);
                con.setReadTimeout(1000 * 30);
                bitmap = BitmapFactory.decodeStream((InputStream) con.getContent(), null, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if(bitmap != null){
                ImageHolder.setImageBitmap(bitmap);
            }

            if(isImageFitToScreen) {
                isImageFitToScreen=false;
                ImageHolder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                ImageHolder.setAdjustViewBounds(true);
            }else{
                isImageFitToScreen=true;
                ImageHolder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                ImageHolder.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }
    }
}
