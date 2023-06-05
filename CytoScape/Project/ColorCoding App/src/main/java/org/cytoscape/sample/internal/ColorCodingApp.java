package org.cytoscape.sample.internal;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;

import org.cytoscape.model.CyTable;

import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ColorCodingApp extends AbstractCyAction {
	
	private CyApplicationManager cyApplicationManager;

	private JFrame frame;
	private JTextField fileField1;
	private JTextField fileField2;

	public ColorCodingApp(CyApplicationManager cyApplicationManager) {
		super("Color Coding App");
		setPreferredMenu("Apps.Samples");
		this.cyApplicationManager = cyApplicationManager;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Createinputbox();
	}

	//creation of inputbox which prompts user for source nodes file, destination nodes file and pathlength
	private void Createinputbox() {
		frame = new JFrame("Two File Dialog Example");
		frame.setSize(500, 200);
		frame.setLocationRelativeTo(null);

		JLabel label1 = new JLabel(" Choose file for Source Nodes        :");
		JLabel label2 = new JLabel(" Choose file for Destination Nodes :");
		JLabel label3 = new JLabel("Enter the path length:");
		JLabel label4 = new JLabel("Enter the success probability:");
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1)); // set initial value, minimum, maximum,
																				// and step

		fileField1 = new JTextField(20);
		fileField2 = new JTextField(20);
		JButton fileButton1 = new JButton("Choose File");
		JButton fileButton2 = new JButton("Choose File");
		JButton okButton = new JButton("OK");
		JTextField textBox = new JTextField(20);
		
		okButton.addActionListener(
				new OkbuttonActionListener(fileField1, fileField2, spinner, frame, cyApplicationManager,textBox));
		JPanel panel = new JPanel();

		panel.add(label1);
		panel.add(fileButton1);
		panel.add(fileField1);

		panel.add(label2);
		panel.add(fileButton2);
		panel.add(fileField2);

		label3.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(label3);
		panel.add(spinner);

		
		label4.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(label4);
		panel.add(textBox);

		frame.add(panel);
		frame.add(okButton, BorderLayout.SOUTH);

		// Add action listeners to the "Choose File" buttons
		fileButton1.addActionListener(new FileChooserActionListener(fileField1));
		fileButton2.addActionListener(new FileChooserActionListener(fileField2));

		// Show the frame
		frame.setVisible(true);
	}

	private class FileChooserActionListener implements ActionListener {
		private final JTextField fileField;

		public FileChooserActionListener(JTextField fileField) {
			this.fileField = fileField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				fileField.setText(selectedFile.getAbsolutePath());
			}
		}
	}

	

	private class OkbuttonActionListener implements ActionListener {
		private final JTextField fileField1;
		private final JTextField fileField2;
		private final JFrame frame;
		private final CyApplicationManager cyApplicationManager;
		private final JSpinner spinner;
		private final JTextField prob_field;
		private Map<String, Integer> stringtoint = new HashMap<String, Integer>();
		private Map<Integer, String> inttostring = new HashMap<Integer, String>();

		public OkbuttonActionListener(JTextField fileField1, JTextField fileField2, JSpinner spinner, JFrame frame,
				CyApplicationManager cyApplicationManager,JTextField prob_field) {
			this.fileField1 = fileField1;
			this.fileField2 = fileField2;
			this.frame = frame;
			this.cyApplicationManager = cyApplicationManager;
			this.spinner = spinner;
			this.prob_field=prob_field;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Check that both fields have valid file paths
			String filePath1 = fileField1.getText();
			String filePath2 = fileField2.getText();
			if (filePath1.isEmpty() || filePath2.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please choose two files.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			File file1 = new File(filePath1);
			File file2 = new File(filePath2);
			if (!file1.exists() || !file2.exists()) {
				JOptionPane.showMessageDialog(frame, "Please choose valid files.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		
			System.out.println("Selected files: " + file1.getAbsolutePath() + ", " + file2.getAbsolutePath());
			frame.setVisible(false);
			List<String> sourcenodes = filereader(filePath1);
			List<String> destinationnodes = filereader(filePath2);
			int path_length = ((Number) spinner.getValue()).intValue();
			String prob_text =prob_field.getText();
			double prob = Double.valueOf(prob_text);
			Start(sourcenodes, destinationnodes, path_length,prob);
		}

		private List<String> filereader(String fileName) {
			List<String> lines = new ArrayList<String>();

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(fileName));
				String line = null;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
			} catch (IOException e) {
				System.err.println("Error reading file: " + e.getMessage());
			}
			return lines;
		}

		public void Start(List<String> sourcenodes, List<String> destinationnodes, int path_length,double prob) {

			// Fetching current CytoScape network
			CyNetwork net = cyApplicationManager.getCurrentNetwork();
			CyNetworkView networkView = cyApplicationManager.getCurrentNetworkView();
			if (net == null)
				throw new NullPointerException("network is null");

			//Fetching all rows of network.
			Collection<CyRow> rows = net.getDefaultNodeTable().getAllRows();
			for (CyRow row : rows) {
				CyNode node = net.getNode(row.get(CyTable.SUID, Long.class));

				String myNodeName = net.getRow(node).get(CyNetwork.NAME, String.class);
				View<CyNode> nodeView = networkView.getNodeView(node);
                
				//coloring source nodes to green and destination nodes to red.
				if (sourcenodes.contains(myNodeName)) {
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.green);
				} else if (destinationnodes.contains(myNodeName)) {
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.red);
				}
				// else{
				// nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.white);
				// }
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE, LineTypeVisualProperty.SOLID);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH, 10.0);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, myNodeName);
				networkView.updateView();
			}

			// BUILDING GRAPH FROM CYTOSCAPE NETWORK
			int numvertices = 0;
			for (CyRow row : rows) {
				CyNode node = net.getNode(row.get(CyTable.SUID, Long.class));
				String myNodeName = net.getRow(node).get(CyNetwork.NAME, String.class);
				stringtoint.put(myNodeName, numvertices);
				inttostring.put(numvertices, myNodeName);
				numvertices = numvertices + 1;
			}

			

			//for building graph as adjacency list
			ArrayList<ArrayList<Integer>> graph_adjlist = new ArrayList<ArrayList<Integer>>(numvertices);
			for (int i = 0; i < numvertices; i++) {
				graph_adjlist.add(new ArrayList<Integer>());
			}

			for (CyRow row : rows) {
				CyNode node = net.getNode(row.get(CyTable.SUID, Long.class));
				List<CyEdge> adj = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
				for (CyEdge edge : adj) {

					CyNode source = edge.getSource();
					CyNode target = edge.getTarget();

					String sourceNodeName = net.getRow(source).get(CyNetwork.NAME, String.class);
					String targetNodeName = net.getRow(target).get(CyNetwork.NAME, String.class);

					Integer sourcenum = stringtoint.get(sourceNodeName);
					Integer targetnum = stringtoint.get(targetNodeName);
					if (!graph_adjlist.get(sourcenum).contains(targetnum)) {
						graph_adjlist.get(sourcenum).add(targetnum);
					}
					if (!graph_adjlist.get(targetnum).contains(sourcenum)) {
						graph_adjlist.get(targetnum).add(sourcenum);
					}
				}
			}

			//for building graph as adjacency matrix
			double[][] graph_adjMat = new double[numvertices + 1][numvertices + 1];
			for (int i = 0; i < numvertices; i++) {
				for (int j = 0; j < numvertices; j++) {
					graph_adjMat[i][j] = 0.0;
				}
			}
			
			List<Integer> sn = new ArrayList<Integer>();
			List<Integer> dn = new ArrayList<Integer>();
			for (String s : sourcenodes) {
				sn.add(stringtoint.get(s));
			}
			for (String d : destinationnodes) {
				dn.add(stringtoint.get(d));
			}
			CyTable tb = cyApplicationManager.getCurrentTable();
			if (tb.getColumn("edgeWeight") != null) {
				CyColumn edgeweights = tb.getColumn("edgeWeight");
				CyColumn sharedNames = tb.getColumn("shared name");
				List<Object> shared_names = sharedNames.getValues(sharedNames.getType());
				List<Object> edge_weights = edgeweights.getValues(edgeweights.getType());
				int size = edge_weights.size();
				for (int i = 0; i < size; i++) {
					String xinteractsy = shared_names.get(i).toString();
					String edwgt = edge_weights.get(i).toString();
					String[] splitted = xinteractsy.split(" ");
					String snode = splitted[0];
					String dnode = splitted[3];
					Integer sintnode = stringtoint.get(snode);
					Integer dintnode = stringtoint.get(dnode);
					Double intedwgt = Double.parseDouble(edwgt);
					graph_adjMat[sintnode][dintnode] = intedwgt;
					graph_adjMat[dintnode][sintnode] = intedwgt;
				}
			}

			ColorCodingImpl(numvertices, graph_adjlist, graph_adjMat, sn, dn, path_length,prob);
            // networkView = networkView2;
			// networkView.updateView();
		}

		
		void ColorCodingImpl(int numVertices, ArrayList<ArrayList<Integer>> graph_adjlist, double graph_adjMat[][],
				List<Integer> sourcenodes, List<Integer> destinationnodes, int path_length, double prob) {
			colorCodingImpl CCI = new colorCodingImpl();
			long start = System.nanoTime();
			ArrayList<result> results = CCI.ColorCoding(numVertices, graph_adjlist, graph_adjMat, sourcenodes,
					destinationnodes, path_length, prob);
			long end = System.nanoTime();
			long timeTaken = end - start;

			JFrame frame = new JFrame("Results");
			JPanel sidebar = new JPanel();
			sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
			sidebar.setBackground(Color.LIGHT_GRAY);

			for (result i : results) {

				ArrayList<Integer> path = i.path;
				double pathcost = i.pathcost;
				String prints = "Path is ";
				int ind = 0;
				for (int j : path) {
					prints += inttostring.get(j);
					if (ind != path.size() - 1) {
						prints += "-->";
					}
					ind++;
				}
				JLabel label1 = new JLabel("<html>" +prints + "<br> </html>");
				sidebar.add(label1);
				String prints1 = "Cost of Path :" + " " + Double.toString(pathcost);
				JLabel label2 = new JLabel("<html>"+prints1 + "<br> </html>");
				sidebar.add(label2);
				sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
				sidebar.add(new JSeparator(JSeparator.HORIZONTAL));
				sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
				
			}
			sidebar.setBounds(0, 0, 500, results.size()*200);
			JScrollPane scrollPane = new JScrollPane(sidebar,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			frame.getContentPane().add(scrollPane);
			frame.pack();
			// frame.add(sidebar);
			frame.setSize(500, results.size() * 200);
			// frame.setLayout(null);
			frame.setVisible(true);
			JOptionPane.showMessageDialog(null, "Time taken is: " + timeTaken / (double) 1000000 + "ms");

		}

	}

}
