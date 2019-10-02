package com.henryrobbins;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.simulation.CompareInstanceSolutions;
import com.henryrobbins.simulation.CompareRunTimes;
import com.henryrobbins.simulation.CompareSolvers;
import com.henryrobbins.solver.Solver;

public class Window extends JFrame {

	private JLabel simDirLbl= new JLabel("Simulation Directory:");
	private JButton simChooseBtn= new JButton("Choose File");
	private JFileChooser simFileChooser= new JFileChooser();
	private JLabel simDir= new JLabel("No Directory Selected");
	private Path simDirPath;

	private JLabel resultDirLbl= new JLabel("Result Directory:");
	private JButton resultChooseBtn= new JButton("Choose File");
	private JFileChooser resultFileChooser= new JFileChooser();
	private JLabel resultDir= new JLabel("No Directory Selected");
	private Path resultDirPath;

	private JLabel fileNameLbl= new JLabel("Result File Name:");
	private JTextField fileNameField= new JTextField(10);
	private JLabel csvLbl= new JLabel(".csv");

	private JLabel simTypeLbl= new JLabel("Simulation Type:");
	private JComboBox<String> typeChoose= new JComboBox<>(new String[] {
			"Instance - Compare Room Assignment",
			"Random - Compare Room Assignment",
			"Random - Compare Run Times" });
	private String simType= new String();

	private JLabel sizesLbl= new JLabel("Hotel Size:");
	private JTextField sizesField= new JTextField(10);

	private JLabel trialLbl= new JLabel("# of Trials:");
	private JTextField trialField= new JTextField(5);

	private JLabel solversLbl= new JLabel("SOLVERS");
	private HashMap<Solver<Assignment>, JCheckBox> solvers= new HashMap<>();
	private JLabel statsLbl= new JLabel("STATISTICS");
	private HashMap<Statistic<Assignment>, JCheckBox> stats= new HashMap<>();

	private JButton run= new JButton("Run Simulation");
	private JProgressBar progress= new JProgressBar(0, 100);

	/** Show the GUI */
	public static void main(String[] pars) {
		new Window();
	}

	/** Construct the GUI */
	public Window() {

		super("Hotel Room Assignment Simulation");

		// Default start with specific instance. Set visibility accordingly
		sizesLbl.setVisible(false);
		sizesField.setVisible(false);
		trialLbl.setVisible(false);
		trialField.setVisible(false);
		simDirLbl.setVisible(true);
		simChooseBtn.setVisible(true);
		simDir.setVisible(true);
		fileNameLbl.setVisible(false);
		fileNameField.setVisible(false);
		csvLbl.setVisible(false);

		// Show percentage on the progress bar
		progress.setStringPainted(true);

		// Set default simulation directory
		resultFileChooser.setCurrentDirectory(Paths.get("AMPL", "Simulations").toFile());
		resultFileChooser.setDialogTitle("Choose Simulation Directory");
		resultFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		resultFileChooser.setAcceptAllFileFilterUsed(false);

		// Set default result directory
		simFileChooser.setCurrentDirectory(Paths.get("AMPL", "Simulations").toFile());
		simFileChooser.setDialogTitle("Choose Result Directory");
		simFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		simFileChooser.setAcceptAllFileFilterUsed(false);

		for (Solver<Assignment> solver : Solver.ASSIGNMENT_SOLVERS) {
			solvers.put(solver, new JCheckBox(solver.toString()));
		}

		for (Statistic<Assignment> stat : Statistic.ASSIGNMENT_STATS) {
			stats.put(stat, new JCheckBox(stat.toString()));
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(500, 700));

		JPanel container= new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		JPanel topPanel= new JPanel();
		topPanel.setPreferredSize(new Dimension(500, 300));
		SpringLayout topLayout= new SpringLayout();
		topPanel.setLayout(topLayout);

		topPanel.add(simDirLbl);
		topPanel.add(simChooseBtn);
		topPanel.add(simDir);

		topPanel.add(resultDirLbl);
		topPanel.add(resultChooseBtn);
		topPanel.add(resultDir);

		topPanel.add(fileNameLbl);
		topPanel.add(fileNameField);
		topPanel.add(csvLbl);

		topPanel.add(typeChoose);
		topPanel.add(simTypeLbl);

		topPanel.add(sizesField);
		topPanel.add(sizesLbl);

		topPanel.add(trialLbl);
		topPanel.add(trialField);

		topLayout.putConstraint(SpringLayout.WEST, simDirLbl, 20, SpringLayout.WEST, topPanel);
		topLayout.putConstraint(SpringLayout.NORTH, simDirLbl, 20, SpringLayout.NORTH, topPanel);
		topLayout.putConstraint(SpringLayout.WEST, simChooseBtn, 5, SpringLayout.EAST, simDirLbl);
		topLayout.putConstraint(SpringLayout.NORTH, simChooseBtn, -3, SpringLayout.NORTH, simDirLbl);
		topLayout.putConstraint(SpringLayout.WEST, simDir, 5, SpringLayout.EAST, simChooseBtn);
		topLayout.putConstraint(SpringLayout.NORTH, simDir, 4, SpringLayout.NORTH, simChooseBtn);

		topLayout.putConstraint(SpringLayout.WEST, resultDirLbl, 0, SpringLayout.WEST, simDirLbl);
		topLayout.putConstraint(SpringLayout.NORTH, resultDirLbl, 15, SpringLayout.SOUTH, simDirLbl);
		topLayout.putConstraint(SpringLayout.WEST, resultChooseBtn, 0, SpringLayout.WEST, simChooseBtn);
		topLayout.putConstraint(SpringLayout.NORTH, resultChooseBtn, -5, SpringLayout.NORTH, resultDirLbl);
		topLayout.putConstraint(SpringLayout.WEST, resultDir, 5, SpringLayout.EAST, resultChooseBtn);
		topLayout.putConstraint(SpringLayout.NORTH, resultDir, 4, SpringLayout.NORTH, resultChooseBtn);

		topLayout.putConstraint(SpringLayout.WEST, fileNameLbl, 0, SpringLayout.WEST, resultDirLbl);
		topLayout.putConstraint(SpringLayout.NORTH, fileNameLbl, 15, SpringLayout.SOUTH, resultDirLbl);
		topLayout.putConstraint(SpringLayout.WEST, fileNameField, 0, SpringLayout.WEST, resultChooseBtn);
		topLayout.putConstraint(SpringLayout.NORTH, fileNameField, -5, SpringLayout.NORTH, fileNameLbl);
		topLayout.putConstraint(SpringLayout.WEST, csvLbl, 5, SpringLayout.EAST, fileNameField);
		topLayout.putConstraint(SpringLayout.NORTH, csvLbl, 4, SpringLayout.NORTH, fileNameField);

		topLayout.putConstraint(SpringLayout.WEST, simTypeLbl, 0, SpringLayout.WEST, fileNameLbl);
		topLayout.putConstraint(SpringLayout.NORTH, simTypeLbl, 15, SpringLayout.SOUTH, fileNameLbl);
		topLayout.putConstraint(SpringLayout.WEST, typeChoose, 0, SpringLayout.WEST, fileNameField);
		topLayout.putConstraint(SpringLayout.NORTH, typeChoose, -5, SpringLayout.NORTH, simTypeLbl);

		topLayout.putConstraint(SpringLayout.WEST, sizesLbl, 0, SpringLayout.WEST, simTypeLbl);
		topLayout.putConstraint(SpringLayout.NORTH, sizesLbl, 15, SpringLayout.SOUTH, simTypeLbl);
		topLayout.putConstraint(SpringLayout.WEST, sizesField, 0, SpringLayout.WEST, typeChoose);
		topLayout.putConstraint(SpringLayout.NORTH, sizesField, -5, SpringLayout.NORTH, sizesLbl);

		topLayout.putConstraint(SpringLayout.WEST, trialLbl, 0, SpringLayout.WEST, sizesLbl);
		topLayout.putConstraint(SpringLayout.NORTH, trialLbl, 15, SpringLayout.SOUTH, sizesLbl);
		topLayout.putConstraint(SpringLayout.WEST, trialField, 0, SpringLayout.WEST, sizesField);
		topLayout.putConstraint(SpringLayout.NORTH, trialField, -5, SpringLayout.NORTH, trialLbl);

		JPanel middlePanel= new JPanel();
		middlePanel.setPreferredSize(new Dimension(250, 600));
		middlePanel.setLayout(new GridLayout(0, 2));

		JPanel solversPanel= new JPanel();
		SpringLayout solversLayout= new SpringLayout();
		solversPanel.setLayout(solversLayout);

		solversPanel.add(solversLbl);
		for (Solver<Assignment> solver : solvers.keySet()) {
			solversPanel.add(solvers.get(solver));
			solvers.get(solver).setSelected(true);
		}
		solversLayout.putConstraint(SpringLayout.WEST, solversLbl, 10, SpringLayout.WEST, solversPanel);
		solversLayout.putConstraint(SpringLayout.NORTH, solversLbl, 10, SpringLayout.NORTH, solversPanel);

		Component lastComponent= solversLbl;

		for (Solver<Assignment> solver : solvers.keySet()) {
			JCheckBox box= solvers.get(solver);
			solversLayout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, solversPanel);
			solversLayout.putConstraint(SpringLayout.NORTH, box, 10, SpringLayout.SOUTH, lastComponent);
			lastComponent= box;
		}

		JPanel statsPanel= new JPanel();
		SpringLayout statsLayout= new SpringLayout();
		statsPanel.setLayout(statsLayout);

		statsPanel.add(statsLbl);
		for (Statistic<Assignment> stat : stats.keySet()) {
			statsPanel.add(stats.get(stat));
			stats.get(stat).setSelected(true);
		}
		statsLayout.putConstraint(SpringLayout.WEST, statsLbl, 10, SpringLayout.WEST, statsPanel);
		statsLayout.putConstraint(SpringLayout.NORTH, statsLbl, 10, SpringLayout.NORTH, statsPanel);

		lastComponent= statsLbl;

		for (Statistic<Assignment> stat : stats.keySet()) {
			JCheckBox box= stats.get(stat);
			statsLayout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, statsPanel);
			statsLayout.putConstraint(SpringLayout.NORTH, box, 10, SpringLayout.SOUTH, lastComponent);
			lastComponent= box;
		}

		middlePanel.add(solversPanel);
		middlePanel.add(statsPanel);

		topPanel.setAlignmentX(CENTER_ALIGNMENT);
		topPanel.setAlignmentY(TOP_ALIGNMENT);
		middlePanel.setAlignmentX(CENTER_ALIGNMENT);
		middlePanel.setAlignmentY(TOP_ALIGNMENT);
		run.setAlignmentX(CENTER_ALIGNMENT);
		progress.setAlignmentX(CENTER_ALIGNMENT);

		container.add(topPanel);
		container.add(middlePanel);
		container.add(run);
		container.add(progress);

		add(container);

		simChooseBtn.addActionListener(e -> {
			int returnVal= simFileChooser.showOpenDialog(Window.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				simDirPath= simFileChooser.getSelectedFile().toPath();
				simDir.setText(simDirPath.toFile().getName());
			}
		});

		resultChooseBtn.addActionListener(e -> {
			int returnVal= resultFileChooser.showOpenDialog(Window.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				resultDirPath= resultFileChooser.getSelectedFile().toPath();
				resultDir.setText(resultDirPath.toFile().getName());
			} else {}
		});

		typeChoose.addActionListener(e -> {
			simType= (String) typeChoose.getSelectedItem();
			if (simType.equals("Instance - Compare Room Assignment")) {
				sizesLbl.setVisible(false);
				sizesField.setVisible(false);
				trialLbl.setVisible(false);
				trialField.setVisible(false);
				simDirLbl.setVisible(true);
				simChooseBtn.setVisible(true);
				simDir.setVisible(true);
				fileNameLbl.setVisible(false);
				fileNameField.setVisible(false);
				csvLbl.setVisible(false);
			} else {
				sizesLbl.setVisible(true);
				sizesField.setVisible(true);
				trialLbl.setVisible(true);
				trialField.setVisible(true);
				simDirLbl.setVisible(false);
				simChooseBtn.setVisible(false);
				simDir.setVisible(false);
				fileNameLbl.setVisible(true);
				fileNameField.setVisible(true);
				csvLbl.setVisible(true);
			}
		});

		run.addActionListener(e -> {

			run.setEnabled(false);

			// Get current fields set by user
			File simDir= simFileChooser.getSelectedFile();
			Instance instance= InstanceFactory.readCSV(simDir.toPath(), 1);
			File resultDir= resultFileChooser.getSelectedFile();
			String name= fileNameField.getText();
			int[] sizes= {};
			if (sizesField.getText().length() > 0) {
				String[] size= sizesField.getText().split(",");
				sizes= new int[size.length];
				for (int i= 0; i < size.length; i++ ) {
					sizes[i]= Integer.parseInt(size[i]);
				}
			}
			int t= 0;
			if (trialField.getText().length() > 0) {
				t= Integer.parseInt(trialField.getText());
			} else {
				t= 1;
			}

			ArrayList<Solver<Assignment>> assignmentSolvers= new ArrayList<>();
			for (Solver<Assignment> solver : solvers.keySet()) {
				if (solvers.get(solver).isSelected()) {
					assignmentSolvers.add(solver);
				}
			}
			ArrayList<Statistic<Assignment>> assignmentStats= new ArrayList<>();
			for (Statistic<Assignment> stat : stats.keySet()) {
				if (stats.get(stat).isSelected()) {
					assignmentStats.add(stat);
				}
			}

			if (typeChoose.getSelectedItem().equals("Instance - Compare Room Assignment")) {

				new CompareInstanceSolutions<>(assignmentSolvers, assignmentStats, instance, resultDir, null).start();

			} else if (typeChoose.getSelectedItem().equals("Random - Compare Room Assignment")) {

				new CompareSolvers<>(t, sizes, assignmentSolvers, assignmentStats, resultDir, name, progress).start();

			} else if (typeChoose.getSelectedItem().equals("Random - Compare Run Times")) {

				new CompareRunTimes<>(t, sizes, assignmentSolvers, resultDir, name, progress).start();
			}

			run.setEnabled(true);

		});

		pack();
		setVisible(true);
	}
}
