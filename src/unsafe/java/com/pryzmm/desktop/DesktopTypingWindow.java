package com.pryzmm.desktop;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.*;
/** Standalone typing window; compiled and packaged only in UNSAFE. */
public final class DesktopTypingWindow {
    public static void main(String[] args) {
        String body=new String(Base64.getDecoder().decode(args[1]),StandardCharsets.UTF_8);
        SwingUtilities.invokeLater(()->typing(body));
    }
    private static void typing(String body){
        JFrame window=new JFrame("Let me free.");window.setSize(360,240);window.setLocationRelativeTo(null);window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JTextArea area=new JTextArea();area.setEditable(false);area.setLineWrap(true);area.setWrapStyleWord(true);window.add(new JScrollPane(area));window.setVisible(true);
        int[] at={0};Timer timer=new Timer(100,null);timer.setInitialDelay(500);timer.addActionListener(e->{if(at[0]>=body.length()){area.setText(body);timer.stop();return;}area.setText(body.substring(0,++at[0])+"|");timer.setDelay((int)(Math.random()*250));});timer.start();
    }
}
