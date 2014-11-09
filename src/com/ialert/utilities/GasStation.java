package com.ialert.utilities;

import android.os.Parcel;
import android.os.Parcelable;

public class GasStation implements Parcelable {
	private String country;
	private String zip;
	private String price;
	private String address;
	private String lat;
	private String lon;
	private String station;
	private String region;
	private String distance;
	//private String city;

	public GasStation() {
		this.country = "";
		this.zip = "";
		this.price = "";
		this.address = "";
		this.lat = "";
		this.lon = "";
		this.station = "";
		this.region = "";
		this.distance = "";
		//this.city = "";
	}

	public GasStation(Parcel in) {
		this.country = in.readString();
		this.zip = in.readString();
		this.price = in.readString();
		this.address = in.readString();
		this.lat = in.readString();
		this.lon = in.readString();
		this.station = in.readString();
		this.region = in.readString();
		this.distance = in.readString();
		//this.city = in.readString();
	}

	/*public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}*/

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		if (station == "null")
			station = "Unknown";
		this.station = station;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		if (distance == "null")
			distance = "Unknown";
		this.distance = distance;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(this.country);
		dest.writeString(this.zip);
		dest.writeString(this.price);
		dest.writeString(this.address);
		dest.writeString(this.lat);
		dest.writeString(this.lon);
		dest.writeString(this.station);
		dest.writeString(this.region);
		//dest.writeString(this.city);
		dest.writeString(this.distance);
	}

	public static final Parcelable.Creator<GasStation> CREATOR = new Parcelable.Creator<GasStation>() {
		public GasStation createFromParcel(Parcel in) {
			return new GasStation(in);
		}

		public GasStation[] newArray(int size) {
			return new GasStation[size];
		}
	};
}
