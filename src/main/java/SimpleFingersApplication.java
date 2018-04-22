import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.neurotec.lang.NCore;
import com.neurotec.samples.util.LibraryManager;

public final class SimpleFingersApplication {

    // ===========================================================
    // Public static  method
    // ===========================================================
    private static Boolean isScannerAlreadyInvokedFlag = false;
    private static JFrame fingerPrintFrame = new JFrame();
    private static MainPanel mainPanel;


    public static void startScanner(final CallBack callBack, final String action) {

        LibraryManager.initLibraryPath();
        callBack.setAction(action);

        SwingUtilities.invokeLater(new Runnable() {


            public void run() {
                 mainPanel = new MainPanel(callBack,action);

//                if (isScannerAlreadyInvokedFlag) {
//                    System.out.println("it has second step");
//                    mainPanel = new MainPanel(callBack,action);
//                    mainPanel.setAction(action);
//                    callBack.setAction(action);
//                    System.out.println("Action on calback..."+callBack.getAction());
//                    System.err.println("Action Round 2:"+action);
//                    fingerPrintFrame.setVisible(true);
////                    EnrollFromScanner enrollFromScanner = new EnrollFromScanner(callBack, action);
////                    enrollFromScanner.cancelCapturing();
////                    enrollFromScanner.init();
//
//                } else {
                    fingerPrintFrame = new JFrame();
                    fingerPrintFrame.setTitle("mUzima Fingerprint Scanner");
                    fingerPrintFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    fingerPrintFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                           // NCore.shutdown();
                        }
                    });

                    fingerPrintFrame.add(mainPanel, BorderLayout.CENTER);
                    fingerPrintFrame.pack();
                    fingerPrintFrame.setLocationRelativeTo(null);
                    fingerPrintFrame.setVisible(true);
                    isScannerAlreadyInvokedFlag = true;
               // }

            }
        });
    }

    public static void DisposeScanner(){
        fingerPrintFrame.dispose();
    }

    // ===========================================================
    // Private constructor
    // ===========================================================

    public SimpleFingersApplication() {

    }
}
