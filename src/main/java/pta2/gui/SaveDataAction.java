/**
 * 
 */
package pta2.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JTable;

import ij.IJ;
import ij.ImagePlus;
import ij.io.SaveDialog;
import pta2.data.AnalyzeTrack;
import pta2.data.TrackPoint;

/**
 * This class implements save menu for ResultDataTable
 * @author araiyoshiyuki
 *
 */
public class SaveDataAction extends AbstractAction {

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	
	private ImagePlus imp;
	private ResultDataTable rdt;
	private boolean batchmode;
	private String parentloc;
	private List<List<TrackPoint>> tracklist;
	static Color[] comboColors = {Color.cyan,Color.blue,Color.red,Color.yellow,Color.green,Color.magenta,Color.orange,Color.white};
	static String[] cString = {"Cyan", "Blue", "Red", "Yellow", "Green", "Magenta", "Orange", "White"};

	public SaveDataAction(ImagePlus imp, ResultDataTable rdt) {
		this.imp = imp;
		this.rdt = rdt;
		this.tracklist = rdt.tracklist;
	}
	
	public SaveDataAction(ImagePlus imp, List<List<TrackPoint>> tracklist, boolean batchmode, String parentloc) {
		this.imp = imp;
		this.tracklist = tracklist;
		this.batchmode = batchmode;
		this.parentloc = parentloc;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		AbstractButton b = (AbstractButton)e.getSource();

		if(b.getText() == "Save all") 
			saveAll();
		else if(b.getText() == "Save selected") 
			saveSelected();			
		else if(b.getText() == "Save checked") 
			saveChecked();
		else if(b.getText() == "Save Table as Text Data") 
			saveTableText();
	}
		
	public void saveAll() {
		String parent = "", fn = null;
		if(!batchmode) {
			SaveDialog sd = new SaveDialog("Save all",imp.getShortTitle()+"FIAllPoints",".txt");
			parent = sd.getDirectory();
			fn = sd.getFileName();
		} else {
			parent = parentloc;
			fn = imp.getShortTitle()+"FIAllPoints.txt";
			IJ.log(parent + fn);
		}
		
		File file = new File(parent, fn);
		try {
			file.createNewFile();
		} catch (IOException e) {
			IJ.log(e.toString());
		}
		if(fn != null) {
			try {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				List<TrackPoint> tmpPlist;
				pw.println("Point Frame x y sx sy intensity integ.int offset area itteration velocity msd cost");
				if(!batchmode) {
					JTable jt = rdt.jt;				
					for(int index = 0; index < tracklist.size(); index++) {
						tmpPlist = tracklist.get(jt.convertRowIndexToModel(index));
						writePointData(tracklist, tmpPlist, pw);
					}
				} else {
					// for batch mode
					for(int index = 0; index < tracklist.size(); index++) {
						tmpPlist = tracklist.get(index);
						writePointData(tracklist, tmpPlist, pw);
					}
				}
				pw.close();
			} catch (IOException e1) {
				IJ.log(e1.toString());
			}
		}			
	}
	public void saveSelected() {
		SaveDialog sd = new SaveDialog("Save selected points",imp.getShortTitle()+"FISelectedPoints",".txt");
		File file = new File(sd.getDirectory(),sd.getFileName());
		if(sd.getFileName()!=null) {
			try {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				List<TrackPoint> tmpPlist;
				int[] selectedlist = rdt.selectedlist;

				pw.println("Point Frame x y sx sy intensity integ.int offset area itteration velocity msd cost");
				for(int index = 0; index < selectedlist.length; index++) {
					tmpPlist = tracklist.get(selectedlist[index]);
					writePointData(tracklist, tmpPlist, pw);
				}
				pw.close();
			} catch (IOException e1) {
				IJ.log(e1.toString());
			}
		}			
	}
	public void saveChecked() {
		SaveDialog sd = new SaveDialog("Save checked points",imp.getShortTitle()+"FICheckedPoints",".txt");
		File file = new File(sd.getDirectory(),sd.getFileName());
		if(sd.getFileName()!=null) {
			try {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				List<TrackPoint> tmpPlist;
				JTable jt = rdt.jt;
				
				pw.println("Point Frame x y sx sy intensity integ.int offset area itteration velocity msd cost");

				for(int index = 0; index < tracklist.size(); index++) {
					if((Boolean) jt.getValueAt(index, 7) == Boolean.TRUE) {
						IJ.log("checked data index is = "+jt.convertRowIndexToModel(index));
						tmpPlist = tracklist.get(jt.convertRowIndexToModel(index));
						writePointData(tracklist, tmpPlist, pw);
					}
				}
				IJ.log("pw = " + pw.toString());
				pw.close();
			} catch (IOException e1) {
				IJ.log(e1.toString());
			}
		}			
	}
	public void saveTableText() {
		SaveDialog sd = new SaveDialog("Save Table Data",imp.getShortTitle()+"TableData",".txt");
		File file = new File(sd.getDirectory(), sd.getFileName());
		if(sd.getFileName() != null) {
			try {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				pw.println("# From To FrameLength Ave.Intensity Ave.Velocity RunLength Check Color");
				JTable jt = rdt.jt;

				for(int row=0; row < jt.getRowCount(); row++) {
					for(int column = 0; column < jt.getColumnCount(); column++) {
						if(column != 8)
							pw.print(jt.getValueAt(row, column));
						else {
							int i = 0;
							for(Color cs:comboColors) {
								String s = cs.toString();
								if(s.equals(jt.getValueAt(row, column).toString()))
									break;
								i++;
							}
							pw.print(cString[i]);
						}
						pw.print(" ");
					}
					pw.println();
				}
				IJ.log(pw.toString());
				pw.close();
			} catch (IOException e1) {
				IJ.log(e1.toString());
			}
		}			
	}
	/*
	 * write Point data
	 */
	private void writePointData(List<List<TrackPoint>> plist, List<TrackPoint> tmpPlist, 
			PrintWriter pw) throws IOException {
		//pw.println(String.format("#Point:%d -- -- -- -- -- -- -- --",plist.indexOf(tmpPlist)));
		int frame = 0;
		int cnt = 0;
		int pnum = plist.indexOf(tmpPlist);
		String vel = "--", msd = "--", cost = "--";
		AnalyzeTrack at = new AnalyzeTrack(imp, tmpPlist);
		for(TrackPoint tmpP: tmpPlist) {
			if (cnt > 0) {
				vel = String.valueOf(at.velocities[cnt - 1]);
				msd = String.valueOf(at.msd[cnt - 1]);
			}
			if (cnt > 1)
				cost = String.valueOf(at.cost[cnt - 2]);
			pw.println(String.format("%d %d %f %f %f %f %f %f %f %f %d %s %s %s", 
					pnum, tmpP.frame, tmpP.tx, tmpP.ty, tmpP.sx, tmpP.sy,
					tmpP.mean, tmpP.integint, tmpP.offset, tmpP.area, tmpP.ite,  
					vel, msd, cost));
			frame = tmpP.frame;
			cnt++;
		}
	}
}
