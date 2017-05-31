package com.bmustapha.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class RNGeoFenceModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNGeoFenceModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNGeoFenceModule";
  }
}