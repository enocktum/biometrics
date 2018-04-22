import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.DatatypeConverter;

import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.biometrics.swing.NFingerViewBase.ShownImage;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFingerScanner;
import com.neurotec.images.NImages;
import com.neurotec.io.NFile;
import com.neurotec.samples.util.Utils;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

public final class EnrollFromScanner extends BasePanel {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final long serialVersionUID = 1L;

    // ===========================================================
    // Private fields
    // ===========================================================

    private NSubject subject;
    private final NDeviceManager deviceManager;
    private boolean scanning;
    private boolean identifyFinger;
    private boolean appendFinger;
    private boolean enrollFinger;

    private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();

    private NFingerView view;
    private JFileChooser fcImage;
    private JFileChooser fcTemplate;
    private File oldImageFile;
    private File oldTemplateFile;

    private JButton btnCancel;
    private JButton btnForce;
    private JButton btnRefresh;
    private JButton btnIdentifyFinger;
    private JButton btnAppendFinger;
    private JButton btnEnrollFinger;
    private JButton btnScan;
    private JCheckBox cbAutomatic;
    private JCheckBox cbShowBinarized;
    private JLabel lblInfo;
    private JPanel panelButtons;
    private JPanel panelInfo;
    private JPanel panelMain;
    private JPanel panelSave;
    private JPanel panelScanners;
    private JPanel panelSouth;
    private JList scannerList;
    private JScrollPane scrollPane;
    private JScrollPane scrollPaneList;
    private CallBack callBack;

    // ===========================================================
    // Public constructor
    // ===========================================================

    public EnrollFromScanner(CallBack callBack, String action) {
        super();
        this.callBack = callBack;
        if (callBack.getAction().equals(CallBack.IDENTIFY_FINGER_ACTION)) {
            identifyFinger = true;
        } else if (callBack.getAction().equals(CallBack.ENROLL_FINGER_ACTION)) {
            enrollFinger = true;
        } else if (callBack.getAction().equals(CallBack.APPEND_FINGER_ACTION)) {
            appendFinger = true;
        }
        requiredLicenses = new ArrayList<String>();
        requiredLicenses.add("Biometrics.FingerExtraction");
        requiredLicenses.add("Devices.FingerScanners");
        optionalLicenses = new ArrayList<String>();
        optionalLicenses.add("Images.WSQ");

        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
    }

    // ===========================================================
    // Private methods
    // ===========================================================

    public void startCapturing() {
        lblInfo.setText("");
        if (FingersTools.getInstance().getClient().getFingerScanner() == null) {
            JOptionPane.showMessageDialog(this, "Please select scanner from the list.", "No scanner selected", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        // Create a finger.
        NFinger finger = new NFinger();

        // Set Manual capturing mode if automatic isn't selected.
        if (!cbAutomatic.isSelected()) {
            finger.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        }

        // Add finger to subject and finger view.
        subject = new NSubject();
        subject.getFingers().add(finger);
        view.setFinger(finger);
        view.setShownImage(ShownImage.ORIGINAL);

        // Begin capturing.
        FingersTools fingersTools = FingersTools.getInstance();
        NBiometricClient biometricClient = fingersTools.getClient();

        NBiometricTask biometricTask = biometricClient.createTask(
                EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.CREATE_TEMPLATE), subject);


//        NBiometricTask task = FingersTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.CREATE_TEMPLATE), subject);
        FingersTools.getInstance().getClient().performTask(biometricTask, null, captureCompletionHandler);
        scanning = true;
        updateControls();
    }

    private void enrollFinger(CallBack callBack) throws IOException {
        if (subject != null) {
            String fingerPrint = DatatypeConverter.printBase64Binary(subject.getTemplateBuffer().toByteArray());
            System.out.println(fingerPrint);
            callBack.enrollFinger(fingerPrint);
            if (oldTemplateFile != null) {
                fcTemplate.setSelectedFile(oldTemplateFile);
            }
        }
    }

    private void appendFinger(CallBack callBack) throws IOException {
        if (subject != null) {
            String fingerPrint = DatatypeConverter.printBase64Binary(subject.getTemplateBuffer().toByteArray());
            System.out.println(fingerPrint);
            callBack.appendFinger(fingerPrint);
            if (oldTemplateFile != null) {
                fcTemplate.setSelectedFile(oldTemplateFile);
            }
        }
    }

    private void identifyFinger(CallBack callBack) throws IOException {
        if (subject != null) {
            String fingerPrint = DatatypeConverter.printBase64Binary(subject.getTemplateBuffer().toByteArray());
            callBack.identifyFinger(fingerPrint);
            if (oldImageFile != null) {
                fcImage.setSelectedFile(oldImageFile);
            }
        }
    }

    private void updateShownImage() {
        if (cbShowBinarized.isSelected()) {
            view.setShownImage(ShownImage.RESULT);
        } else {
            view.setShownImage(ShownImage.ORIGINAL);
        }
    }

    // ===========================================================
    // Package private methods
    // ===========================================================

    void updateStatus(String status) {
        lblInfo.setText(status);
    }

    NSubject getSubject() {
        return subject;
    }

    NFingerScanner getSelectedScanner() {
        return (NFingerScanner) scannerList.getSelectedValue();
    }


    // ===========================================================
    // Protected methods
    // ===========================================================

    @Override
    protected void initGUI() {
        panelMain = new JPanel();
        panelScanners = new JPanel();
        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        panelButtons = new JPanel();
        scannerList = new JList();
        btnRefresh = new JButton();
        btnCancel = new JButton();
        btnScan = new JButton();
        btnForce = new JButton();
        cbAutomatic = new JCheckBox();
        scrollPane = new JScrollPane();
        view = new NFingerView();
        panelSouth = new JPanel();
        lblInfo = new JLabel();
        fcTemplate = new JFileChooser();
        fcImage = new JFileChooser();
        cbShowBinarized = new JCheckBox();
        btnAppendFinger = new JButton();
        btnEnrollFinger = new JButton();
        panelSave = new JPanel();
        panelInfo = new JPanel();

        setLayout(new BorderLayout());
        add(panelLicensing, BorderLayout.NORTH);
        panelMain.setLayout(new BorderLayout());
        add(panelMain, BorderLayout.CENTER);
        panelScanners.setBorder(BorderFactory.createTitledBorder("Scanners list"));
        panelScanners.setLayout(new BorderLayout());
        panelMain.add(panelScanners, BorderLayout.NORTH);
        scrollPaneList = new JScrollPane();
        scrollPaneList.setPreferredSize(new Dimension(0, 90));
        panelScanners.add(scrollPaneList, BorderLayout.CENTER);
        scannerList.setModel(new DefaultListModel());
        scannerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scannerList.setBorder(LineBorder.createBlackLineBorder());
        scannerList.addListSelectionListener(new ScannerSelectionListener());
        scrollPaneList.setViewportView(scannerList);

        panelButtons.setLayout(new FlowLayout(FlowLayout.LEADING));
        panelScanners.add(panelButtons, BorderLayout.SOUTH);
        btnRefresh.setText("Refresh list");
        btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateScannerList();
            }
        });
        panelButtons.add(btnRefresh);
        btnScan.setText("Scan");
        btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startCapturing();
            }
        });
        panelButtons.add(btnScan);
        btnCancel.setText("Cancel");
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelCapturing();
            }
        });
        panelButtons.add(btnCancel);
        btnForce.setText("Force");
        btnForce.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FingersTools.getInstance().getClient().force();
            }
        });
        panelButtons.add(btnForce);
        cbAutomatic.setSelected(true);
        cbAutomatic.setText("Scan automatically");
        panelButtons.add(cbAutomatic);

        panelMain.add(scrollPane, BorderLayout.CENTER);
        view.setShownImage(ShownImage.RESULT);
        view.setAutofit(true);
        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                super.mouseClicked(ev);
                if (ev.getButton() == MouseEvent.BUTTON3) {
                    cbShowBinarized.doClick();
                }
            }


        });
        scrollPane.setViewportView(view);
        panelSouth.setLayout(new BorderLayout());
        panelMain.add(panelSouth, BorderLayout.SOUTH);
        panelInfo.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        panelInfo.setLayout(new GridLayout(1, 1));
        panelSouth.add(panelInfo, BorderLayout.NORTH);
        lblInfo.setText(" ");
        panelInfo.add(lblInfo);
        panelSave.setLayout(new FlowLayout(FlowLayout.LEADING));
        panelSouth.add(panelSave, BorderLayout.WEST);
        btnIdentifyFinger = new JButton();
        btnIdentifyFinger.setText("Identify Finger");
        btnIdentifyFinger.setEnabled(false);
        btnIdentifyFinger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    identifyFinger(callBack);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        panelSave.add(btnIdentifyFinger);
        btnEnrollFinger.setText("Enroll Finger");
        btnEnrollFinger.setEnabled(false);
        btnEnrollFinger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    enrollFinger(callBack);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        panelSave.add(btnEnrollFinger);

        btnAppendFinger.setText("Append Finger");
        btnAppendFinger.setEnabled(false);
        btnAppendFinger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    appendFinger(callBack);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        panelSave.add(btnAppendFinger);
        cbShowBinarized.setSelected(true);
        cbShowBinarized.setText("Show binarized image");
        cbShowBinarized.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateShownImage();
            }
        });
        panelSave.add(cbShowBinarized);
        NViewZoomSlider zoomSlider = new NViewZoomSlider();
        zoomSlider.setView(view);
        panelSouth.add(zoomSlider, BorderLayout.EAST);


//        fcImage.setFileFilter(new Utils.ImageFileFilter(NImages.getSaveFileFilter()));

    }

    @Override
    protected void setDefaultValues() {
        // No default values.
    }


    public void updateControls() {

        System.out.println("UPDATE CONTROLS");

        btnScan.setEnabled(!scanning);
        btnCancel.setEnabled(scanning);
        btnForce.setEnabled(scanning && !cbAutomatic.isSelected());
        btnRefresh.setEnabled(!scanning);

        btnEnrollFinger.setEnabled(!scanning && (subject != null) && (subject.getStatus() == NBiometricStatus.OK) && enrollFinger);
        btnIdentifyFinger.setEnabled(!scanning && (subject != null) && (subject.getStatus() == NBiometricStatus.OK) && identifyFinger);
        btnAppendFinger.setEnabled(!scanning && (subject != null) && (subject.getStatus() == NBiometricStatus.OK) && appendFinger);
        cbShowBinarized.setEnabled(!scanning);
        cbAutomatic.setEnabled(!scanning);
    }

    @Override
    protected void updateFingersTools() {
        try {
            FingersTools fingersTools = FingersTools.getInstance();
            NBiometricClient nBiometricClient = fingersTools.getClient();
            nBiometricClient.reset();
            nBiometricClient.setUseDeviceManager(true);
            nBiometricClient.setFingersReturnBinarizedImage(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

// ===========================================================
// Public methods
// ===========================================================

    public void updateScannerList() {
        DefaultListModel model = (DefaultListModel) scannerList.getModel();
        model.clear();
        for (NDevice device : deviceManager.getDevices()) {
            model.addElement(device);
        }
        NFingerScanner scanner = (NFingerScanner) FingersTools.getInstance().getClient().getFingerScanner();
        if ((scanner == null) && (model.getSize() > 0)) {
            scannerList.setSelectedIndex(0);
        } else if (scanner != null) {
            scannerList.setSelectedValue(scanner, true);
        }
    }

    public void cancelCapturing() {
        FingersTools.getInstance().getClient().cancel();
    }

// ===========================================================
// Inner classes
// ===========================================================


    private class CaptureCompletionHandler implements CompletionHandler<NBiometricTask, Object> {

        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    scanning = false;
                    updateShownImage();
                    if (result.getStatus() == NBiometricStatus.OK) {
                        updateStatus("Quality: " + getSubject().getFingers().get(0).getObjects().get(0).getQuality());
                    } else {
                        updateStatus(result.getStatus().toString());
                    }
                    updateControls();
                }

            });
        }

        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    scanning = false;
                    updateShownImage();
                    showError(th);
                    updateControls();
                }

            });
        }


    }

    private class ScannerSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            FingersTools.getInstance().getClient().setFingerScanner(getSelectedScanner());
        }

    }

}
