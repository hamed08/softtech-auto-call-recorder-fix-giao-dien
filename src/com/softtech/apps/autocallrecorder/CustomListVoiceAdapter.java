package com.softtech.apps.autocallrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxFileInfo;
import com.softtech.apps.constant.Constant;
import com.softtech.apps.sync.android.util.ContactListComparator;

public class CustomListVoiceAdapter extends BaseAdapter implements Filterable {

	Context context;
	private static final String AUDIO_RECORDER_FOLDER = "allcalls";
	private static final String AUDIO_RECORDER_FOLDER_FAVORITES = "favorites";
	static List<RowVoiceRecorded> rowVoiceRecorded = new ArrayList<RowVoiceRecorded>();
	List<RowVoiceRecorded> database = new ArrayList<RowVoiceRecorded>();
	private ContactListComparator mSortComparator;
	private File folder;
	private File files[];

	private File folder_favorite;
	private File files__favorites[];

	private List<Contact> listContact;

	public CustomListVoiceAdapter(Context context, int type) {
		// TODO Auto-generated constructor stub
		this.context = context;
		listContact = new ArrayList<Contact>();
		// Log.d("ADAPTER","Type = "+type);
		getContacts();
		database.clear();
		
		// return if SDCard not avaiable
		boolean mounted = false;
		if((MainActivity.updateExternalStorageState() == MainActivity.MEDIA_MOUNTED)){
			mounted = true;
		}
		if(!mounted)
			return;
		// Read all favorites file
		String filepath_favorite = Environment.getExternalStorageDirectory()
				.getPath();
		folder_favorite = new File(filepath_favorite, "softtech/"
				+ AUDIO_RECORDER_FOLDER_FAVORITES);
		if (!folder_favorite.exists()) {
			folder_favorite.mkdirs();
		}
		files__favorites = folder_favorite.listFiles();
		//Log.d("TOTAL", "So file o folder favorites = "+files__favorites.length);
		if (!files__favorites.equals(null)) {
			for (File a : files__favorites) {
				boolean matched = false; // Di chuyen vi bien danh dau
				String ss[] = a.getName().split("-");
				boolean isSync = false;
				if (ss[2].charAt(ss[2].length() - 1) == 1) {
					isSync = true;
				}

				int i = 0;
				for (Contact aContact : listContact) {
					if (ss[1].trim() != null
							&& aContact.get_phone_number().trim()
									.contains(ss[1].trim())) {

						RowVoiceRecorded voice1 = new RowVoiceRecorded(listContact.get(i)
								.get_name(), a.getAbsolutePath(),
								Long.parseLong(ss[0].trim()), ss[1].trim(),
								isSync);
						database.add(voice1);
						matched = true;
						break;
					}
					i++;
				}
				if (!matched) {

					RowVoiceRecorded voice2 = new RowVoiceRecorded("Unknown",
							a.getAbsolutePath(), Long.parseLong(ss[0].trim()),
							ss[1].trim(), isSync);
					database.add(voice2);
				}	
				
			}
		}

		// Read all file in folder and add it into listview
		if (type == 0) {
			String filepath = Environment.getExternalStorageDirectory()
					.getPath();
			folder = new File(filepath, "softtech/" + AUDIO_RECORDER_FOLDER);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			files = folder.listFiles();
			//Log.d("TOTAL", "So file o folder allcalls = "+files.length);
			if (!files.equals(null)) {
				
				int ii=1;
				for (File a : files) {
					boolean matched = false;// Di chuyen vi bien danh dau
					//Log.d("FILE","## Lan thu"+ii+" File name = "+a.getName());
					String ss[] = a.getName().split("-");

					boolean isSync = false;
					if (ss[2].charAt(ss[2].length() - 1) == 1) {
						isSync = true;
					}

					int i = 0;
					for (Contact aContact : listContact) {
						if (ss[1].trim() != null
								&& aContact.get_phone_number().trim()
										.contains(ss[1].trim())) {

							RowVoiceRecorded voice1 = new RowVoiceRecorded(listContact.get(i)
									.get_name(), a.getAbsolutePath(),
									Long.parseLong(ss[0].trim()), ss[1].trim(),
									isSync);
							database.add(voice1);
							matched = true;
							break;
						}
						i++;
					}
					if (!matched) {

						RowVoiceRecorded voice2 = new RowVoiceRecorded("Unknown",
								a.getAbsolutePath(), Long.parseLong(ss[0]
										.trim()), ss[1].trim(), isSync);
						database.add(voice2);
					}
					ii++;
				}
			}
		}
		//Log.d("RECORD", "Tong so file record doc trong folder ="+database.size());
		if(database != null){
			
			mSortComparator = new ContactListComparator();
			
			Collections.sort(database, mSortComparator);
		}
		
		rowVoiceRecorded = database;
		
	}

	public Boolean removeItem(int position) {
		String file_path = rowVoiceRecorded.get(position).getmPath();
		File file = new File(file_path);
		boolean deleted = file.delete();
		if (deleted) {
			rowVoiceRecorded.remove(position);
			notifyDataSetChanged();
		}
		return deleted;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(rowVoiceRecorded == null || rowVoiceRecorded.equals(null) || rowVoiceRecorded.size() == 0){
			return 1;
		}
		return rowVoiceRecorded.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(rowVoiceRecorded == null || rowVoiceRecorded.equals(null) || rowVoiceRecorded.size() == 0){
			return null;
		}
		return rowVoiceRecorded.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		if(rowVoiceRecorded == null || rowVoiceRecorded.equals(null) || rowVoiceRecorded.size() == 0){
			return 0;
		}
		return rowVoiceRecorded.indexOf(getItem(position));
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		// TODO Auto-generated method stub
		View row = convertView;
		ViewHolder viewHolder = null;
		if(rowVoiceRecorded == null || rowVoiceRecorded.equals(null) || rowVoiceRecorded.size() == 0){
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			row = mInflater.inflate(R.layout.item_search_notfound, null);
			return row;
		}else{
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			row = mInflater.inflate(R.layout.list_voice_custom, null);
			viewHolder = new ViewHolder();

			viewHolder.imgAvatar = (ImageView) row
					.findViewById(R.id.imgAvatar);
			viewHolder.contactName = (TextView) row
					.findViewById(R.id.tvContactName);
			viewHolder.phoneNumber = (TextView) row
					.findViewById(R.id.tvPhoneNumber);
			viewHolder.dateTime = (TextView) row
					.findViewById(R.id.tvTime);
			viewHolder.duration = (TextView) row
					.findViewById(R.id.tvTime);
			viewHolder.imgCloud = (ImageView) row
					.findViewById(R.id.imgCloud);

			row.setTag(viewHolder);
		}
		if (row == null) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			row = mInflater.inflate(R.layout.list_voice_custom, null);
			viewHolder = new ViewHolder();

			viewHolder.imgAvatar = (ImageView) row
					.findViewById(R.id.imgAvatar);
			viewHolder.contactName = (TextView) row
					.findViewById(R.id.tvContactName);
			viewHolder.phoneNumber = (TextView) row
					.findViewById(R.id.tvPhoneNumber);
			viewHolder.dateTime = (TextView) row
					.findViewById(R.id.tvTime);
			viewHolder.duration = (TextView) row
					.findViewById(R.id.tvTime);
			viewHolder.imgCloud = (ImageView) row
					.findViewById(R.id.imgCloud);

			row.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) row.getTag();
		}

		RowVoiceRecorded row_pos = rowVoiceRecorded.get(position);

		if (row_pos.getmPath().contains(Constant.ISSYNC0)) {
			rowVoiceRecorded.get(position).setIsSync(false);
			try {
				viewHolder.imgCloud.setImageResource(R.drawable.cloud);
			} catch (Exception e) {
				// TODO: handle exception
			}
				
		} else {
			rowVoiceRecorded.get(position).setIsSync(true);
			try {
				viewHolder.imgCloud.setImageResource(R.drawable.home_cloud);
			} catch (Exception e) {
				// TODO: handle exception
			}
				
		}

		// setting the image resource and title
		try {
			viewHolder.imgAvatar.setImageResource(R.drawable.home_noavatar_male);
			viewHolder.contactName.setText(row_pos.getmName());
		} catch (Exception e) {
			// TODO: handle exception
		}

		long timeCreate = row_pos.getmTimeCreate();
		
		String time = String.valueOf(timeCreate);
		
		// rename file
		String fileName[] = time.split("-");
		
		String name = "";
		// date
		// month
		name += fileName[0].substring(4, 6) + "-";
		// day
		name += fileName[0].substring(6, 8) + "-";
		// year
		name += fileName[0].substring(0, 4);
		
		name += " ";

		// hour
		name += fileName[0].substring(8, 10) + ":";
		name += fileName[0].substring(10, 12) + ":";
		name += fileName[0].substring(12, 14);
		try {
			viewHolder.dateTime.setText(name);
			viewHolder.phoneNumber.setText(row_pos.getmPhoneNumber());
		} catch (Exception e) {
			// TODO: handle exception
		}
		// duration.setText(row_pos.getmDuration());
		return row;
	}
	
	void dropData() {
		rowVoiceRecorded.removeAll(rowVoiceRecorded);
		notifyDataSetChanged();
	}

	public void getContacts() {
		// Get all contact here
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		cur.moveToFirst();

		while (cur.moveToNext()) {
			String contactId = cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts._ID));

			// Log.e("contact id"," contact id="+contactId);

			String name = cur
					.getString(cur
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

			// String
			// phone=cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			// Log.e("name","name="+name);

			String hasPhone = null;
			int hasphone = -1;
			try {
				hasPhone = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				hasphone = Integer.parseInt(hasPhone);
				// Log.e("contactID",contactId);
			} catch (Exception ex) {

				// Log.e("contactID",contactId);
			}
			if (hasphone > 0) {
				// Log.d("CONTACT", "Has phone number from SIM CARD");
				String phone = cur
						.getString(cur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				Contact ct = new Contact(contactId, name, phone, contactId,0);
				listContact.add(ct);
			}
		}

	}

	
	private static class ViewHolder {
		public TextView contactName;
		public TextView phoneNumber;
		public TextView dateTime;
		public TextView duration;
		public ImageView imgCloud;
		public ImageView imgAvatar;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence charSequence) {
				//Log.d("FILTER", "################## Class filter da duoc goi");
				FilterResults results = new FilterResults();
				// If there's nothing to filter on, return the original data for
				// your list
				if (charSequence == null || charSequence.length() == 0) {
					results.values = database;
					results.count = database.size();
				} else {
					List<RowVoiceRecorded> filterResultsData = new ArrayList<RowVoiceRecorded>();

					// if search details is 0, search fullName, else, search
					// all details

					for (RowVoiceRecorded c : database) {
						if (c.getmName().toLowerCase(Locale.ENGLISH)
								.contains(charSequence)) {
							filterResultsData.add(c);
						}
					}

					results.values = filterResultsData;
					results.count = filterResultsData.size();
				}

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence charSequence,
					FilterResults filterResults) {
				// set the data to the filter results and notifyDataSetChanged()
				rowVoiceRecorded = (List<RowVoiceRecorded>) filterResults.values;
				notifyDataSetChanged();
				Log.d("ADAPTER", "############### Ket qua tra ve");
			}
		};
	}
}
