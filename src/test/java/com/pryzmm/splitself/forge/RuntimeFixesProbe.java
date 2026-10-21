package com.pryzmm.splitself.forge;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
public final class RuntimeFixesProbe {
    static int count;
    static void check(boolean value){if(!value)throw new AssertionError("Check "+count);count++;}
    public static void main(String[] args){
        check(!FaceRules.skyExpired(59999));check(FaceRules.skyExpired(60000));check(FaceRules.skyExpired(90000));
        Vector3f direction=new Vector3f(10,50,-100).normalize();
        check(FaceRules.looking(direction.x,direction.y,direction.z));check(!FaceRules.looking(-direction.x,-direction.y,-direction.z));
        Matrix4f projection=new Matrix4f().perspective((float)Math.toRadians(70),16f/9f,.05f,1000f);
        Vector4f still=new Vector4f(10,50,-100,1).mul(projection);
        Vector4f turned=new Vector4f(10,50,-100,1).mul(new Matrix4f().rotateY((float)Math.toRadians(30))).mul(projection);
        check(Math.abs(still.x/still.w-turned.x/turned.w)>.1f);
        check(SimpleEventRules.WHITE_MS==300);
        String target="https://example.invalid/?x='& y=юникод";
        var windows=DesktopLinks.command("Windows 11",target);check(!String.join(" ",windows).contains(target));check(windows.get(0).equals("powershell.exe"));
        check(DesktopLinks.command("Linux",target).equals(List.of("xdg-open",target)));
        check(DesktopLinks.command("Mac OS X",target).equals(List.of("open",target)));
        var sky=new FaceRules.Sky();sky.toggle();check(sky.visible);sky.frame(true,2);check(!sky.visible);
        sky.toggle();boolean captured=false;for(int i=0;i<1500;i++)captured|=sky.frame(true,3);check(captured&&!sky.visible);
        System.out.println("RuntimeFixesProbe PASS: "+count+" assertions; no desktop/network/game execution");
    }
}
