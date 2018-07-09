import com.vwo.mobile.VWO;
import com.vwo.mobile.VWOConfig;
import com.vwo.mobile.events.VWOStatusListener;
import com.vwo.mobile.listeners.ActivityLifecycleListener;
import com.vwo.mobile.utils.VWOLog;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VWOCordovaPlugin extends CordovaPlugin {

    private ActivityLifecycleListener listener;

    private static final int LOG_LEVEL_DEBUG = 1;
    private static final int LOG_LEVEL_INFO = 2;
    private static final int LOG_LEVEL_WARNING = 3;
    private static final int LOG_LEVEL_ERROR = 4;
    private static final int LOG_LEVEL_OFF = 5;

    private static final String OPT_OUT = "optOut";
    private static final String DISABLE_PREVIEW = "disablePreview";
    private static final String CUSTOM_VARIABLES = "customVariables";

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        listener = new ActivityLifecycleListener();
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("launchSynchronously")) {
            String apiKey = args.getString(0);
            Double timeoutInSeconds = args.getDouble(1) * 1000;
            // Convert timeout to milliseconds
            Long timeout = timeoutInSeconds.longValue();
            VWOConfig vwoConfig = parseJSONToConfig(args.getJSONObject(2));
            launchSynchronously(apiKey, timeout, vwoConfig, callbackContext);
            return true;

        } else if (action.equals("launch")) {
            String apiKey = args.getString(0);
            VWOConfig vwoConfig = parseJSONToConfig(args.getJSONObject(1));
            launch(apiKey, vwoConfig, callbackContext);
            return true;

        } else if (action.equals("setCustomVariable")) {
             String key = args.getString(0);
             String value = args.getString(1);
             setCustomVariable(key, value, callbackContext);
             return true;
 
        } else if (action.equals("version")) {
            getVersion(callbackContext);
            return true;

        } else if (action.equals("objectForKey")) {
            String key = args.getString(0);
            getVariationForKey(key, callbackContext);
            return true;

        } else if (action.equals("intForKey")) {
            String key = args.getString(0);
            int defaultValue = args.getInt(1);
            getIntegerForKey(key, defaultValue, callbackContext);
            return true;

        } else if (action.equals("stringForKey")) {
            String key = args.getString(0);
            String defaultValue = args.getString(1);
            getStringForKey(key, defaultValue, callbackContext);
            return true;

        } else if (action.equals("boolForKey")) {
            String key = args.getString(0);
            boolean defaultValue = args.getBoolean(1);
            getBooleanForKey(key, defaultValue, callbackContext);
            return true;

        } else if (action.equals("floatForKey")) {
            String key = args.getString(0);
            double defaultValue = args.getDouble(1);
            getDoubleForKey(key, defaultValue, callbackContext);
            return true;

        } else if (action.equals("trackConversion")) {
            String goalIdentifier = args.getString(0);
            trackConversion(goalIdentifier, callbackContext);
            return true;

        } else if (action.equals("trackConversionWithValue")) {
            String goalIdentifier = args.getString(0);
            double value = Double.parseDouble(args.getString(1));
            trackConversionWithValue(goalIdentifier, value, callbackContext);
            return true;

        } else if (action.equals("setLogLevel")) {
            int logLevel = args.getInt(0);
            setLogLevel(logLevel, callbackContext);
            return true;

        } else if (action.equals("variationNameForTestKey")) {
            String testKey = args.getString(0);
            variationNameForTestKey(testKey, callbackContext);
            return true;

        }
        return false;
    }

    private void setLogLevel(final int level, final CallbackContext callbackContext) {
        final int logLevel;
        switch (level) {
            case LOG_LEVEL_DEBUG:
                logLevel = VWOLog.CONFIG;
                break;
            case LOG_LEVEL_INFO:
                logLevel = VWOLog.INFO;
                break;
            case LOG_LEVEL_WARNING:
                logLevel = VWOLog.WARNING;
                break;
            case LOG_LEVEL_ERROR:
                logLevel = VWOLog.SEVERE;
                break;
            case LOG_LEVEL_OFF:
                logLevel = VWOLog.OFF;
                break;
            default:
                VWOLog.e(VWOLog.CONFIG_LOGS, "Invalid log level : " + level, false, false);
                logLevel = -1;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (logLevel == -1) {
                    callbackContext.error("Failed to change log level");
                } else {
                    VWOLog.setLogLevel(logLevel);
                    callbackContext.success("Log level changed successfully");
                }
            }
        });
    }

    private void variationNameForTestKey(final String testKey, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    callbackContext.success(VWO.getVariationNameForTestKey(testKey));
                } catch (Exception exception) {
                    VWOLog.e(VWOLog.DATA_LOGS, exception, true, true);
                }
            }
        });
    }

    private void trackConversion(final String goalIdentifier, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                VWO.trackConversion(goalIdentifier);
            }
        });
    }

    private void trackConversionWithValue(final String goalIdentifier, final double value, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                VWO.trackConversion(goalIdentifier, value);
            }
        });
    }

    private VWOConfig parseJSONToConfig(JSONObject jsonObject) throws JSONException {
        VWOConfig.Builder vwoConfigBuilder = new VWOConfig.Builder();
        vwoConfigBuilder.setLifecycleListener(listener);
        if (jsonObject.has(DISABLE_PREVIEW) && jsonObject.getBoolean(DISABLE_PREVIEW)) {
            vwoConfigBuilder.disablePreview();
        }
        if (jsonObject.has(OPT_OUT)) {
            vwoConfigBuilder.setOptOut(jsonObject.getBoolean(OPT_OUT));
        }
        if (jsonObject.has(CUSTOM_VARIABLES)) {
            vwoConfigBuilder.setCustomVariables(JSONtoMap(jsonObject.getJSONObject(CUSTOM_VARIABLES)));
        }

        return vwoConfigBuilder.build();
    }


    private static Map<String, String> JSONtoMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            if (object.isNull(key)) {
                map.put(key, null);
            } else {
                map.put(key, object.getString(key));
            }
        }
        return map;
    }

    private void getIntegerForKey(final String key, final int defaultValue, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                JSONObject wrapperObject = new JSONObject();
                int object = VWO.getIntegerForKey(key, defaultValue);
                try {
                    wrapperObject.put(key, object);
                    callbackContext.success(wrapperObject);
                } catch (JSONException exception) {
                    VWOLog.e(VWOLog.DATA_LOGS, exception, false, false);
                    callbackContext.error("Unable to create json: " + exception.getMessage());
                }
            }
        });
    }

    private void getStringForKey(final String key, final String defaultValue, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                JSONObject wrapperObject = new JSONObject();
                String object = VWO.getStringForKey(key, defaultValue);
                try {
                    if (object == null) {
                        wrapperObject.put(key, JSONObject.NULL);
                    } else {
                        wrapperObject.put(key, object);
                    }
                    callbackContext.success(wrapperObject);
                } catch (JSONException exception) {
                    VWOLog.e(VWOLog.DATA_LOGS, exception, false, false);
                    callbackContext.error("Unable to create json: " + exception.getMessage());
                }
            }
        });
    }

    private void getBooleanForKey(final String key, final boolean defaultValue, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                JSONObject wrapperObject = new JSONObject();
                boolean object = VWO.getBooleanForKey(key, defaultValue);
                try {
                    wrapperObject.put(key, object);
                    callbackContext.success(wrapperObject);
                } catch (JSONException exception) {
                    VWOLog.e(VWOLog.DATA_LOGS, exception, false, false);
                    callbackContext.error("Unable to create json: " + exception.getMessage());
                }
            }
        });
    }

    private void getDoubleForKey(final String key, final double defaultValue, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                JSONObject wrapperObject = new JSONObject();
                double object = VWO.getDoubleForKey(key, defaultValue);
                try {
                    wrapperObject.put(key, object);
                    callbackContext.success(wrapperObject);
                } catch (JSONException exception) {
                    VWOLog.e(VWOLog.DATA_LOGS, exception, false, false);
                    callbackContext.error("Unable to create json: " + exception.getMessage());
                }
            }
        });
    }

    private void setCustomVariable(final String key, final String value, final CallbackContext callbackContext) {
         cordova.getActivity().runOnUiThread(new Runnable() {
             public void run() {
                 VWO.setCustomVariable(key, value);
             }
         });
     }

    private void getVariationForKey(final String key, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                JSONObject wrapperObject = new JSONObject();
                Object object = VWO.getObjectForKey(key, null);

                if (object == null) {
                    try {
                        wrapperObject.put(key, JSONObject.NULL);
                        callbackContext.success(wrapperObject);
                    } catch (JSONException exception) {
                        VWOLog.e(VWOLog.DATA_LOGS, exception, false, false);
                        callbackContext.error("Unable to create json: " + exception.getMessage());
                    }
                } else {
                    try {
                        wrapperObject.put(key, object);
                        callbackContext.success(wrapperObject);
                    } catch (JSONException exception) {
                        VWOLog.e(VWOLog.DATA_LOGS, exception, false, false);
                        callbackContext.error("Unable to create json: " + exception.getMessage());
                    }
                }
            }
        });
    }

    private void launchSynchronously(final String apiKey, final Long timeout, final VWOConfig config, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                VWO.with(cordova.getActivity(), apiKey).config(config).launchSynchronously(timeout);
                callbackContext.success("VWO Initialized");
            }
        });
    }

    private void getVersion(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                callbackContext.success(VWO.version());
            }
        });
    }

    private void launch(final String apiKey, final VWOConfig config, final CallbackContext callbackContext) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                VWO.with(cordova.getActivity(), apiKey).config(config).launch(new VWOStatusListener() {
                    @Override
                    public void onVWOLoaded() {
                        callbackContext.success("VWO Loaded");
                    }

                    @Override
                    public void onVWOLoadFailure(String reason) {
                        callbackContext.error("VWO Load Failure");
                    }
                });
            }
        });
    }

    @Override
    public void onPause(boolean multitasking) {
        listener.onPause();
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        listener.onResume();
    }

    @Override
    public void onStop() {
        listener.onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        listener.onStart();
    }
}
