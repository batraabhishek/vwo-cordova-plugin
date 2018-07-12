#import "VWOCordovaPlugin.h"
#import "VWO.h"

#import <Cordova/CDVAvailability.h>

@implementation VWOCordovaPlugin

- (void)setLogLevel:(CDVInvokedUrlCommand *)command {
    int logLevel = [[command argumentAtIndex:0] intValue];
    switch (logLevel) {//Refer plugin.js for log level values
        case 1: [VWO setLogLevel:VWOLogLevelDebug]; break;
        case 2: [VWO setLogLevel:VWOLogLevelInfo]; break;
        case 3: [VWO setLogLevel:VWOLogLevelWarning]; break;
        case 4: [VWO setLogLevel:VWOLogLevelError]; break;
        case 5: [VWO setLogLevel:VWOLogLevelNone]; break;
        default: break;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)setCustomVariable:(CDVInvokedUrlCommand *)command {
    NSString *key = [command argumentAtIndex:0];
    NSString *value = [command argumentAtIndex:1];
    [VWO setCustomVariable:key withValue:value];
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

//Private
- (VWOConfig *)vwoConfigFromDictionary:(NSDictionary *)configDict {
    VWOConfig *config = [VWOConfig new];
    config.disablePreview = [configDict[@"disablePreview"] boolValue];
    config.optOut = [configDict[@"optOut"] boolValue];
    config.customVariables = configDict[@"customVariables"];
    return config;
}

- (void)launchSynchronously:(CDVInvokedUrlCommand *)command {
    NSString* apiKey = [command argumentAtIndex:0];
    double timeout = [[command argumentAtIndex:1] doubleValue];
    NSDictionary *configDict = [command argumentAtIndex:2];
    VWOConfig *vwoConfig = [self vwoConfigFromDictionary:configDict];
    [VWO launchSynchronouslyForAPIKey:apiKey timeout:timeout config:vwoConfig];

    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)launch:(CDVInvokedUrlCommand *)command {
    NSString* apiKey = [command argumentAtIndex:0];
    NSDictionary *configDict = [command argumentAtIndex:1];
    VWOConfig *vwoConfig = [self vwoConfigFromDictionary:configDict];

    [VWO launchForAPIKey:apiKey config:vwoConfig completion:^{
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];

    } failure:^(NSString * _Nonnull error) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Error"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}


- (void)intForKey:(CDVInvokedUrlCommand *)command {
    NSString *key = [command argumentAtIndex:0];
    int defaultValue = [[command argumentAtIndex:1] intValue];

    int val = [VWO intForKey:key defaultValue:defaultValue];

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsDictionary:@{key : @(val)}];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)floatForKey:(CDVInvokedUrlCommand *)command {
    NSString *key = [command argumentAtIndex:0];
    double defaultValue = [[command argumentAtIndex:1] doubleValue];

    double val = [VWO doubleForKey:key defaultValue:defaultValue];

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsDictionary:@{key : @(val)}];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)boolForKey:(CDVInvokedUrlCommand *)command {
    NSString *key = [command argumentAtIndex:0];
    BOOL defaultValue = [[command argumentAtIndex:1] boolValue];

    BOOL val = [VWO boolForKey:key defaultValue:defaultValue];

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsDictionary:@{key : @(val)}];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)stringForKey:(CDVInvokedUrlCommand *)command {
    NSString *key = [command argumentAtIndex:0];
    NSString *defaultValue = [command argumentAtIndex:1];

    NSString *val = [VWO stringForKey:key defaultValue:defaultValue];

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsDictionary:@{key : val}];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)objectForKey:(CDVInvokedUrlCommand *)command {
    NSString *key = [command argumentAtIndex:0];

        // default value is always nil as Java does not support General object type.
        // Hence value is inserted in Dictionary and then sent
    id variation = [VWO objectForKey:key defaultValue:nil];

    CDVPluginResult* result;
    if (variation == nil) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{key : [NSNull null]}];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{key : variation}];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)variationNameForTestKey:(CDVInvokedUrlCommand *)command {
    NSString *testKey = [command argumentAtIndex:0];

    NSString *variationName = [VWO variationNameForTestKey:testKey];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:variationName];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)trackConversion:(CDVInvokedUrlCommand *)command {
    NSString *goal = [command argumentAtIndex:0];
    [VWO trackConversion:goal];
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)trackConversionWithValue:(CDVInvokedUrlCommand *)command {
    NSString *goal = [command argumentAtIndex:0];
    double value = [[command argumentAtIndex:1] doubleValue];
    [VWO trackConversion:goal withValue:value];
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)version:(CDVInvokedUrlCommand *)command {
    NSString *version = [VWO version];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:version];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
