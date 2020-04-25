package com.henryrobbins.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;

import com.henryrobbins.AMPLHelper;
import com.henryrobbins.ResourcesPath;

public class StartWindow extends JFrame {

	/** Auto-generated ID */
	private static final long serialVersionUID= -4211345274433011576L;

	private JLabel dirLbl= new JLabel("AMPL Directory:");
	private JButton dirChooseBtn= new JButton("Choose Directory");
	private JFileChooser dirChooser= new JFileChooser();
	private JLabel dir= new JLabel("No Directory Selected");
	private Path dirPath;

	private JButton done= new JButton("Done");

	/** Extract resources. Set java.library.path. Show the GUI */
	public static void main(String[] pars) {

		// Extract all resources and set resources path
		try {
			File resources= Files.createTempDirectory("resources").toFile();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(resources)));

			File libDir= new File(resources, "lib");
			libDir.mkdir();

			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/lib/libampl.2.0.3.0.dylib"),
				new File(libDir, "libampl.2.0.3.0.dylib"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/lib/libampl.2.dylib"),
				new File(libDir, "libampl.2.dylib"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/lib/libampl.dylib"),
				new File(libDir, "libampl.dylib"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/lib/libjavaswigwrapper.jnilib"),
				new File(libDir, "libjavaswigwrapper.jnilib"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/lib/libcsharpswigwrapper.so"),
				new File(libDir, "libcsharpswigwrapper.so"));

			File modelDir= new File(resources, "models");
			modelDir.mkdir();

			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/models/assignment.mod"),
				new File(modelDir, "assignment.mod"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/models/schedule.mod"),
				new File(modelDir, "schedule.mod"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/models/solution.mod"),
				new File(modelDir, "solution.mod"));

			File testDir= new File(resources, "tests");
			testDir.mkdir();

			for (int i= 0; i < 6; i++ ) {
				File sim= new File(testDir, "" + i);
				sim.mkdir();
				FileUtils.copyInputStreamToFile(
					StartWindow.class.getClass().getResourceAsStream("/resources/tests/" + i + "/hotel.csv"),
					new File(sim, "hotel.csv"));
				File inst= new File(sim, "0");
				inst.mkdir();
				FileUtils.copyInputStreamToFile(
					StartWindow.class.getClass().getResourceAsStream("/resources/tests/" + i + "/0/arrivals.csv"),
					new File(inst, "arrivals.csv"));
				FileUtils.copyInputStreamToFile(
					StartWindow.class.getClass().getResourceAsStream("/resources/tests/" + i + "/0/weights.csv"),
					new File(inst, "weights.csv"));
			}

			File paramDir= new File(resources, "params");

			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/params/default.properties"),
				new File(paramDir, "default.properties"));
			FileUtils.copyInputStreamToFile(
				StartWindow.class.getClass().getResourceAsStream("/resources/params/1TypeNoRand.properties"),
				new File(paramDir, "1TypeNoRand.properties"));

			ResourcesPath.setPath(Paths.get(resources.getAbsolutePath()));

		} catch (Exception e) {
			JOptionPane optionPane= new JOptionPane("Error extracting resources", JOptionPane.ERROR_MESSAGE);
			JDialog dialog= optionPane.createDialog("Error Message");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			e.printStackTrace();
		}

		// Set java.library.path
		try {
			System.setProperty("java.library.path", ResourcesPath.path().toString() + "/lib");
			Field field= ClassLoader.class.getDeclaredField("sys_paths");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {
			JOptionPane optionPane= new JOptionPane("Error setting java.library.path", JOptionPane.ERROR_MESSAGE);
			JDialog dialog= optionPane.createDialog("Error Message");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			e.printStackTrace();
		}

		new StartWindow();
	}

	/** Construct the GUI */
	public StartWindow() {

		super("Start Screen");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(500, 100));
		Dimension screenSize= new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		Dimension windowSize= new Dimension(getPreferredSize());
		int wdwLeft= screenSize.width / 2 - windowSize.width / 2;
		int wdwTop= screenSize.height / 2 - windowSize.height / 2 - 50;
		this.setLocation(wdwLeft, wdwTop);

		dirChooser.setDialogTitle("Select AMPL directory location");
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dirChooser.setAcceptAllFileFilterUsed(false);

		JPanel panel= new JPanel(new GridBagLayout());
		panel.setPreferredSize(new Dimension(500, 100));
		GridBagConstraints c= new GridBagConstraints();
		c.fill= GridBagConstraints.HORIZONTAL;

		c.gridx= 0;
		c.gridy= 0;
		c.anchor= GridBagConstraints.LINE_START;
		panel.add(dirLbl, c);

		c.gridx= 1;
		c.gridy= 0;
		c.anchor= GridBagConstraints.LINE_START;
		panel.add(dirChooseBtn, c);

		c.gridx= 2;
		c.gridy= 0;
		c.anchor= GridBagConstraints.LINE_START;
		panel.add(dir, c);

		c.gridx= 1;
		c.gridy= 1;
		c.anchor= GridBagConstraints.CENTER;
		panel.add(done, c);

		add(panel);

		dirChooseBtn.addActionListener(e -> {
			int returnVal= dirChooser.showOpenDialog(StartWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				dirPath= dirChooser.getSelectedFile().toPath();
				dir.setText(dirPath.toFile().getName());
			} else {}
		});

		/** construct the SimulationWindow. */
		done.addActionListener(e -> {

			boolean valid= true;
			String[] files= { "ampl", "ampl.lic", "gurobi", "libgurobi81.dylib" };
			HashMap<String, Boolean> amplFiles= new HashMap<>();
			for (String file : files) {
				amplFiles.put(file, true);
			}

			try {
				for (String file : files) {
					if (!dirPath.resolve(file).toFile().exists()) {
						valid= false;
						amplFiles.put(file, false);
					}
				}

				if (!valid) { throw new IllegalArgumentException(); }

				AMPLHelper.setPath(dirPath);
				new SimulationWindow();
				dispose();

			} catch (NullPointerException exep) {
				JOptionPane optionPane= new JOptionPane("Select an AMPL folder", JOptionPane.ERROR_MESSAGE);
				JDialog dialog= optionPane.createDialog("No AMPL folder given");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			} catch (IllegalArgumentException exep) {
				String msg= "Invalid AMPL folder. Directory does not contain: \n";
				for (String file : files) {
					if (!amplFiles.get(file)) {
						msg+= file + "\n";
					}
				}
				JOptionPane optionPane= new JOptionPane(msg, JOptionPane.ERROR_MESSAGE);
				JDialog dialog= optionPane.createDialog("Invalid AMPL folder");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		});

		pack();
		setVisible(true);
	}
}
