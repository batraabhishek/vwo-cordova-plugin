
var exec = require('cordova/exec');

var PLUGIN_NAME = 'VWOCordovaPlugin';

// Default config object
const vwoConfig = {
  optOut: false,
  customVariables: {},
  disablePreview: false
};

var VWO = function () {};

VWO.logLevelDebug   = 1
VWO.logLevelInfo    = 2
VWO.logLevelWarning = 3
VWO.logLevelError   = 4
VWO.logLevelOff     = 5

/** Launch VWO Synchronously
 *
 * @param success {callback} Success Callback
 * @param error {callback} Failure Callback
 * @param level {number} Log level
 */
VWO.setLogLevel = function(level){
  if (typeof level !== 'number') {
    throw new Error('Invalid log level');
  }
  exec(function(data) {}, function(error) {}, PLUGIN_NAME, 'setLogLevel', [level]);
};

function validatedConfig(config) {
  if (config === null  || config === undefined) {
    return vwoConfig;
  }
  var finalConfig = {};
  for (prop in vwoConfig) {
      if (typeof(config[prop]) !== "undefined" && typeof(config[prop]) === typeof(vwoConfig[prop])) {
        finalConfig[prop] = config[prop];
      } else {
        finalConfig[prop] = vwoConfig[prop];
      }
  }
  return finalConfig;
}

/** Launch VWO synchronously
 *
 * @param apiKey {string} The App Key generated in VWO's console
 * @param timeout {number} Connection Timeout of API
 * @param config {Object} Launch configuration
 */
VWO.launchSynchronously = function(apiKey, timeout, config){
  if (typeof timeout !== 'number') {
    throw new Error('timeout must be a number');
  }
  if(!apiKey && typeof level !== 'string') {
    throw new Error('Invalid API Key');
  }
  exec(function(data) {},
    function(error) {},
    PLUGIN_NAME,
    'launchSynchronously',
    [apiKey, timeout, validatedConfig(config)]
  );
};


/** Launch VWO asynchronously with Callback
 *
 * @param apiKey {string} The App Key generated in VWO's console
 * @param config {Object} Launch configuration
 * @param success {callback} Success Callback
 * @param failureCallback {callback} Failure Callback
 */
VWO.launch = function(apiKey, config, success, error){
  if(!apiKey) {
    throw new Error('Must pass in a API Key');
  }

  exec(success, error, PLUGIN_NAME, 'launch', [apiKey, validatedConfig(config)]);
};

/** Get the integer for a key
 *
 * @param success {callback} Success Callback
 * @param key {string} Key for the campaign
 * @param defaultValue {value} Default Value
 * @return Variation object
 */
VWO.intForKey = function (key, defaultValue, success) {
  if (!key) {
    throw new Error('Unique key must be passed');
  }
  if (typeof defaultValue !== 'number') {
    throw new Error('defaultValue must be a number');
  }
  exec( function(data) {
    success(data[key]);
  }, function(error) {}, PLUGIN_NAME, 'intForKey', [key, defaultValue]);
};

/** Get the Float for a key
 *
 * @param success {callback} Success Callback
 * @param key {string} Key for the campaign
 * @param defaultValue {value} Default Value
 * @return Variation object
 */
VWO.floatForKey = function (key, defaultValue, success) {
  if (!key) {
    throw new Error('Unique key must be passed');
  }
  if (typeof defaultValue !== 'number') {
    throw new Error('defaultValue must be a number');
  }
  exec( function(data) {
    success(data[key]);
  }, function(error) {}, PLUGIN_NAME, 'floatForKey', [key, defaultValue]);
};

/** Get the Boolean for a key
 *
 * @param success {callback} Success Callback
 * @param key {string} Key for the campaign
 * @param defaultValue {value} Default Value
 * @return Variation object
 */
VWO.boolForKey = function (key, defaultValue, success) {
  if (!key) {
    throw new Error('Unique key must be passed');
  }
  if (typeof defaultValue !== 'boolean') {
    throw new Error('defaultValue must be a boolean');
  }
  exec( function(data) {
    success(data[key]);
  }, function(error) {}, PLUGIN_NAME, 'boolForKey', [key, defaultValue]);
};

/** Get the String for a key
 *
 * @param success {callback} Success Callback
 * @param key {string} Key for the campaign
 * @param defaultValue {value} Default Value
 * @return Variation object
 */
VWO.stringForKey = function (key, defaultValue, success) {
  if (!key) {
    throw new Error('Unique key must be passed');
  }
  if (defaultValue === undefined) {
    throw new Error('Must pass defaultValue');
  }
  exec( function(data) {
    success(data[key]);
  }, function(error) {}, PLUGIN_NAME, 'stringForKey', [key, defaultValue]);
};

/** Get the Variation object for a key
 *
 * @param success {callback} Success Callback
 * @param key {string} Key for the campaign
 * @param defaultValue {value} Default Value
 * @return Variation object
 */
VWO.objectForKey = function (key, defaultValue, success) {
  if (!key) {
    throw new Error('Unique key must be passed');
  }
  //DefaultValue is resolved here. Native SDK would always pass nil as default value.
  exec( function(data) {
    if (data[key] === null || data[key] === undefined) {
      success(defaultValue);
    } else {
      success(data[key]);
    }
  }, function(error) {}, PLUGIN_NAME, 'objectForKey', [key]);//Default value is not passed here.
};


VWO.variationNameForTestKey = function(testKey, success) {
  if(!testKey) {
      throw new Error('Must pass valid testKey');
  }
  exec(success, function(error) {}, PLUGIN_NAME, 'variationNameForTestKey', [testKey]);
}

/** Mark the conversion for goal
 *
 * @param success {callback} Success Callback
 * @param error {callback} Failure Callback
 * @param goal {string} Goal's name
 */
VWO.trackConversion = function (goal) {
  if (!goal) {
    throw new Error('Must pass Goal name');
  }
  exec(function(data) {}, function(error) {}, PLUGIN_NAME, 'trackConversion', [goal]);
};

/** Mark the conversion for goal with value
 *
 * @param success {callback} Success Callback
 * @param error {callback} Failure Callback
 * @param goal {string} Goal's name
 * @param value {double} Goal's value
 */
VWO.trackConversionWithValue = function (goal, value) {
  if (typeof value !== 'number') {
    throw new Error('Value must be a number');
  }
  if (!goal) {
    throw new Error('Must pass Goal name');
  }
  exec(function(data) {}, function(error) {}, PLUGIN_NAME, 'trackConversionWithValue', [goal, value]);
};

/** Set custom variable by passing key and value
 *
 * @param success {callback} Success Callback
 * @param error {callback} Failure Callback
 * @param key {string} Key
 * @param value {string} Value
 */
VWO.setCustomVariable = function(key, value){
  if(!key || !value) {
    throw new Error('Key or Value is null');
  }
  exec(function(data) {}, function(error) {}, PLUGIN_NAME, 'setCustomVariable', [key, value]);
};

/** Get the version of the SDK
 *
 * @param success {callback} Success Callback
 * @param error {callback} Failure Callback
 */
VWO.version = function(success) {
  exec(success, function(error) {}, PLUGIN_NAME, 'version', []);
};

module.exports = VWO;
