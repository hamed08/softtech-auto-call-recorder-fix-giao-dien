package com.softtech.apps.autocallrecorder;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("NewApi")
public class optionFrament3 extends Fragment {
	private AdView adView;
	public optionFrament3() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater .inflate(R.layout.option3, container, false);
		adView = (AdView) rootView.findViewById(R.id.adView);
		if(hasConnections()){
			// Look up the AdView as a resource and load a request
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					AdRequest adRequest = new AdRequest.Builder().build();
				    adView.loadAd(adRequest);
				}
			}, 10000); // Time > 10s.  
		}
		Button btRate = (Button) rootView.findViewById(R.id.btRateApp);
		TextView aboutus = (TextView) rootView.findViewById(R.id.tvAboutTitle);
		Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/VINHAN.TTF");
		aboutus.setTypeface(font);
		btRate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rate();
			}
		});
		
        return rootView;
	}
	public void rate() {
		  Intent intent = new Intent(Intent.ACTION_VIEW);
		  String market_link = getActivity().getApplicationContext().getPackageName();
		  intent.setData(Uri.parse("market://details?id="+market_link));
		  startActivity(intent);
	}
	public boolean hasConnections() {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (null == ni)
			return false;
		return ni.isConnectedOrConnecting();
	}
}
