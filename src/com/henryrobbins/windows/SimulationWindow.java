package com.henryrobbins.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.henryrobbins.RandProperties;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Hotel;
import com.henryrobbins.hotel.HotelFactory;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.simulation.SimAddGuest;
import com.henryrobbins.simulation.SimHotel;
import com.henryrobbins.simulation.SimInstanceSolvers;
import com.henryrobbins.simulation.SimParameters;
import com.henryrobbins.simulation.SimRunTimes;
import com.henryrobbins.simulation.SimShowTrials;
import com.henryrobbins.simulation.SimSolvers;
import com.henryrobbins.solver.Solver;

public class SimulationWindow extends JFrame {

	/** Auto-generated ID */
	private static final long serialVersionUID= -7560610944641740599L;

	private JPanel topPanel= new JPanel();
	private JPanel middlePanel= new JPanel();
	private JPanel bottomPanel= new JPanel();

	private JLabel simTypeLbl= new JLabel("Simulation Type:");
	private JComboBox<String> typeChoose= new JComboBox<>(new String[] {
			"Instance - Compare",
			"Random Instance - Compare",
			"Random - Compare",
			"Random - Compare (Trials)",
			"Random - Run Times",
			"Random - Parameters",
			"Random - Add Guest" });
	private String simType= new String();

	private JLabel resultDirLbl= new JLabel("Result Directory:");
	private JButton resultChooseBtn= new JButton("Choose File");
	private JFileChooser resultFileChooser= new JFileChooser();
	private JLabel resultDir= new JLabel("No Directory Selected");
	private Path resultDirPath;

	private JLabel fileNameLbl= new JLabel("Result File Name:");
	private JTextField fileNameField= new JTextField(20);
	private JLabel csvLbl= new JLabel(".csv");

	private JLabel paramLbl= new JLabel("Parameter File:");
	private JButton paramChooseBtn= new JButton("Choose File");
	private JFileChooser paramFileChooser= new JFileChooser();
	private JLabel param= new JLabel("No File Selected");
	private Path paramPath;

	private JLabel hotelLbl= new JLabel("Hotel CSV:");
	private JButton hotelChooseBtn= new JButton("Choose File");
	private JFileChooser hotelFileChooser= new JFileChooser();
	private JLabel hotelFileLbl= new JLabel("No File Selected");
	private Path hotelPath;

	private JLabel arrivalsLbl= new JLabel("Arrivals CSV:");
	private JButton arrivalsChooseBtn= new JButton("Choose File");
	private JFileChooser arrivalsFileChooser= new JFileChooser();
	private JLabel arrivalsFileLbl= new JLabel("No File Selected");
	private Path arrivalsPath;

	private JLabel weightsLbl= new JLabel("Weights CSV:");
	private JButton weightsChooseBtn= new JButton("Choose File");
	private JFileChooser weightsFileChooser= new JFileChooser();
	private JLabel weightsFileLbl= new JLabel("No File Selected");
	private Path weightsPath;

	private JLabel objLbl= new JLabel("Objective:");
	private JTextField objField= new JTextField(20);

	private JLabel sizesLbl= new JLabel("Hotel Size(s):");
	private JTextField sizesField= new JTextField(20);

	private JLabel trialLbl= new JLabel("# of Trials:");
	private JTextField trialField= new JTextField(5);

	private JLabel alphaLbl= new JLabel("Alpha(s):");
	private JTextField alphaField= new JTextField(20);

	private JLabel betaLbl= new JLabel("Beta(s):");
	private JTextField betaField= new JTextField(20);

	private JLabel gammaLbl= new JLabel("Gamma(s):");
	private JTextField gammaField= new JTextField(20);

	private JLabel solversLbl= new JLabel("SOLVERS");
	private LinkedHashMap<Solver<Assignment>, JCheckBox> solvers= new LinkedHashMap<>();

	private JLabel statsLbl= new JLabel("STATISTICS");
	private LinkedHashMap<Statistic<Assignment>, JCheckBox> stats= new LinkedHashMap<>();

	private JButton run= new JButton("Run Simulation");
	private JProgressBar progress= new JProgressBar(0, 100);

	private HashSet<JComponent> components= new HashSet<>(Arrays.asList(paramLbl, paramChooseBtn, param, hotelLbl,
		hotelChooseBtn, hotelFileLbl, arrivalsLbl, arrivalsChooseBtn, arrivalsFileLbl, weightsLbl, weightsChooseBtn,
		weightsFileLbl, sizesLbl, sizesField, trialLbl, trialField, alphaLbl, alphaField, betaLbl, betaField,
		gammaLbl, gammaField, objLbl, objField));

	/** Corrects the Construct the GUI */
	public SimulationWindow() {

		super("Hotel Room Assignment Simulation");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(560, 700));
		Dimension screenSize= new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		Dimension windowSize= new Dimension(getPreferredSize());
		int wdwLeft= screenSize.width / 2 - windowSize.width / 2;
		int wdwTop= screenSize.height / 2 - windowSize.height / 2 - 50;
		this.setLocation(wdwLeft, wdwTop);

		// Default start with specific instance. Set visibility accordingly
		clear();
		instanceConfig();
		solversAndStats();

		// Set default hotel directory
		hotelFileChooser.setDialogTitle("Choose Hotel CSV File");
		hotelFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		hotelFileChooser.setAcceptAllFileFilterUsed(false);

		// Set default arrivals directory
		arrivalsFileChooser.setDialogTitle("Choose Arrivals CSV File");
		arrivalsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		arrivalsFileChooser.setAcceptAllFileFilterUsed(false);

		// Set default weights directory
		weightsFileChooser.setDialogTitle("Choose Weights CSV File");
		weightsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		weightsFileChooser.setAcceptAllFileFilterUsed(false);

		// Set default results directory
		resultFileChooser.setDialogTitle("Choose Instance Directory");
		resultFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		resultFileChooser.setAcceptAllFileFilterUsed(false);

		// Set default parameter file
		paramFileChooser.setDialogTitle("Choose Parameter File");
		paramFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		paramFileChooser.setAcceptAllFileFilterUsed(false);

		GridBagConstraints c= new GridBagConstraints();
		c.insets= new Insets(3, 5, 3, 5);
		c.fill= GridBagConstraints.HORIZONTAL;

		topPanel= new JPanel(new GridBagLayout());
		topPanel.setPreferredSize(new Dimension(450, 200));

		c.gridx= 0;
		c.gridy= 0;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(simTypeLbl, c);

		c.gridx= 1;
		c.gridy= 0;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(typeChoose, c);
		c.gridwidth= 1;

		c.gridx= 0;
		c.gridy= 1;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(resultDirLbl, c);

		c.gridx= 1;
		c.gridy= 1;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(resultChooseBtn, c);

		c.gridx= 2;
		c.gridy= 1;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(resultDir, c);

		c.gridx= 0;
		c.gridy= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(fileNameLbl, c);
		c.gridwidth= 1;

		c.gridx= 1;
		c.gridy= 2;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(fileNameField, c);
		c.gridwidth= 1;

		c.gridx= 3;
		c.gridy= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(csvLbl, c);

		c.gridx= 0;
		c.gridy= 3;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(paramLbl, c);

		c.gridx= 1;
		c.gridy= 3;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(paramChooseBtn, c);

		c.gridx= 2;
		c.gridy= 3;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(param, c);

		c.gridx= 0;
		c.gridy= 4;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(hotelLbl, c);

		c.gridx= 1;
		c.gridy= 4;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(hotelChooseBtn, c);

		c.gridx= 2;
		c.gridy= 4;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(hotelFileLbl, c);

		c.gridx= 0;
		c.gridy= 5;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(arrivalsLbl, c);

		c.gridx= 1;
		c.gridy= 5;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(arrivalsChooseBtn, c);

		c.gridx= 2;
		c.gridy= 5;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(arrivalsFileLbl, c);

		c.gridx= 0;
		c.gridy= 6;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(weightsLbl, c);

		c.gridx= 1;
		c.gridy= 6;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(weightsChooseBtn, c);

		c.gridx= 2;
		c.gridy= 6;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(weightsFileLbl, c);

		c.gridx= 0;
		c.gridy= 6;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(sizesLbl, c);

		c.gridx= 1;
		c.gridy= 6;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(sizesField, c);
		c.gridwidth= 1;

		c.gridx= 0;
		c.gridy= 7;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(trialLbl, c);

		c.gridx= 1;
		c.gridy= 7;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(trialField, c);
		c.gridwidth= 1;

		c.gridx= 0;
		c.gridy= 8;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(objLbl, c);

		c.gridx= 1;
		c.gridy= 8;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(objField, c);
		c.gridwidth= 1;

		c.gridx= 0;
		c.gridy= 9;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(alphaLbl, c);

		c.gridx= 1;
		c.gridy= 9;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(alphaField, c);
		c.gridwidth= 1;

		c.gridx= 0;
		c.gridy= 10;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(betaLbl, c);

		c.gridx= 1;
		c.gridy= 10;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(betaField, c);
		c.gridwidth= 1;

		c.gridx= 0;
		c.gridy= 11;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(gammaLbl, c);

		c.gridx= 1;
		c.gridy= 11;
		c.gridwidth= 2;
		c.anchor= GridBagConstraints.LINE_START;
		topPanel.add(gammaField, c);
		c.gridwidth= 1;

		middlePanel= new JPanel(new GridBagLayout());
		middlePanel.setPreferredSize(new Dimension(450, 200));

		c.gridx= 0;
		c.gridy= 0;
		c.anchor= GridBagConstraints.CENTER;
		middlePanel.add(solversLbl, c);

		for (Solver<Assignment> solver : Solver.ASSIGNMENT_SOLVERS) {
			c.gridy++ ;
			c.anchor= GridBagConstraints.LINE_START;
			JCheckBox cBox= new JCheckBox(solver.toString());
			cBox.setSelected(false);
			solvers.put(solver, cBox);
			middlePanel.add(cBox, c);
		}

		c.gridx= 1;
		c.gridy= 0;
		c.anchor= GridBagConstraints.CENTER;
		middlePanel.add(statsLbl, c);

		for (Statistic<Assignment> stat : Statistic.ASSIGNMENT_STATS) {
			c.gridy++ ;
			c.anchor= GridBagConstraints.LINE_START;
			JCheckBox cBox= new JCheckBox(stat.toString());
			cBox.setSelected(false);
			stats.put(stat, cBox);
			middlePanel.add(cBox, c);
		}

		bottomPanel= new JPanel(new GridBagLayout());
		bottomPanel.setPreferredSize(new Dimension(450, 200));

		c.gridx= 1;
		c.gridy= 0;
		c.anchor= GridBagConstraints.CENTER;
		bottomPanel.add(run, c);

		c.gridx= 0;
		c.gridy= 1;
		c.gridheight= 2;
		c.gridwidth= 3;
		c.anchor= GridBagConstraints.CENTER;
		bottomPanel.add(progress, c);
		progress.setStringPainted(true);
		c.gridheight= 1;
		c.gridwidth= 1;

		JPanel container= new JPanel(new GridBagLayout());
		topPanel.setPreferredSize(new Dimension(450, 600));

		c.ipadx= 10;
		c.ipady= 10;

		c.gridx= 0;
		c.gridy= 0;
		c.anchor= GridBagConstraints.CENTER;
		container.add(topPanel, c);

		c.gridx= 0;
		c.gridy= 1;
		c.anchor= GridBagConstraints.CENTER;
		container.add(middlePanel, c);

		c.gridx= 0;
		c.gridy= 2;
		c.anchor= GridBagConstraints.CENTER;
		container.add(bottomPanel, c);

		add(container);

		resultChooseBtn.addActionListener(e -> {
			int returnVal= resultFileChooser.showOpenDialog(SimulationWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				resultDirPath= resultFileChooser.getSelectedFile().toPath();
				resultDir.setText(resultDirPath.toFile().getName());
			} else {}
		});

		paramChooseBtn.addActionListener(e -> {
			int returnVal= paramFileChooser.showOpenDialog(SimulationWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				paramPath= paramFileChooser.getSelectedFile().toPath();
				param.setText(paramPath.toFile().getName());
			} else {}
		});

		hotelChooseBtn.addActionListener(e -> {
			int returnVal= hotelFileChooser.showOpenDialog(SimulationWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				hotelPath= hotelFileChooser.getSelectedFile().toPath();
				hotelFileLbl.setText(hotelPath.toFile().getName());
			}
		});

		arrivalsChooseBtn.addActionListener(e -> {
			int returnVal= arrivalsFileChooser.showOpenDialog(SimulationWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				arrivalsPath= arrivalsFileChooser.getSelectedFile().toPath();
				arrivalsFileLbl.setText(arrivalsPath.toFile().getName());
			}
		});

		weightsChooseBtn.addActionListener(e -> {
			int returnVal= weightsFileChooser.showOpenDialog(SimulationWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				weightsPath= weightsFileChooser.getSelectedFile().toPath();
				weightsFileLbl.setText(weightsPath.toFile().getName());
			}
		});

		typeChoose.addActionListener(e -> {
			simType= (String) typeChoose.getSelectedItem();
			String[] type= simType.split(" - ");

			clear();

			if (type[0].equals("Instance")) {
				instanceConfig();
			} else if (type[0].equals("Random")) {
				randConfig();
			} else if (type[0].equals("Random Instance")) {
				randConfig();
				hotelLbl.setVisible(true);
				hotelChooseBtn.setVisible(true);
				hotelFileLbl.setVisible(true);
				sizesLbl.setVisible(false);
				sizesField.setVisible(false);
			}

			if (type[1].equals("Compare")) {
				solversAndStats();
			} else if (type[1].equals("Compare (Trials)")) {
				solversAndStats();
			} else if (type[1].equals("Run Times")) {
				solversAndStats();
			} else if (type[1].equals("Parameters")) {
				parametersVisb();
			} else if (type[1].equals("Add Guest")) {
				// nothing
			}

		});

		run.addActionListener(e -> {
			try {
				run.setEnabled(false);

				// get current universal fields set by user
				File resultDir= resultFileChooser.getSelectedFile();
				String name= fileNameField.getText();

				// if random, set given param file
				if (typeChoose.getSelectedItem().toString().split(" - ")[0].equals("Random") ||
					typeChoose.getSelectedItem().toString().split(" - ")[0].equals("Random Instance")) {
					RandProperties.set(paramFileChooser.getSelectedFile());
				}

				// run chosen simulation
				if (typeChoose.getSelectedItem().equals("Instance - Compare")) {
					Hotel hotel= HotelFactory.readCSV(hotelPath);
					Instance instance= InstanceFactory.readCSV(hotel, arrivalsPath, weightsPath);
					new SimInstanceSolvers<>(getSolvers(), getStats(), instance, resultDir, name, null).start();
				} else if (typeChoose.getSelectedItem().equals("Random Instance - Compare")) {
					Hotel hotel= HotelFactory.readCSV(hotelPath);
					new SimHotel<>(getT(), hotel, getSolvers(), getStats(), resultDir, name,
						progress).start();
				} else if (typeChoose.getSelectedItem().equals("Random - Compare")) {
					new SimSolvers<>(getT(), getSizes(), getSolvers(), getStats(), resultDir, name, progress).start();
				} else if (typeChoose.getSelectedItem().equals("Random - Compare (Trials)")) {
					new SimShowTrials<>(getT(), getSizes()[0], getSolvers(), getStats(), resultDir, name, progress)
						.start();
				} else if (typeChoose.getSelectedItem().equals("Random - Run Times")) {
					new SimRunTimes<>(getT(), getSizes(), getSolvers(), resultDir, name, progress).start();
				} else if (typeChoose.getSelectedItem().equals("Random - Parameters")) {
					new SimParameters(getT(), getSizes()[0], getAlphas(), getBetas(), getGammas(),
						objField.getText(), Statistic.ASSIGNMENT_STATS, resultDir, name, progress).start();
				} else if (typeChoose.getSelectedItem().equals("Random - Add Guest")) {
					new SimAddGuest(getT(), getSizes(), resultDir, name, progress).start();
				}
			} catch (Exception exep) {
				exep.printStackTrace();
				JOptionPane optionPane= new JOptionPane(exep.getMessage(), JOptionPane.ERROR_MESSAGE);
				JDialog dialog= optionPane.createDialog("Error Message");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}

			run.setEnabled(true);

		});

		pack();

		setVisible(true);
	}

	// methods for setting gui configuration

	private void clear() {
		for (JComponent comp : components) {
			comp.setVisible(false);
		}
		middlePanel.setVisible(false);
	}

	private void instanceConfig() {
		hotelLbl.setVisible(true);
		hotelChooseBtn.setVisible(true);
		hotelFileLbl.setVisible(true);
		arrivalsLbl.setVisible(true);
		arrivalsChooseBtn.setVisible(true);
		arrivalsFileLbl.setVisible(true);
		weightsLbl.setVisible(true);
		weightsChooseBtn.setVisible(true);
		weightsFileLbl.setVisible(true);
	}

	private void randConfig() {
		paramLbl.setVisible(true);
		paramChooseBtn.setVisible(true);
		param.setVisible(true);
		sizesLbl.setVisible(true);
		sizesField.setVisible(true);
		trialLbl.setVisible(true);
		trialField.setVisible(true);
	}

	private void parametersVisb() {
		objLbl.setVisible(true);
		objField.setVisible(true);
		alphaLbl.setVisible(true);
		alphaField.setVisible(true);
		betaLbl.setVisible(true);
		betaField.setVisible(true);
		gammaLbl.setVisible(true);
		gammaField.setVisible(true);
	}

	private void solversAndStats() {
		middlePanel.setVisible(true);
	}

	// methods for getting user inputs

	private ArrayList<Solver<Assignment>> getSolvers() {
		ArrayList<Solver<Assignment>> assignmentSolvers= new ArrayList<>();
		for (Solver<Assignment> solver : Solver.ASSIGNMENT_SOLVERS) {
			if (solvers.get(solver).isSelected()) {
				assignmentSolvers.add(solver);
			}
		}
		return assignmentSolvers;
	}

	private ArrayList<Statistic<Assignment>> getStats() {
		ArrayList<Statistic<Assignment>> assignmentStats= new ArrayList<>();
		for (Statistic<Assignment> stat : Statistic.ASSIGNMENT_STATS) {
			if (stats.get(stat).isSelected()) {
				assignmentStats.add(stat);
			}
		}
		return assignmentStats;
	}

	private int getT() {
		if (trialField.getText().length() > 0) {
			return Integer.parseInt(trialField.getText());
		} else {
			return 1;
		}
	}

	private int[] getSizes() {
		int[] sizes= {};
		if (sizesField.getText().length() > 0) {
			String[] size= sizesField.getText().split(",");
			sizes= new int[size.length];
			for (int i= 0; i < size.length; i++ ) {
				sizes[i]= Integer.parseInt(size[i]);
			}
		}
		return sizes;
	}

	private double[] getAlphas() {
		double[] alphas= {};
		if (alphaField.getText().length() > 0) {
			String[] alpha= alphaField.getText().split(",");
			alphas= new double[alpha.length];
			for (int i= 0; i < alpha.length; i++ ) {
				alphas[i]= Double.parseDouble(alpha[i]);
			}
		}
		return alphas;
	}

	private double[] getBetas() {
		double[] betas= {};
		if (betaField.getText().length() > 0) {
			String[] beta= betaField.getText().split(",");
			betas= new double[beta.length];
			for (int i= 0; i < beta.length; i++ ) {
				betas[i]= Double.parseDouble(beta[i]);
			}
		}
		return betas;
	}

	private double[] getGammas() {
		double[] gammas= {};
		if (gammaField.getText().length() > 0) {
			String[] gamma= gammaField.getText().split(",");
			gammas= new double[gamma.length];
			for (int i= 0; i < gamma.length; i++ ) {
				gammas[i]= Double.parseDouble(gamma[i]);
			}
		}
		return gammas;
	}
}