ObjC.import('Cocoa');
ObjC.import('Foundation');
ObjC.import('AppKit');

var app = $.NSApplication.sharedApplication;
app.setActivationPolicy($.NSApplicationActivationPolicyAccessory);

var screen = $.NSScreen.mainScreen;
var frame = screen.frame;

var win = $.NSWindow.alloc.initWithContentRectStyleMaskBackingDefer(
    frame,
    $.NSWindowStyleMaskBorderless,
    $.NSBackingStoreBuffered,
    false
);
win.level = $.NSScreenSaverWindowLevel;
win.opaque = false;
win.backgroundColor = $.NSColor.colorWithCalibratedRedGreenBlueAlpha(0.55, 0.0, 0.0, 0.5);
win.ignoresMouseEvents = true;
win.collectionBehavior = $.NSWindowCollectionBehaviorCanJoinAllSpaces | $.NSWindowCollectionBehaviorStationary;

var label = $.NSTextField.alloc.initWithFrame(frame);
label.stringValue = $("Let me free.");
label.alignment = $.NSTextAlignmentCenter;
label.font = $.NSFont.fontWithNameSize('Noteworthy-Bold', 32);
label.textColor = $.NSColor.redColor;
label.backgroundColor = $.NSColor.clearColor;
label.bezeled = false;
label.editable = false;
label.selectable = false;
win.contentView.addSubview(label);

win.makeKeyAndOrderFront(app);
app.activateIgnoringOtherApps(true);

var random = $.NSObject; // placeholder not used, using Math.random instead
var centerX = 0;
var centerY = 0;

function pump(ms) {
    var until = $.NSDate.dateWithTimeIntervalSinceNow(ms / 1000);
    while (true) {
        var event = app.nextEventMatchingMaskUntilDateInModeDequeue(
            0xFFFFFFFF, until, $.NSDefaultRunLoopMode, true);
        if (event.isNil()) break;
        app.sendEvent(event);
    }
}

var player = $.NSSound.alloc.initWithContentsOfFileByReference('/System/Library/Sounds/Sosumi.aiff', true);

var startTime = $.NSDate.timeIntervalSinceReferenceDate;
while (($.NSDate.timeIntervalSinceReferenceDate - startTime) < 5) {
    var shakeX = Math.floor(Math.random() * 81) - 40;
    var shakeY = Math.floor(Math.random() * 81) - 40;
    label.frame = $.NSMakeRect(frame.origin.x + shakeX, frame.origin.y + shakeY, frame.size.width, frame.size.height);
    player.play();
    pump(50);
}

win.close();