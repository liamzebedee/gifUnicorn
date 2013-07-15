package net.cryptum.dev.gifunicorn;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

/*
 * GifUnicorn - A tool for compiling multiple images into a GIF
 *  By Liam Edwards-Playne (liamzebedee)
 *  - Licensed under GPLv2
 *  
 *  Read the README for more information (you don't say?)
 */
public class GifUnicorn extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	JButton jButtonChooseImages;
	JButton jButtonMakeAnimation;
	JFileChooser jFileChooser;
	JFileChooser jFileSaver;
	JProgressBar jProgressBar;
	JCheckBox jCheckBoxLoopContinously;
	JComboBox jComboBoxInterval;

	File[] imageFiles;
	File animationFile;

	boolean filesSelected = false;
	
	public GifUnicorn() {
		// Initialise frame
		JFrame jFrame = new JFrame("GifUnicorn by liamzebedee");
		jFrame.setSize(400, 200);
		Container container = jFrame.getContentPane();
		container.setBackground(Color.white);
		container.setLayout(new FlowLayout());
		
		// Setup interface
		jButtonChooseImages = new JButton("Choose Images...");
		jButtonChooseImages.addActionListener(this);

		jButtonMakeAnimation = new JButton("Create Animation...");
		jButtonMakeAnimation.addActionListener(this);

		jFileChooser = new JFileChooser();
		jFileChooser.setMultiSelectionEnabled(true);
		jFileChooser.setVisible(false);
		jFileChooser.setDialogTitle("Choose images to create animation from...");
		jFileChooser.setFileFilter(new ImageFileFilter());
		jFileChooser.setSize(500, 300);

		jFileSaver = new JFileChooser();
		jFileSaver.setVisible(false);
		jFileSaver.setSize(500, 300);
		jFileSaver.setDialogTitle("Save animation...");
		jFileSaver.setApproveButtonText("Save animation...");

		jComboBoxInterval = new JComboBox();
		jComboBoxInterval.addItem("25");
		jComboBoxInterval.addItem("50");
		jComboBoxInterval.addItem("100");
		jComboBoxInterval.addItem("150");
		jComboBoxInterval.addItem("200");
		jComboBoxInterval.addItem("250");
		jComboBoxInterval.addItem("500");

		jCheckBoxLoopContinously = new JCheckBox();

		jProgressBar = new JProgressBar();
		jProgressBar.setString("Build");

		// Add components
		container.add(jButtonChooseImages);
		container.add(new JLabel("Interval (ms)"));
		container.add(jComboBoxInterval);
		container.add(new JLabel("Loop Animation"));
		container.add(jCheckBoxLoopContinously);
		container.add(jButtonMakeAnimation);
		container.add(jFileChooser);
		container.add(jFileSaver);
		container.add(jProgressBar);
		jFrame.setVisible(true);
	}

	public static void main(String[] args) {
		new GifUnicorn();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		JButton jButton = (JButton) actionEvent.getSource();
		if (jButton == jButtonChooseImages) {
			jFileChooser.setVisible(true);
			jFileChooser.showOpenDialog(this);
			imageFiles = jFileChooser.getSelectedFiles();
		}
		else if (jButton == jButtonMakeAnimation) {
			if (imageFiles != null) {
				jFileSaver.setVisible(true);
				jFileSaver.showSaveDialog(this);
				this.animationFile = jFileSaver.getSelectedFile();
				new Thread(new AnimationCreator()).run();
			}
		}
	}

	class ImageFileFilter extends FileFilter {
		@Override
		public boolean accept(File file) {
			String fileExtension = Utils.getExtension(file);
			if(file.isDirectory()) return true;
			if(fileExtension == null) return false;
			else if(fileExtension.equals("png")) return true;
			else if(fileExtension.equals("jpg")) return true;
			else if(fileExtension.equals("jpeg")) return true;
			else if(fileExtension.equals("gif")) return true;
			else if(fileExtension.equals("tif")) return true;
			else if(fileExtension.equals("tiff")) return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Images: .png .jpg .jpeg .gif .tif .tiff";
		}
	}

	class AnimationCreator implements Runnable {

		public AnimationCreator() {}

		@Override
		public void run() {
			try {
				// Get output image type from first file in selection
				int firstImageType = ImageIO.read(imageFiles[0]).getType();

				ImageOutputStream imageOutputStream = new FileImageOutputStream(animationFile); 

				// Create a GIF sequence with the type of the first image, and options (loop, interval)
				GifSequenceWriter gifSequenceWriter = new GifSequenceWriter(
						imageOutputStream,
						firstImageType,
						Integer.parseInt((String) jComboBoxInterval.getSelectedItem()),
						true);

				int progressBarValue = 0;
				int progressBarIncrementAmount = 100 / imageFiles.length;
				jProgressBar.setValue(0);

				for(File file : imageFiles) {
					BufferedImage bufferedImage = ImageIO.read(file);
					gifSequenceWriter.writeToSequence(bufferedImage);

					progressBarValue += progressBarIncrementAmount;
					jProgressBar.setValue(progressBarValue);
					jProgressBar.updateUI();
				}
				
				jProgressBar.setValue(100);

				gifSequenceWriter.close();
				imageOutputStream.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
