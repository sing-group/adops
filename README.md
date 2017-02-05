# ADOPS

ADOPS (Automatic Detection Of Positively Selected Sites) software was developed with the goal of providing an automatic and flexible tool for detecting positively selected sites given a set of unaligned nucleotide sequence data.

## Team
[Molecular Evolution Group](http://evolution.ibmc.up.pt/) - [IBMC](https://www.ibmc.up.pt/):
  * **Jorge Vieira**
  * Cristina P. Vieira
  * Nuno A. Fonseca

[SING Group](http://sing-group.org/) - [Universidade de Vigo](http://www.uvigo.gal/):
  * **Miguel Reboiro-Jato**
  * David Reboiro Jato
  * Florentino Fdez-Riverola
  * Noé Vázquez González
  * Hugo López-Fernández

## Downloading and Installing ADOPS
### 0. Prerequisites
ADOPS needs T-Coffee, MrBayes and CodeML applications in order to work, so you will have to install them before start with this installation process.

You can find the installation files and instructions of this programs in their respective web pages:

  * [T-Coffee Home Page](http://www.tcoffee.org/Projects_home_page/t_coffee_home_page.html)
  * [MrBayes Home Page](http://mrbayes.sourceforge.net/)
  * [PAML Home Page](http://abacus.gene.ucl.ac.uk/software/paml.html) (includes CodeML)

### 1. Download
Download your ADOPS copy from [here](http://static.sing-group.org/ADOPS/ADOPS_v0.6.0.zip) (8.0MB) and go to step 2.
Or you can download [this](http://static.sing-group.org/ADOPS/BDBM_v0.19.2-ADOPS_v0.6.0.zip) Ubuntu 12.04 (32 bits) Virtual Machine (~1GB) with a copy of ADOPS, T-Coffee, MrBayes and PAML already installed and configured. Run the virtual machine (you will need [Virtual Box](https://www.virtualbox.org/)) and go to step 4.

If you have some trouble installing the ADOPS VM, you can check our [ADOPS VM - Quick Installation Guide](http://static.sing-group.org/ADOPS/ADOPS%20VM%20-%20Quick%20Installation%20Guide.pdf).

### 2. Decompress
Decompress the downloaded file (you will need a ZIP decompressor).

You can decompress and install ADOPS in any folder, although we recommend you to install it in a folder without spaces in its path due to some problems with the programs used by ADOPS. For example:

Once decompressed you should have a directory structure like seen in Figure 1.

You can decompress and install ADOPS in any folder, although we recommend you to install it in a folder without spaces in its path due to some problems with the programs used by ADOPS. For example:

  * /home/user/ADOPS **[OK]**
  * C:\Programs\ADOPS **[OK]**
  * /home/user/My Programs/ADOPS **[Not recommended]**
  * C:\Documents and settings\ADOPS **[Not recommended]**

### 3. Configure
Before start working with ADOPS you have to configure the paths to MrBayes and CodeML applications.

Open the system.conf configuration file and set the appropriate value to the following properties:

  * **tcoffee.bin**: T-Coffee command. T-Coffee must be in the system path<sup>1</sup>
  * **mrbayes.dir**: Path to the MrBayes installation directory. Independently of your OS use / as path separator (e.g. `C:/Users/john/mrbayes/`) **[Required]**
  * **mrbayes.bin**: Name of the MrBayes executable file
  * **codeml.dir**: Path to the CodeML installation directory. Independently of your OS use / as path separator (e.g. `C:/Users/john/paml/`) **[Required]**
  * **codeml.bin**: Name of the CodeML executable file **[Required]**

System configuration file should look as follows:

```
input.sequences=
input.fasta=
input.names=

tcoffee.bin=<T-Coffee command>
tcoffee.params=
tcoffee.maxSeqs=3
tcoffee.alignMethod=CLUSTALW2

mrbayes.mpich=
mrbayes.dir=<Path to MrBayes>
mrbayes.bin=<MrBayes executable>
mrbayes.params=
mrbayes.ngen=500000
mrbayes.tburnin=1250
mrbayes.pburnin=1250

codeml.dir=<Path to CodeML>
codeml.bin=<CodeML executable>
codeml.params=
codeml.models=0 1 2 3 7 8
```

### 4. Launch!
Now you are ready to launch ADOPS.

In case you are under a Windows environment you have to execute the `run.bat` file. Otherwise, if you are under a Linux environment you have to execute the `run.sh` file.

If you want to test the application you can download this example files:

  * [Example fasta file](http://static.sing-group.org/ADOPS/input.fasta)
  * [Coffea fasta file](http://static.sing-group.org/ADOPS/Coffea.fasta)<sup>2</sup>

## Citing ADOPS
If you use ADOPS, please, cite this work:

D. Reboiro-Jato, M. Reboiro-Jato, F. Fdez-Riverola, C.P. Vieira, N.A. Fonseca, J. Vieira
[ADOPS - Automatic Detection Of Positively Selected Sites](http://journal.imbio.de/index.php?paper_id=200)
Journal of Integrative Bioinformatics, 9(3):200, 2012.

___

<sup>1</sup> Unfortunately, there is no Windows 64 bits version of T-Coffee, so you will need a MacOS, Linux or Windows 32 bits system.

<sup>2</sup> Data set taken from Expression and Trans-Specific Polymorphism of Self-Incompatibility RNases in Coffea (Rubiaceae). Nowak, M., Davis, A., Anthony, F., Yoder, A. PloS one 6(6) (2011)
