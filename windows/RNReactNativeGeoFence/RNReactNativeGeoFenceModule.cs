using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Com.Reactlibrary.RNReactNativeGeoFence
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNReactNativeGeoFenceModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNReactNativeGeoFenceModule"/>.
        /// </summary>
        internal RNReactNativeGeoFenceModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNReactNativeGeoFence";
            }
        }
    }
}
