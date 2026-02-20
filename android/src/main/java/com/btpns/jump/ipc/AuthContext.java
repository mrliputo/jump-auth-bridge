package com.btpns.jump.ipc;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Parcelable used across apps via AIDL. */
public class AuthContext implements Parcelable {
  public String issuerPackage;
  public String userId;
  @Nullable public String displayName;
  @Nullable public List<String> roles;
  public String accessToken;
  public long expiresAtEpochMs;

  public AuthContext() {}

  protected AuthContext(Parcel in) {
    issuerPackage = in.readString();
    userId = in.readString();
    displayName = in.readString();
    ArrayList<String> r = new ArrayList<>();
    in.readStringList(r);
    roles = r.isEmpty() ? null : r;
    accessToken = in.readString();
    expiresAtEpochMs = in.readLong();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(issuerPackage);
    dest.writeString(userId);
    dest.writeString(displayName);
    dest.writeStringList(roles);
    dest.writeString(accessToken);
    dest.writeLong(expiresAtEpochMs);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<AuthContext> CREATOR = new Creator<AuthContext>() {
    @Override
    public AuthContext createFromParcel(Parcel in) {
      return new AuthContext(in);
    }

    @Override
    public AuthContext[] newArray(int size) {
      return new AuthContext[size];
    }
  };
}

