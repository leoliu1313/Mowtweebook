package com.example.chinyao.mow.mowtweebook.model;

import org.parceler.Parcel;

/**
 * Created by chinyao on 8/7/2016.
 */

@Parcel
public class MowtweebookMedia {
	String media_url;
	MowtweebookSizes sizes;

	public MowtweebookMedia() {}

	public String getMedia_url() {
		return media_url;
	}

	public MowtweebookSizes getSizes() {
		return sizes;
	}
}
