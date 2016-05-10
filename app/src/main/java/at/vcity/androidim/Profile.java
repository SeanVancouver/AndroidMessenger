package at.vcity.androidim;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.vcity.androidim.interfaces.IAppManager;
import at.vcity.androidim.services.IMService;
import at.vcity.androidim.tools.FriendController;
import at.vcity.androidim.types.FriendInfo;
import at.vcity.androidim.types.MessageInfo;
import at.vcity.androidim.types.STATUS;

/**
 * Created by luoxiao on 2016-04-27.
 */
public class Profile extends AppCompatActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 9002;
    ImageView ImageHolder, ImageHolder1, ImageHolder2;
    String ClickedUser, CurrentUserId;
    boolean isImageFitToScreen;
    Context context;
    public static Uri selectedImage;
    public static Bitmap bitmapd, bitmapd1, bitmapd2;
    String Concac, Concac1, Concac2;
    private IAppManager imService;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            imService = ((IMService.IMBinder)service).getService();
            System.out.println(" Profile onServiceConnected ");
        }

        public void onServiceDisconnected(ComponentName className) {
            imService = null;
            Toast.makeText(Profile.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);
        bitmapd = null;

        ClickedUser = getIntent().getExtras().getString("json_data");
        CurrentUserId = getIntent().getExtras().getString("json_userKey");

        Concac = CurrentUserId.concat("zza");
        Concac1 = CurrentUserId.concat("zzb");
        Concac2 = CurrentUserId.concat("zzc");

        ImageHolder = (ImageView) findViewById(R.id.ImageHolder);
        ImageHolder1 = (ImageView) findViewById(R.id.ImageHolder1);
        ImageHolder2 = (ImageView) findViewById(R.id.ImageHolder2);

        ImageHolder.setOnClickListener(this);
        ImageHolder1.setOnClickListener(this);
        ImageHolder2.setOnClickListener(this);

        TextView tv = (TextView) findViewById(R.id.editText);
        tv.setText(ClickedUser);

        new DisplayImage(Concac).execute();
        new DisplayImage1(Concac1).execute();
        new DisplayImage2(Concac2).execute();

        String mapKey = CurrentUserId;

        TextView age = (TextView)findViewById(R.id.editTextAgeV);
        String ageText = FriendList.hashmap.get(mapKey);
        age.setText(ageText);

        TextView sex = (TextView)findViewById(R.id.editTextSexV);
        String sexText = FriendList.hashmap1.get(mapKey);
        sex.setText(sexText);

        TextView bio = (TextView)findViewById(R.id.editTextBioV);
        String bioText = FriendList.hashmap2.get(mapKey);
        bio.setText(bioText);

        //int test =24;  We are using map key so this not needed.
        //LoadProfileInfo(test);

        ArrayList<String> mainPicStore = new ArrayList<String>();

        //All testing below
        mainPicStore.add("list");
        mainPicStore.add("list2");
        System.out.println(FriendList.mainPicStore + " Profile onCreate temp ");
        FriendInfo[] friends = FriendController.getFriendsInfo();
    }

    /*
    public void onImageClick(View view){
        Intent im = new Intent(this, FullImage.class);
        startActivity(im);
    }*/

    public void onChatClick(View view) {
        if(ClickedUser.equals(Login.currentUser)){Toast.makeText(getApplicationContext(),"Cannot chat with yourself!", Toast.LENGTH_LONG).show();}
        else{
        Intent intent = new Intent(this, Messaging.class);
        intent.putExtra(FriendInfo.USERNAME, getIntent().getExtras().getString("json_data"));
        startActivity(intent);}
    }

    public void onClick(View v) {
        String Concac;
        Context context = v.getContext();
        switch (v.getId()) {

            case R.id.Add1:
                Concac = CurrentUserId.concat("zza");
                Intent i = new Intent(context, ImagePopUp.class);
                i.putExtra("photo_id", Concac);
                startActivity(i);
                break;

            case R.id.Add2:
                Concac = CurrentUserId.concat("zzb");
                Intent i2 = new Intent(context, ImagePopUp.class);
                i2.putExtra("photo_id", Concac);
                startActivity(i2);
                break;

            case R.id.Add3:
                Concac = CurrentUserId.concat("zzc");
                Intent i3 = new Intent(context, ImagePopUp.class);
                i3.putExtra("photo_id", Concac);
                startActivity(i3);
                break;

            //Full screen of photo
            case R.id.ImageHolder:
                Concac = CurrentUserId.concat("zza");
                Intent im = new Intent(this, FullImage.class);
                im.putExtra("photo_id", Concac);
                startActivity(im);
                break;

            case R.id.ImageHolder1:
                Concac = CurrentUserId.concat("zzb");
                Intent im1 = new Intent(this, FullImage.class);
                im1.putExtra("photo_id", Concac);
                startActivity(im1);
                break;

            case R.id.ImageHolder2:
                Concac = CurrentUserId.concat("zzc");
                Intent im2 = new Intent(this, FullImage.class);
                im2.putExtra("photo_id", Concac);
                startActivity(im2);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            //Intent i = new Intent(this, ImagePopUp.class);
            //i.putExtra("passToPopUp", selectedImage);
            //imageToUplaod.setImageURI(null);
            ImageHolder.setImageURI(selectedImage);
        }
    }

    private String getEncodedData(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(data.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (sb.length() > 0)
                sb.append("&");

            sb.append(key + "=" + value);
        }
        return sb.toString();
    }


    private class DisplayImage extends AsyncTask<Void, Void, Bitmap> {
        String name;

        public DisplayImage(String name) {
            this.name = name;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap;
            try {
                URL url = new URL(Secret.DisplayPicturesSec + name + ".JPG");
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

            if (bitmap != null) {
                ImageHolder.setImageBitmap(bitmap);
            }
            bitmapd = bitmap;
        }
    }

    private class DisplayImage1 extends AsyncTask<Void, Void, Bitmap> {
        String name;

        public DisplayImage1(String name) {
            this.name = name;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap;

            try {
                URL url = new URL(Secret.DisplayPicturesSec + name + ".JPG");
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

            if (bitmap != null) {
                ImageHolder1.setImageBitmap(bitmap);
            }
            bitmapd1 = bitmap;
        }
    }

    private class DisplayImage2 extends AsyncTask<Void, Void, Bitmap> {
        String name;

        public DisplayImage2(String name) {
            this.name = name;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap;
            try {
                URL url = new URL(Secret.DisplayPicturesSec + name + ".JPG");
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
            if (bitmap != null) {
                ImageHolder2.setImageBitmap(bitmap);
            }
            bitmapd2 = bitmap;
        }
    }

    @Override
    protected void onResume() {
        bindService(new Intent(Profile.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);

        super.onResume();
    }


    @Override
    protected void onPause()
    {
        unbindService(mConnection);
        super.onPause();
    }
}
