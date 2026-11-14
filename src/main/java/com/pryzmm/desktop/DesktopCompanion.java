package com.pryzmm.desktop;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.*;

/** Separate GUI JVM: Minecraft can keep its own headless AWT configuration. */
public final class DesktopCompanion {
    public static void main(String[] args){
        String mode=args[0],body=new String(Base64.getDecoder().decode(args[1]),StandardCharsets.UTF_8);
        SwingUtilities.invokeLater(()->{
            if(!SystemTray.isSupported()){JOptionPane.showMessageDialog(null,body,"Messages",JOptionPane.INFORMATION_MESSAGE);return;}
            try{
                BufferedImage image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
                var g=image.createGraphics();g.setColor(new Color(80,130,230));g.fillRoundRect(2,2,28,25,7,7);g.setColor(Color.WHITE);g.drawString("!",14,20);g.dispose();
                TrayIcon icon=new TrayIcon(image,"Messenger");icon.setImageAutoSize(true);
                icon.addActionListener(e->{System.out.println("[SplitSelfDesktop] OPEN_MESSAGE");System.out.flush();SystemTray.getSystemTray().remove(icon);System.exit(0);});
                PopupMenu menu=new PopupMenu();MenuItem open=new MenuItem("Open message");open.addActionListener(e->{System.out.println("[SplitSelfDesktop] OPEN_MESSAGE");System.out.flush();System.exit(0);});menu.add(open);icon.setPopupMenu(menu);
                SystemTray.getSystemTray().add(icon);icon.displayMessage("Messages",body,TrayIcon.MessageType.INFO);
                Timer end=new Timer(120000,e->{SystemTray.getSystemTray().remove(icon);System.exit(0);});end.setRepeats(false);end.start();
            }catch(Exception e){e.printStackTrace();JOptionPane.showMessageDialog(null,body,"Messages",JOptionPane.INFORMATION_MESSAGE);}
        });
    }

}
