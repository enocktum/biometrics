import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel implements ChangeListener {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	private String action;

	// ===========================================================
	// Static constructor
	// ===========================================================

	static {
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private JTabbedPane tabbedPane;
	private EnrollFromScanner enrollFromScanner;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public MainPanel(CallBack callBack,String action) {
		super(new GridLayout(1, 1));
		this.action = action;
		initGUI(callBack,action);
	}



	// ===========================================================
	// Private methods
	// ===========================================================

	private void initGUI(CallBack callBack,String action) {
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);

		enrollFromScanner = new EnrollFromScanner(callBack,this.action);
		enrollFromScanner.init();
		tabbedPane.addTab("", enrollFromScanner);

		add(tabbedPane);
		setPreferredSize(new Dimension(680, 600));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void obtainLicenses(BasePanel panel) throws IOException {
		if (!panel.isObtained()) {
			boolean status = FingersTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
			FingersTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
			panel.getLicensingPanel().setRequiredComponents(panel.getRequiredLicenses());
			panel.getLicensingPanel().setOptionalComponents(panel.getOptionalLicenses());
			panel.updateLicensing(status);
		}
	}

	// ===========================================================
	// Event handling
	// ===========================================================

	//@Override
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == tabbedPane) {
			try {
				switch (tabbedPane.getSelectedIndex()) {
					case 0: {
						obtainLicenses(enrollFromScanner);
						enrollFromScanner.updateFingersTools();
						enrollFromScanner.updateScannerList();
						break;
					}
					default: {
						throw new IndexOutOfBoundsException("unreachable");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Could not obtain licenses for components: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
