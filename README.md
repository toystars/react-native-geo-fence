
# react-native-geo-fence

[![npm](https://img.shields.io/npm/v/react-native-geo-fence.svg)](https://www.npmjs.com/package/react-native-geo-fence) [![Downloads](https://img.shields.io/npm/dt/react-native-geo-fence.svg)](https://www.npmjs.com/package/react-native-geo-fence) [![Licence](https://img.shields.io/npm/l/react-native-geo-fence.svg)](https://www.npmjs.com/package/react-native-geo-fence)

> Simple React-Native geofencing package

## Installation

``` bash
$ npm install react-native-geo-fence --save
```
or use yarn

``` bash
$ yarn add react-native-geo-fence
```

### Mostly automatic installation

`$ react-native link react-native-geo-fence`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.bmustapha.reactlibrary.RNGeoFencePackage;;` to the imports at the top of the file
  - Add `new RNGeoFencePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-geo-fence'
  	project(':react-native-geo-fence').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-geo-fence/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-geo-fence')
  	```

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-geo-fence` and add `RNReactNativeGeoFence.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReactNativeGeoFence.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<


## Usage
```javascript
import React, { Component } from 'react';
import {
  NativeAppEventEmitter,
} from 'react-native';
import RNGeofence from 'react-native-geo-fence';
import _ from 'lodash';


export default class Home extends Component {

  componentDidMount() {
    // trigger geofencing
    this.initiateGeofencing();
    // listen to native "GeofenceEvent" event triggered by "react-native-geo-fence"
    this.listeners = [
      NativeAppEventEmitter.addListener('GeofenceEvent', this.handleNativeEvents),
    ];
  }

  componentWillUnmount() {
    // stop geofencing
    RNGeofence.stopGeofencing();
    // remove listener
    _.each(this.listeners, l => l.remove());
  }

  handleNativeEvents = (event) => {
    /*
    * event contains an object as below
    *
    * {
    *   event: 'geofenceTrigger',
    *   data: [{
    *     transition: 'Entered', // transition is either "Entered" or "Exited" explains if user entered or exited geofence
    *     key: 'qyuwhbhh783',
    *     latitude: 6.4334191,
    *     longitude: 3.4345843,
    *   }]
    * }
    *
    * data contains an array of geofences triggered.
    * It is always an array, even if only one geofence was triggered
    *
    * */

    console.log('Native Event: ', event);

    // do something else with event object and geofences
  };

  initiateGeofencing = () => {
    // create an array of geofences you want to get notified for as below
    const geofencesArray = [{
      key: 'qyuwhbhh783', // must be unique, used internally to return unique geofence
      latitude: 6.4334191,
      longitude: 3.4345843,
    }, {
      key: '6273hbbvdhbf',
      latitude: 34.8372645,
      longitude: 19.763423,
    }, {
      key: 'hjd09283745',
      latitude: 12.2519453,
      longitude: 9.8125365,
    }];

    // create radius and expiry time
    const geofenceRadiusInMetres = 500; // geofence radius
    const geofenceExpirationInMilliseconds = 86400000; // geofence expiration time

    // add geofences array to module
    RNGeofence.populateGeofenceList(
      geofencesArray,
      geofenceRadiusInMetres,
      geofenceExpirationInMilliseconds,
    );

    // start tracking geofences
    RNGeofence.beginGeofencing();
  };

  render() {
    return null;
  }
}
```

### Managing Location permission in Android 6.0 and above
`react-native-geo-fence` uses `ACCESS_FINE_LOCATION` permission to track user's location changes. 
Prior to Android 6.0, all locations specified in app manifest will be granted at install time. For Android 6.0 and above, permissions need to be requested at run time. To achieve this, react-native-geo-fence requests for said permission when `RNGeofence.beginGeofencing()` is triggered. Due to this, some other setups are required to be completed to enable `react-native-geo-fence` know when user has granted said permission.

Add this to `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

Add the snippet below to your project `MainActivity.java`
```java
// import the following lines at the top
import static com.bmustapha.reactlibrary.RNGeoFenceModule.RNGeoFenceModuleContext;
import static com.bmustapha.reactlibrary.RNGeoFenceModule.RNGeoFenceModule_REQ_PERMISSION;

// override MainActivity onRequestPermissionsResult
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
        case RNGeoFenceModule_REQ_PERMISSION: {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission granted
                RNGeoFenceModuleContext.resume();
            }
            break;
        }
    }
}
```

## TODO
- iOS implementation


## Contributing

Contributions are **welcome** and will be fully **credited**.

Contributions are accepted via Pull Requests on [Github](https://github.com/toystars/react-native-geo-fence).


### Pull Requests

- **Document any change in behaviour** - Make sure the `README.md` and any other relevant documentation are kept up-to-date.

- **Consider our release cycle** - We try to follow [SemVer v2.0.0](http://semver.org/). Randomly breaking public APIs is not an option.

- **Create feature branches** - Don't ask us to pull from your master branch.

- **One pull request per feature** - If you want to do more than one thing, send multiple pull requests.

- **Send coherent history** - Make sure each individual commit in your pull request is meaningful. If you had to make multiple intermediate commits while developing, please [squash them](http://www.git-scm.com/book/en/v2/Git-Tools-Rewriting-History#Changing-Multiple-Commit-Messages) before submitting.


## Issues

Check issues for current issues.

## Author

[Mustapha Babatunde](https://twitter.com/iAmToystars)
 

## License

The MIT License (MIT). Please see [LICENSE](LICENSE) for more information.
