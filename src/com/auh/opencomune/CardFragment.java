/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.auh.opencomune;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class CardFragment extends Fragment {

	private static final String ARG_POSITION = "position";

	private int position;

	public static CardFragment newInstance(int position) {
		CardFragment f = new CardFragment();
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		position = getArguments().getInt(ARG_POSITION);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(0,0,0,0);
		FrameLayout fl = new FrameLayout(getActivity());
		fl.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
				.getDisplayMetrics());

		ScrollView scorrimento = new ScrollView(getActivity());
		params.setMargins(0, 0, 0, 0);
		scorrimento.setLayoutParams(params);
		scorrimento.setBackgroundResource(android.R.drawable.screen_background_light);
		
		LinearLayout layoutvert = new LinearLayout(getActivity());
		params.setMargins(0,0,0,0);
		layoutvert.setPadding(margin, margin, margin, margin);
		layoutvert.setLayoutParams(params);
			
		switch (position){
		case 0: //notizie
		{
			LinearLayout listanotizie = (LinearLayout) inflater.inflate(R.layout.notizie, null);
			listanotizie.setLayoutParams(params);
			layoutvert.addView(listanotizie);
			break;
		}
		case 1: //segnala
		{
			LinearLayout listasegnala = (LinearLayout) inflater.inflate(R.layout.segnala, null);
			listasegnala.setLayoutParams(params);
			layoutvert.addView(listasegnala);
			break;
		}
		case 2: //modulistica
		{
			LinearLayout listamoduli = (LinearLayout) inflater.inflate(R.layout.moduli, null);
			listamoduli.setLayoutParams(params);
			layoutvert.addView(listamoduli);
			break;
		}
		case 3: //info
		{
			ScrollView infos = (ScrollView) inflater.inflate(R.layout.info, null);
			infos.setLayoutParams(params);
			layoutvert.addView(infos);
			break;
		}
		};
		
		scorrimento.addView(layoutvert);
		fl.addView(scorrimento);
		return fl;
	}
	
	

}