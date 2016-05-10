package at.vcity.androidim;

import android.app.Activity;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import at.vcity.androidim.interfaces.IAppManager;
import at.vcity.androidim.services.IMService;
import at.vcity.androidim.tools.FriendController;
import at.vcity.androidim.types.FriendInfo;
import at.vcity.androidim.types.STATUS;


public class FriendList extends Activity
{
	public static final int SIGN_UP_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	public static final int LOGOUT = Menu.FIRST+2;
	private IAppManager imService = null;
	private FriendListAdapter friendAdapter;
	ListView listView;
	GridView textView;
	Bitmap mainPic;

	public static ArrayList<Bitmap> mainPicStore;
	public String ownusername = new String();
	public static HashMap<String,String> hashmap,hashmap1,hashmap2, hashmap3;

	private class FriendListAdapter extends BaseAdapter
	{
		Context context;
		ImageView iconB;
		class ViewHolder {
			TextView text;
			ImageView icon;
		}
		private LayoutInflater mInflater;
		private Bitmap mOnlineIcon;
		private Bitmap mOfflineIcon;

		private FriendInfo[] friends = null;

		//The constructor
		public FriendListAdapter(Context context, int resource) {
			this.context = context;
			mInflater = LayoutInflater.from(context);

			mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.greenstar);
			mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.redstar);
		}

		public void setFriendList(FriendInfo[] friends)
		{
			this.friends = friends;
		}


		public int getCount() {
			return friends.length; //issue
		}


		public FriendInfo getItem(int position) {
			return friends[position];
		}

		public long getItemId(int position) {

			return 0;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {

			convertView = mInflater.inflate(R.layout.friend_list_screen, null);
			iconB = (ImageView)convertView.findViewById(R.id.iconB);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			text.setText(friends[position].userName);
			text.bringToFront();
			icon.setImageBitmap(friends[position].status == STATUS.ONLINE ? mOnlineIcon : mOfflineIcon);

			hashmap.put(friends[position].userKey, friends[position].age);
			hashmap1.put(friends[position].userKey,friends[position].sex);
			hashmap2.put(friends[position].userKey,friends[position].bio);
			hashmap3.put(friends[position].userName,friends[position].userKey);

			System.out.println(" FriendList getView " + friends[position].userKey + " userKey ");

			try{mainPic = new DisplayImage1(friends[position].userKey).execute().get();}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			iconB.setImageBitmap(mainPic);
			System.out.println(" FriendList getView " + mainPic + " mainPic ");

			iconB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {

					FriendInfo friend = friendAdapter.getItem(position);
					Intent i = new Intent(context, Profile.class);
					i.putExtra("json_data", friend.userName);
					i.putExtra("json_userKey", friend.userKey);
					startActivity(i);
				}
			});

			text.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {

					FriendInfo friend = friendAdapter.getItem(position);
					Intent i = new Intent(context, Profile.class);
					i.putExtra("json_data", friend.userName);
					i.putExtra("json_userKey", friend.userKey);
					startActivity(i);
				}
			});
			//iconB.setImageBitmap(mainPic);

			return convertView;
		}

	}

	///INSERT ABOVE }

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("Broadcast receiver ", "received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(IMService.FRIEND_LIST_UPDATED))
				{
					FriendList.this.updateData(FriendController.getFriendsInfo(),
							FriendController.getUnapprovedFriendsInfo());
				}
			}
		}
	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service)  {
			imService = ((IMService.IMBinder)service).getService();

			FriendInfo[] friends = FriendController.getFriendsInfo();

			System.out.println(friends + " FriendList ServiceConnection friends " + friends);

			if (friends != null) {
				FriendList.this.updateData(friends, null); // parseFriendInfo(friendList);
			}

			setTitle(imService.getUsername() + "'s friend list");
			ownusername = imService.getUsername();

			textView.setAdapter(friendAdapter);
			//listView.setAdapter(friendAdapter); //added
		}

		public void onServiceDisconnected(ComponentName className) {
			imService = null;
			System.out.println(" FriendList.java onServiceDisconnected ");
			Toast.makeText(FriendList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};

	public void updateData(FriendInfo[] friends, FriendInfo[] unApprovedFriends)
	{
		if (friends != null) {
			friendAdapter.setFriendList(friends);
		}
	}

	@Override
	protected void onPause()
	{
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		bindService(new Intent(FriendList.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);

		IntentFilter i = new IntentFilter();
		//i.addAction(IMService.TAKE_MESSAGE);	
		i.addAction(IMService.FRIEND_LIST_UPDATED);

		registerReceiver(messageReceiver, i);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, SIGN_UP_ID, 0, R.string.edit_profile);
		menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);
		menu.add(0, LOGOUT, 0, "Log Out");

		hashmap = new HashMap<String, String>();
		hashmap1 = new HashMap<String, String>();
		hashmap2 = new HashMap<String, String>();
		hashmap3 = new HashMap<String, String>();

		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{

		switch(item.getItemId())
		{
			case SIGN_UP_ID:
			{
				Intent i = new Intent(FriendList.this, ProfileEdit.class);
				startActivity(i);
				return true;
			}
			case EXIT_APP_ID:
			{
				//imService.exit();
				//finish();
				Intent intent = new Intent(this, Login.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				System.out.println("gothrough?");
				intent.putExtra("Exit me", true);
				startActivity(intent);

				finish();
				return true;
			}
			case LOGOUT:
			{
				imService.exit();
				Intent i = new Intent(FriendList.this, Login.class);
				startActivity(i);
				finish();
				return true;
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcomedisplaylayout);
		textView = (GridView)findViewById(R.id.grid_view); //this is the id within the view
		mainPicStore = new ArrayList<Bitmap>();


		//MAKE THE BACKGROUND TASK HERE, THEN HAVE ADAPTER'S GETVIEW RETRIEVE FROM THE ARRAYLIST

		friendAdapter = new FriendListAdapter(this, R.layout.friend_list_screen);

		//listView.setAdapter(friendAdapter); //issue obviously cuz FriendListAdapter did not finish executing!
	}

	private class DisplayImage1 extends AsyncTask<Void,Void, Bitmap> {
		String name;
		public DisplayImage1(String name){
			this.name = name;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap;

			try {
				URL url = new URL("http://secondaccountjfk.comxa.com/pictures/" + name + "zza.JPG");
				//URL url = new URL("http://shawn1234321.netne.net/pictures/" + name + "zza.JPG");
				//URL url = new URL("http://shawn1234321.netne.net/pictures/13zza.JPG");
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
	}

}
