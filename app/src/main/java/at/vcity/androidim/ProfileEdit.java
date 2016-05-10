package at.vcity.androidim;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Map;

import at.vcity.androidim.interfaces.IAppManager;
import at.vcity.androidim.services.IMService;
import at.vcity.androidim.types.FriendInfo;

/**
 * Created by luoxiao on 2016-05-07.
 */
public class ProfileEdit extends Activity implements View.OnClickListener {
    ImageView ImageHolder, ImageHolder1, ImageHolder2;
    public static Bitmap bitmapd,bitmapd1,bitmapd2;
    private EditText age,sex,bio;
    private IAppManager imService;
    private Handler handler = new Handler();
    //private static final String SERVER_UPDATE_FAILED = "0";
    private static final String SERVER_UPDATE_FAILED = "10";
    private static final String SERVER_UPDATE_SUCCESFULL = "1";
    String CurUserKey;
    String Concac,Concac1,Concac2;
    String ageCache, sexCache, bioCache;
    boolean cache;
    public static final int FRIENDLIST_PG = Menu.FIRST;
    public static final int DELETE_ACCOUNT = Menu.FIRST+1;
    public TextView ProfileOf;

    private ServiceConnection mConnection = new ServiceConnection() {


        public void onServiceConnected(ComponentName className, IBinder service) {
            imService = ((IMService.IMBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            imService = null;
            Toast.makeText(ProfileEdit.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);

        age = (EditText) findViewById(R.id.editTextAge);
        sex = (EditText) findViewById(R.id.editTextSex);
        bio = (EditText) findViewById(R.id.editTextBio);

        ProfileOf = (TextView) findViewById(R.id.textView13);
        ProfileOf.setText(Login.currentUser);

        ImageHolder = (ImageView) findViewById(R.id.ImageHolder);
        ImageHolder1 = (ImageView) findViewById(R.id.ImageHolder1);
        ImageHolder2 = (ImageView) findViewById(R.id.ImageHolder2);

        ImageHolder.setOnClickListener(this);
        ImageHolder1.setOnClickListener(this);
        ImageHolder2.setOnClickListener(this);

        CurUserKey = FriendList.hashmap3.get(Login.currentUser);

        Concac = CurUserKey.concat("zza");
        Concac1 = CurUserKey.concat("zzb");
        Concac2 = CurUserKey.concat("zzc");

        new DisplayImage(Concac).execute();
        new DisplayImage1(Concac1).execute();
        new DisplayImage2(Concac2).execute();

        String mapKey = CurUserKey;
        EditText age = (EditText)findViewById(R.id.editTextAge);
        String ageText = FriendList.hashmap.get(mapKey);


        EditText sex = (EditText)findViewById(R.id.editTextSex);
        String ageSex = FriendList.hashmap1.get(mapKey);


        EditText bio = (EditText)findViewById(R.id.editTextBio);
        String ageBio = FriendList.hashmap2.get(mapKey);

            age.setText(ageText);
            sex.setText(ageSex);
            bio.setText(ageBio);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        menu.add(0, FRIENDLIST_PG, 0, "back to Main page");
        menu.add(0, DELETE_ACCOUNT, 0, "Delete your account");
        return result;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch(item.getItemId())
        {
            case FRIENDLIST_PG:
            {
                Intent i = new Intent(ProfileEdit.this, FriendList.class);
                startActivity(i);
                Toast.makeText(getApplicationContext(),"Please wait", Toast.LENGTH_LONG).show();
                return true;
            }
            case DELETE_ACCOUNT:
            {
                String FirstDeletion = CurUserKey.concat("zza");
                try{new DeleteImage(FirstDeletion).execute().get();}
                catch (Exception e) {
                    e.printStackTrace();
                }
                String SecDeletion = CurUserKey.concat("zzb");
                try{new DeleteImage(SecDeletion).execute().get();}
                catch (Exception e) {
                    e.printStackTrace();
                }
                String ThirdDeletion = CurUserKey.concat("zzc");
                try{new DeleteImage(ThirdDeletion).execute().get();}
                catch (Exception e) {
                    e.printStackTrace();
                }

                Thread thread = new Thread(){
                    public void run() {
                        try {//The action is performed within if statement
                            System.out.println(" ProfileEdit + onMenuItemSelected CurUserKey " + CurUserKey);
                            if (imService.deleteAccount(CurUserKey) == null)
                            {
                                handler.post(new Runnable(){

                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Cannot be deleted", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        } catch(UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                Intent i = new Intent(ProfileEdit.this, Login.class);
                startActivity(i);
                Toast.makeText(getApplicationContext(),"Account deleted", Toast.LENGTH_LONG).show();
            }
        }

        return super.onMenuItemSelected(featureId, item);
    }

    public void firstAddClick(View view){
        String Concac;
        Concac = CurUserKey.concat("zza");
        Context context = view.getContext();
        Intent i = new Intent(context, ImagePopUp.class);
        i.putExtra("photo_id", Concac);
        startActivity(i);
    }

    public void firstDeleteClick(View view){
        String Concac;
        Concac = CurUserKey.concat("zza");
        Context context = view.getContext();
        Intent i = new Intent(context, ImagePopUp.class);
        i.putExtra("photo_id", Concac);
        startActivity(i);
    }


    public void SecondAddClick(View view){
        String Concac;
        Concac = CurUserKey.concat("zzb");
        Context context = view.getContext();
        Intent i = new Intent(context, ImagePopUp.class);
        i.putExtra("photo_id", Concac);
        startActivity(i);
    }

    public void ThirdAddClick(View view){
        String Concac;
        Concac = CurUserKey.concat("zzc");
        Context context = view.getContext();
        Intent i = new Intent(context, ImagePopUp.class);
        i.putExtra("photo_id", Concac);
        startActivity(i);
    }

    public void onClick(View v) {
        String Concac;
        Context context = v.getContext();
        switch (v.getId()) {
            //Full screen of photo
            case R.id.ImageHolder:
                Concac = CurUserKey.concat("zza");
                Intent im = new Intent(this, FullImage.class);
                im.putExtra("photo_id", Concac);
                startActivity(im);
                break;

            case R.id.ImageHolder1:
                Concac = CurUserKey.concat("zzb");
                Intent im1 = new Intent(this, FullImage.class);
                im1.putExtra("photo_id", Concac);
                startActivity(im1);
                break;

            case R.id.ImageHolder2:
                Concac = CurUserKey.concat("zzc");
                Intent im2 = new Intent(this, FullImage.class);
                im2.putExtra("photo_id", Concac);
                startActivity(im2);
                break;
        }
    }

    public void updateProfileClick(View view) {
            Thread thread = new Thread() {
                String result = new String();
                @Override
                public void run() {
                    result = imService.updateUser(age.getText().toString(),
                            sex.getText().toString(),
                            bio.getText().toString());

                    System.out.println(" ProfileEdit updateProfileClick result " + result);

                    handler.post(new Runnable() {
                        String string = "1";

                        public void run() {

                            if (result.equals(SERVER_UPDATE_SUCCESFULL)) { //iii null object 112
                                Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_LONG).show();
                                //showDialog(SIGN_UP_SUCCESSFULL);
                            } else if (result.equals(SERVER_UPDATE_FAILED)) {
                                Toast.makeText(getApplicationContext(), "Update failed", Toast.LENGTH_LONG).show();
                                //showDialog(SIGN_UP_USERNAME_CRASHED);
                            }
                        }

                    });
                }

            };
            thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ageCache = age.getText().toString();
        sexCache = sex.getText().toString();
        bioCache = bio.getText().toString();

        age.setText(ageCache);
        sex.setText(sexCache);
        bio.setText(bioCache);

        cache = false;

       // Intent i = new Intent(this,ProfileEdit.class);
       // startActivity(i);
    }

    @Override
    protected void onResume() {
        bindService(new Intent(ProfileEdit.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);

        super.onResume();
    }


    @Override
    protected void onPause()
    {
        unbindService(mConnection);
        super.onPause();
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

            if(bitmap != null){
                ImageHolder.setImageBitmap(bitmap);
            }
            bitmapd = bitmap;
        }
    }

    private class DisplayImage1 extends AsyncTask<Void,Void, Bitmap>{
        String name;
        public DisplayImage1(String name){
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

            if(bitmap != null){
                ImageHolder1.setImageBitmap(bitmap);
            }
            bitmapd1 = bitmap;
        }
    }

    private class DisplayImage2 extends AsyncTask<Void,Void, Bitmap>{
        String name;
        public DisplayImage2(String name){
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
            if(bitmap != null){
                ImageHolder2.setImageBitmap(bitmap);
            }
            bitmapd2 = bitmap;
        }
    }

    private class DeleteImage extends AsyncTask<Void, Void, String> {
        Bitmap image;
        String name;

        public DeleteImage(String name){
            this.name = name;
        }

        @Override
        protected String doInBackground(Void... params) {
            Map<String,String> dataToSend = new HashMap<>();
           // dataToSend.put("image",encodedImage);
            dataToSend.put("name", name);

            String encodedStr = getEncodedData(dataToSend);

            BufferedReader reader = null;

            try {
                URL url = new URL(Secret.photoDelete);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");

                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();
                Log.i("custom_check", "The values received in the store part are as follows:");
                Log.i("custom_check",line);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return encodedStr;
        }
    }

    private String getEncodedData(Map<String,String> data) {
        StringBuilder sb = new StringBuilder();
        for(String key : data.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(data.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(sb.length()>0)
                sb.append("&");

            sb.append(key + "=" + value);
        }
        return sb.toString();
    }
}
