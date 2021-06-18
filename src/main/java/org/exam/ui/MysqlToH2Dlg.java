package org.exam.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.exam.service.MysqlToH2Service;
import org.exam.util.BaseUtils;
import org.exam.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MysqlToH2Dlg extends JDialog {
    private JPanel mainPanel;
    private JTextArea mysqlTxtArea;
    private JTextPane h2TxtPnl;
    private JButton openFileBtn;
    private JButton convertBtn;
    private JButton saveBtn;
    private JScrollPane mysqlScrollPane;
    private JScrollPane h2ScrollPane;
    private JSplitPane splitPane;

    public MysqlToH2Dlg(Project project) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setPreferredSize(new Dimension((int) (0.8 * (screenSize.getWidth() - 80)),
                (int) (screenSize.getHeight() - 80)));
        setTitle("convert mysql to h2");
        setContentPane(mainPanel);
        setAlwaysOnTop(true);

        JDialog myDlg = this;
        mainPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(0.5);
            }
        });

        //打开文件
        openFileBtn.addActionListener(e -> {
            try {
                myDlg.setAlwaysOnTop(false);
                loadFile(project);
            } finally {
                myDlg.setAlwaysOnTop(true);
            }
            convert();
        });

        //转换
        convertBtn.addActionListener(e -> convert());

        //保存
        saveBtn.addActionListener(e -> {
            try {
                myDlg.setAlwaysOnTop(false);
                saveFile(project);
            } finally {
                myDlg.setAlwaysOnTop(true);
            }
        });
    }

    private void saveFile(Project project) {
        final String h2Txt = h2TxtPnl.getText();
        if (!BaseUtils.isEmpty(h2Txt)) {
            VirtualFileWrapper targetFile = FileChooserFactory.getInstance().createSaveFileDialog(
                    new FileSaverDescriptor("Save to", "", "sql"), project)
                    .save(ProjectUtil.guessProjectDir(project), "h2.sql");
            if (targetFile != null) {
                FileUtils.copyToFile(h2Txt.getBytes(StandardCharsets.UTF_8), targetFile.getFile());
            }
        }
    }

    private void convert() {
        try {
            final String convertTxt = MysqlToH2Service.getInstance().convert(mysqlTxtArea.getText());
            h2TxtPnl.setText(convertTxt);
        } catch (Exception e) {
            Messages.showWarningDialog("error:" + e.getMessage(), "Warn");
        }
    }

    private void loadFile(Project project) {
        VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor("sql"),
                project, null);
        if (virtualFile == null) return;
        long length = virtualFile.getLength();
        if (length > 1048576) {
            Messages.showWarningDialog("File size:" + length + "B. Directly convert when the file larger then 1M.", "Warn");
            VirtualFileWrapper targetFile = FileChooserFactory.getInstance().createSaveFileDialog(
                    new FileSaverDescriptor("Save to", "", "sql"), project)
                    .save(ProjectUtil.guessProjectDir(project), "h2.sql");
            if (targetFile != null) {
                try {
                    MysqlToH2Service.getInstance().convert(virtualFile.getPath(), targetFile.getFile().getCanonicalPath());
                } catch (IOException e) {
                    Messages.showWarningDialog("error:" + e.getMessage(), "Warn");
                }
            }
        } else {
            final String source = FileUtils.copyToString(new File(virtualFile.getPath()), StandardCharsets.UTF_8);
            mysqlTxtArea.setText(source);
        }
    }


    public void open() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
