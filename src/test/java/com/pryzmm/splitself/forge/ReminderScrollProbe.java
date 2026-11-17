package com.pryzmm.splitself.forge;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import com.pryzmm.minemessage.ui.MessageListPanel;
import com.pryzmm.minemessage.ui.MessageObject;
public final class ReminderScrollProbe {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void scrolling() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var conversation = new MessageListPanel();
            conversation.setLayout(new BoxLayout(conversation, BoxLayout.PAGE_AXIS));
            for (int i = 0; i < 30; i++) conversation.add(new MessageObject("Fixture conversation message " + i, false,
                    java.time.LocalDate.of(2026, 1, 1)));
            var pane = new JScrollPane(conversation);
            pane.setSize(new Dimension(320, 200));
            pane.doLayout();
            pane.getViewport().doLayout();
            conversation.doLayout();
            var bar = pane.getVerticalScrollBar();
            check(bar.getMaximum() > bar.getVisibleAmount(), "Conversation must overflow the viewport");
            check(!conversation.getScrollableTracksViewportHeight(), "Conversation must not shrink to viewport height");
            int before = bar.getValue();
            pane.dispatchEvent(new MouseWheelEvent(pane, MouseEvent.MOUSE_WHEEL, 0, 0, 100, 100,
                    0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, 1));
            check(bar.getValue() > before, "Mouse wheel must scroll the reminder conversation");
        });
    }

    public static void main(String[] args) throws Exception {
        scrolling();
        System.out.println("ReminderScrollProbe PASS: " + checks + " assertions; real Swing overflow and wheel scrolling; no window");
    }
}
