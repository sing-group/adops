/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2017 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package es.uvigo.ei.sing.adops.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.adops.configuration.Configuration;

public class SingleExperiment implements Experiment {
//	private static final Logger log = Logger.getLogger(SingleExperiment.class);

	private final File namesFile;

	private final File fastaFile;
	
//	private final File alignedFastaFile, alignedProteinFastaFile;
	
	private final File folder;
	private final File propertiesFile;
	private final Configuration configuration;
	private String notes;

	private ExperimentOutput result;
	
	private boolean running;
	
	public SingleExperiment(File folder, File propertiesFile, File fastaFile, File namesFile) throws IOException {
		super();
		this.folder = folder;
		this.propertiesFile = propertiesFile;
		
		if (!this.folder.exists() && !this.folder.mkdirs()) {
			throw new IllegalArgumentException(
				"Experiment folder (" + this.folder.getAbsolutePath() + ") could not be created"
			);
		}
		
		this.configuration = new Configuration(
			Configuration.getSystemConfiguration(), 
			this.propertiesFile
		);
		
		this.running = false;
		
		this.fastaFile = fastaFile;
		this.namesFile = namesFile;
//		this.alignedFastaFile = new File(this.folder, "aligned");
//		this.alignedProteinFastaFile = new File(this.folder, "prot.aligned");
	}

	@Override
	public Configuration getConfiguration() {
		return this.configuration;
	}

	@Override
	public File getPropertiesFile() {
		return this.propertiesFile;
	}

	@Override
	public File getFolder() {
		return this.folder;
	}

	@Override
	public File getFilesFolder() {
		return new File(this.getFolder(), "allfiles");
	}
	
	public void setResult(ExperimentOutput result) {
		this.result = result;
	}

	@Override
	public ExperimentOutput getResult() {
		return this.result;
	}

	@Override
	public boolean hasResult() {
		return this.result != null;
	}

	@Override
	public File getFastaFile() {
		return this.fastaFile;
	}

	@Override
	public File getNamesFile() {
		return this.namesFile;
	}
	
	@Override
	public Map<String,String> getNames() {
        BufferedReader br = null;
        FileReader reader = null;
        
        try {
			reader = new FileReader(this.getNamesFile());
	        br = new BufferedReader(reader);
	        
	        final Map<String,String> names = new HashMap<String,String>();
	        
	        String line = null;
			while ((line = br.readLine()) != null) {
	        	final String[] splits = line.split("-");
	        	
	        	names.put(splits[1].trim(), splits[0].trim());
	        }
	        
	        return names;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null)
				try { br.close(); }
				catch (IOException ioe) {}
			if (reader != null)
				try { reader.close(); }
				catch (IOException ioe) {}
		}
	}

//	@Override
//	public File getAlignedFastaFile() {
//		return this.alignedFastaFile;
//	}
//	
//	@Override
//	public boolean hasAlignedFastaFile() {
//		return this.alignedFastaFile.isFile();
//	}

//	@Override
//	public File getRenamedAlignedProteinFastaFile() {
//		return this.alignedProteinFastaFile;
//	}
//	
//	@Override
//	public boolean hasRenamedAlignedProteinFastaFile() {
//		return this.alignedProteinFastaFile.isFile();
//	}
//	
	@Override
	public String getNotes() {
		return this.notes;
	}

	@Override
	public void setNotes(String notes) {
		this.notes=notes;
	}

	public File getNotesFile() {
		return new File(this.folder,"notes.txt");
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}
	
	@Override
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public void delete() {
		try {
			FileUtils.deleteDirectory(folder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public File getSourceFastaFile() {
		return this.fastaFile;
	}

	@Override
	public File getSourceNamesFile() {
		return this.namesFile;
	}

	@Override
	public void generateInputFiles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteResult() {
		if (this.hasResult()) {
			this.result.delete();
			this.result = null;
		}
	}
	
	@Override
	public boolean isClean() {
		if (this.hasResult()) {
			return false;
		} else {
			final FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.equals(SingleExperiment.this.getNotesFile()) ||
						pathname.equals(SingleExperiment.this.getPropertiesFile()) ||
						pathname.equals(SingleExperiment.this.getFastaFile()) || 
						pathname.equals(SingleExperiment.this.getNamesFile())
					) {
						return false;
					} else if (pathname.equals(SingleExperiment.this.getFilesFolder())) {
						return pathname.listFiles().length != 0;
					}
					
					return true;
				}
			};
			
			return this.getFolder().listFiles(filter).length > 0;
		}
	}
	
	@Override
	public void clear() {
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.equals(SingleExperiment.this.getNotesFile()) ||
					pathname.equals(SingleExperiment.this.getPropertiesFile()) ||
					pathname.equals(SingleExperiment.this.getFastaFile()) || 
					pathname.equals(SingleExperiment.this.getNamesFile()) ||
					pathname.equals(SingleExperiment.this.getFilesFolder())
				) {
					return false;
				}
				
				return true;
			}
		};
		
		for (File file : this.getFolder().listFiles(filter)) {
			if (file.isFile()) file.delete();
			else if (file.isDirectory())
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {}
		}
		
		try {
			FileUtils.cleanDirectory(this.getFilesFolder());
		} catch (IOException e) {}
		
		this.result = null;
	}

	public String getMrBayesVersion() throws IOException {
		final String binDir = this.configuration.getMrBayesConfiguration().getDirectory();
		final String bin = this.configuration.getMrBayesConfiguration().getBinary();
		final String mpichCommand = this.configuration.getMrBayesConfiguration().getMpich();
		final String mrbayesCommand = mpichCommand + " " + binDir + bin + " random_file";
		

		Process process = Runtime.getRuntime().exec(mrbayesCommand);
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);

		String version = br.readLine();
		
		return version.trim();
	}

	public String getCodeMLVersion() throws IOException {
		File temp = File.createTempFile("codeml", ".ctl");
		
		final String binDir = this.configuration.getCodeMLConfiguration().getDirectory();
		final String bin = this.configuration.getCodeMLConfiguration().getBinary();
		final String codemlCommand = binDir + File.separator + bin + " " + temp.getAbsolutePath();

		Process process = Runtime.getRuntime().exec(codemlCommand);
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);

		String version = br.readLine();
		
		temp.delete();
		
		return version.trim();		
	}

	@Override
	public String getWarnings() {
		// TODO Auto-generated method stub
		return null;
	}
}
