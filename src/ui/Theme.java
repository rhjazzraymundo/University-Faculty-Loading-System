package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Central color palette for the whole system.
 * Change a color here and every screen updates.
 *
 * Palette:  #FAF1EC  #E4CEC2  #905F66  #4E0911  #260707
 */
public final class Theme {

    private Theme() {}

    // ----- Core palette (from the design picture) -----
    public static final Color CREAM    = new Color(0xFA, 0xF1, 0xEC); // #FAF1EC
    public static final Color BLUSH    = new Color(0xE4, 0xCE, 0xC2); // #E4CEC2
    public static final Color MAUVE    = new Color(0x90, 0x5F, 0x66); // #905F66
    public static final Color BURGUNDY = new Color(0x4E, 0x09, 0x11); // #4E0911
    public static final Color ESPRESSO = new Color(0x26, 0x07, 0x07); // #260707

    // ----- Roles used by the screens -----
    public static final Color BACKGROUND = CREAM;     // window background
    public static final Color BORDER     = BLUSH;     // card borders, table grid
    public static final Color HEADER     = BURGUNDY;  // top header bars
    public static final Color HEADER_SUB = BLUSH;     // subtitle text on header bars
    public static final Color PRIMARY    = BURGUNDY;  // Save / Add / Login buttons
    public static final Color ACCENT     = MAUVE;     // Update buttons
    public static final Color DANGER     = ESPRESSO;  // Delete / Exit buttons
    public static final Color TEXT       = ESPRESSO;  // main text
    public static final Color MUTED      = MAUVE;     // secondary text
    public static final Color CARD       = Color.WHITE;

    // Blend of burgundy and mauve (secondary buttons, 4th dashboard card)
    public static final Color SECONDARY  = new Color(111, 52, 60);
    // Faded mauve for disabled items
    public static final Color DISABLED   = new Color(176, 140, 146);

    // ----- Dashboard stat cards -----
    public static final Color CARD_1 = BURGUNDY;
    public static final Color CARD_2 = MAUVE;
    public static final Color CARD_3 = ESPRESSO;
    public static final Color CARD_4 = SECONDARY;

    // ----- Status colors (kept readable on purpose: red / amber / green) -----
    public static final Color STATUS_OVER = new Color(220, 38, 38);
    public static final Color STATUS_WARN = new Color(217, 119, 6);
    public static final Color STATUS_OK   = new Color(22, 101, 52);

    /** Call once after setting the Look and Feel. */
    public static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TabbedPane.background", BACKGROUND);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", CARD);
        UIManager.put("TextField.selectionBackground", BLUSH);
        UIManager.put("TextField.selectionForeground", TEXT);
        UIManager.put("PasswordField.selectionBackground", BLUSH);
        UIManager.put("PasswordField.selectionForeground", TEXT);
        UIManager.put("ComboBox.selectionBackground", BLUSH);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("Table.selectionBackground", BLUSH);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("ScrollBar.thumb", BLUSH);
    }

    /** Applies the palette to a JTable and its header. */
    public static void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(BLUSH);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, false, false, row, col);
                setBackground(HEADER);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(SwingConstants.LEFT);
                setOpaque(true);
                return this;
            }
        });
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 30));
    }
}
