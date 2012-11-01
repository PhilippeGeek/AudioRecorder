import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;

public class AudioClassroom extends JApplet implements ActionListener, ChangeListener, ItemListener {

	private static final long serialVersionUID = -9086481880151742444L;
	// Global declarations
	protected boolean running;
	ByteArrayOutputStream out = null;
	AudioFileFormat.Type fileType;
	Object lock = new Object();
	TargetDataLine line = null;
	SourceDataLine sline = null;
	volatile boolean paused = false;
	boolean first;

	JButton record;
	JButton play;
	JButton stop;
	JButton send;
	JButton open_file;
	JLabel choosed_file;
	boolean file_chosed;

	public void init() {

		setLayout(null);

		record = new JButton("Record");
		play = new JButton("Play");
		stop = new JButton("Stop");
		send = new JButton("send");
		open_file = new JButton("Parcourir");
		choosed_file = new JLabel();

		record.setBounds(70, 10, 80, 25);
		play.setBounds(155, 10, 80, 25);
		stop.setBounds(240, 10, 80, 25);
		send.setBounds(70, 50, 80, 25);
		open_file.setBounds(70, 80, 100, 25);
		choosed_file.setBounds(200, 80, 200, 25);

		add(record);
		add(play);
		add(stop);
		add(send);
		add(open_file);
		add(choosed_file);

		record.setEnabled(true);
		play.setEnabled(true);
		stop.setEnabled(true);
		send.setEnabled(true);
		open_file.setEnabled(true);

		record.addActionListener(this);
		play.addActionListener(this);
		stop.addActionListener(this);
		send.addActionListener(this);
		open_file.addActionListener(this);

	}// End of init

	// ***** ActionPerformed method for ActionListener****/

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == record) {
			record.setEnabled(false);
			stop.setEnabled(true);
			play.setEnabled(false);
			send.setEnabled(false);
			recordAudio();
		} else if (e.getSource() == play) {
			stop.setEnabled(true);
			send.setEnabled(false);
			if (first) {
				playAudio();
			}
		} else if (e.getSource() == stop) {
			record.setEnabled(true);
			stop.setEnabled(false);
			play.setEnabled(true);
			send.setEnabled(true);
			running = false;
			stopAudio();
			saveAudio();
		} else if (e.getSource() == send) {

			send.setEnabled(false);
			UploadToServer();

		} else if (e.getSource() == open_file){
			
			choose_file();
			send.setEnabled(true);
		}
	}

	// ************** Method Declarations ****/

	public void choose_file() {
		
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 		        "Fichiers Audio.", "au", "mp3", "wav", "m4a", "mp4");
		//chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("envoyer un fichier audio préenregistré");
        int returnVal = chooser.showOpenDialog(getParent());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName() ); 
           choosed_file.setText(chooser.getSelectedFile().getPath());
           System.out.println("You chose to open this file: " + chooser.getSelectedFile().getPath());
           file_chosed = true;
           System.out.println("file chosed = " + file_chosed);
        }
		
	}

	private void recordAudio() {

		first = true;
		file_chosed = false;

		try {

			final AudioFileFormat.Type fileType = AudioFileFormat.Type.AU;
			final AudioFormat format = getFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();

			Runnable runner = new Runnable() {
				int bufferSize = (int) format.getSampleRate()
						* format.getFrameSize();
				byte buffer[] = new byte[bufferSize];

				public void run() {
					out = new ByteArrayOutputStream();
					running = true;

					try {
						while (running) {

							int count = line.read(buffer, 0, buffer.length);
							if (count > 0) {

								out.write(buffer, 0, count);
								InputStream input = new ByteArrayInputStream(
										buffer);
								final AudioInputStream ais = new AudioInputStream(
										input, format, buffer.length
												/ format.getFrameSize());

							}
						}
						out.close();
					} catch (IOException e) {
						System.exit(-1);
					}
				}
			};
			Thread recordThread = new Thread(runner);
			recordThread.start();
		}

		catch (LineUnavailableException e) {
			System.err.println("Line Unavailable:" + e);
			e.printStackTrace();
			System.exit(-2);
		} catch (Exception e) {
			System.out.println("Direct Upload Error");
			e.printStackTrace();
		}

	}// End of RecordAudio method

	private void playAudio() {

		try {

			byte audio[] = out.toByteArray();
			InputStream input = new ByteArrayInputStream(audio);
			final AudioFormat format = getFormat();
			final AudioInputStream ais = new AudioInputStream(input, format,
					audio.length / format.getFrameSize());
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			sline = (SourceDataLine) AudioSystem.getLine(info);
			sline.open(format);
			sline.start();
			Float audioLen = (audio.length / format.getFrameSize())
					* format.getFrameRate();

			Runnable runner = new Runnable() {
				int bufferSize = (int) format.getSampleRate()
						* format.getFrameSize();
				byte buffer[] = new byte[bufferSize];

				public void run() {

					try {

						int count;
						synchronized (lock) {
							while ((count = ais.read(buffer, 0, buffer.length)) != -1) {

								while (paused) {

									if (sline.isRunning()) {

										sline.stop();
									}
									try {

										lock.wait();
									} catch (InterruptedException e) {
									}
								}
								if (!sline.isRunning()) {

									sline.start();
								}
								if (count > 0) {
									sline.write(buffer, 0, count);
								}
							}
						}
						first = true;
						sline.drain();
						sline.close();
					} catch (IOException e) {
						System.err.println("I/O problems:" + e);
						System.exit(-3);
					}
				}
			};

			Thread playThread = new Thread(runner);
			playThread.start();
		} catch (LineUnavailableException e) {
			System.exit(-4);
		}

	}// End of PlayAudio method

	private void stopAudio() {

		if (sline != null) {
			sline.stop();
			sline.close();
		} else {
			line.stop();
			line.close();
		}
	}// End of StopAudio

	public void UploadToServer() {
		File file = null;
		try {
			if (file_chosed)
			{
				file = new File(choosed_file.getText());
		           System.out.println("le fichier choisi est " + choosed_file.getText()); 

			}
			else
			{
				file = new File("record.mp3");
		           System.out.println("le fichier choisi est record.mp3 "); 

			}
			

			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[in.available()];
			int bytesread = 0;

			String ServerLink = "http://localhost:80/test/";

			URL URLConnectionToUpload = new URL(ServerLink);
			URLConnection ConnectionToUpload = URLConnectionToUpload
					.openConnection();
			ConnectionToUpload.setDoInput(true);
			ConnectionToUpload.setDoOutput(true);
			ConnectionToUpload.setUseCaches(false);
			ConnectionToUpload.setDefaultUseCaches(false);

			DataOutputStream out = new DataOutputStream(
					ConnectionToUpload.getOutputStream());

			while ((bytesread = in.read(buf)) > -1) {
				out.write(buf, 0, bytesread);
			}

			out.flush();
			out.close();
			in.close();

			DataInputStream inputFromClient = new DataInputStream(
					ConnectionToUpload.getInputStream());
			// get what you want from servlet
			// .......
			inputFromClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void ToMP3() {

		IMediaReader reader = ToolFactory.makeReader("record.wav");
		reader.addListener(ToolFactory.makeWriter("record.mp3", reader));

		while (reader.readPacket() == null)
			;

		File file = new File("record.wav");
		file.delete();
	}

	private void saveAudio() {

		Thread thread = new saveThread();
		thread.start();

	} // End of saveAudio

	private AudioFormat getFormat() {

		Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float sampleRate = 44100.0F;
		int sampleSizeInBits = 16;
		int channels = 2;
		int frameSize = 4;
		float frameRate = 44100.0F;
		boolean bigEndian = false;

		return new AudioFormat(encoding, sampleRate, sampleSizeInBits,
				channels, frameSize, frameRate, bigEndian);

	}// End of getAudioFormat

	class saveThread extends Thread {

		public void run() {

			AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
			String name = "record" + ".wav";
			File file = new File(name);

			try {

				byte audio[] = out.toByteArray();
				InputStream input = new ByteArrayInputStream(audio);
				final AudioFormat format = getFormat();
				final AudioInputStream ais = new AudioInputStream(input,
						format, audio.length / format.getFrameSize());
				AudioSystem.write(ais, fileType, file);
			} catch (Exception e) {
				e.printStackTrace();
			}

			ToMP3();

		}

	}// End of inner class saveThread

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub

	}

}// End of main
