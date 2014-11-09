package com.ialert.utilities;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceStation implements Parcelable {
	private String country;
	private String zip;
	private String address;
	private String lat;
	private String lon;
	private String name;
	private String region;
	private String distance;
	private String city;
	private String email;
	private String phone;
	private String url;
	private String fax;

	public ServiceStation() {
		this.country = "";
		this.zip = "";
		this.address = "";
		this.lat = "";
		this.lon = "";
		this.name = "";
		this.region = "";
		this.distance = "";
		this.city = "";
		this.email = "";
		this.phone = "";
		this.url = "";
		this.fax = "";
	}

	public ServiceStation(Parcel in) {
		this.country = in.readString();
		this.zip = in.readString();
		this.address = in.readString();
		this.lat = in.readString();
		this.lon = in.readString();
		this.name = in.readString();
		this.region = in.readString();
		this.distance = in.readString();
		this.city = in.readString();
		this.email = in.readString();
		this.phone = in.readString();
		this.url = in.readString();
		this.fax = in.readString();
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

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

	public String getName() {
		return name;
	}

	public void setStation(String name) {
		if (name == "null")
			name = "Unknown";
		this.name = name;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {		
		dest.writeString(this.country);
		dest.writeString(this.zip);
		dest.writeString(this.address);
		dest.writeString(this.lat);
		dest.writeString(this.lon);
		dest.writeString(this.name);
		dest.writeString(this.region);
		dest.writeString(this.distance);
		dest.writeString(this.city);
		dest.writeString(this.email);
		dest.writeString(this.phone);
		dest.writeString(this.url);
		dest.writeString(this.fax);
	}

	public static final Parcelable.Creator<ServiceStation> CREATOR = new Parcelable.Creator<ServiceStation>() {
		public ServiceStation createFromParcel(Parcel in) {
			return new ServiceStation(in);
		}

		public ServiceStation[] newArray(int size) {
			return new ServiceStation[size];
		}
	};
}
