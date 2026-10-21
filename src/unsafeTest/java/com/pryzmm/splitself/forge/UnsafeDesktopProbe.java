package com.pryzmm.splitself.forge;
import java.util.List;
public final class UnsafeDesktopProbe {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        check(UnsafeDriveRules.drives("windows","Drive\r\nD:\r\nE:\r\nnoise\r\n").equals(List.of("D:","E:")),"only valid CD drive identifiers");
        check(UnsafeDriveRules.drives("mac os x","Vendor info\n1 Optical\n2 Second").equals(List.of("1","2")),"mac drive parser");
        check(UnsafeDriveRules.drives("linux","sr0 rom\nsr1 rom").equals(List.of("/dev/sr0","/dev/sr1")),"linux ROM parser");
        var command=UnsafeDriveRules.eject("windows","D:");
        check(command.contains("Hidden") && command.get(command.size()-1).contains("ParseName('D:').InvokeVerb('Eject')"),"hidden Windows Eject command constructed only");
        boolean rejected=false;try{UnsafeDriveRules.eject("windows","D:'; whoami");}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"drive cannot inject PowerShell");
        check(UnsafeDriveRules.eject("linux","/dev/sr0").equals(List.of("eject","/dev/sr0")),"Linux args separate");
        check(UnsafeDriveRules.eject("mac os x","1").equals(List.of("drutil","-drive","1","tray","eject")),"Mac args separate");

        var process=CommandEventScript.process("test user");
        check(process.command().contains("-EncodedCommand"),"Fixed command script still packaged");
        check(process.environment().get("SPLITSELF_COMMAND_USER").equals("test user"),"User is data");
        check(EditionDesktopIntegrations.typingAvailable(),"UNSAFE implementation selected");
        System.out.println("UnsafeDesktopProbe PASS: drive rules and command construction; no process started");
    }
}
