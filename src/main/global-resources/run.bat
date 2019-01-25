@REM ADOPS
@REM %%
@REM Copyright (C) 2012 - 2019 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
@REM %%
@REM This program is free software: you can redistribute it and/or modify
@REM it under the terms of the GNU General Public License as
@REM published by the Free Software Foundation, either version 3 of the
@REM License, or (at your option) any later version.
@REM 
@REM This program is distributed in the hope that it will be useful,
@REM but WITHOUT ANY WARRANTY; without even the implied warranty of
@REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
@REM GNU General Public License for more details.
@REM 
@REM You should have received a copy of the GNU General Public
@REM License along with this program.  If not, see
@REM <http://www.gnu.org/licenses/gpl-3.0.html>.

@echo off
start javaw -Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel -jar lib\aibench-aibench-${aibench.version}.jar
