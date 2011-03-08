cd "testoutput"

set mytics 4
set mxtics 4
#set terminal pslatex style eps
set term post eps enhanced color
set size 0.75,0.75

set xlabel "duration"
set ylabel "percentage"
set title "Durations"
set output "durations.eps"
plot "histdurations.txt" w histep t "ATA", "histdurationsgen.txt" w histep t "model"

set output "durationsw.eps"
set title "Durations, weighted by total hours"
plot "histdurationsw.txt" w histep t "ATA", "histdurationswgen.txt" w histep t "model"

set output "hours.eps"
set title "Total hours"
set xlabel "total hours"
set logscale x
plot "histhours.txt" w histep t "ATA", "histhoursgen.txt" w histep t "model"

set output "antennas.eps"
unset logscale
set title "Number of antennas"
set xlabel "number of antennas"
set logscale y
plot "histantennasw.txt" w histep t "ATA", "histantennaswgen.txt" w histep t "model"
set output "antennasw.eps"
set title "Number of antennas, weighted by total hours"
plot "histantennas.txt" w histep t "ATA", "histantennasgen.txt" w histep t "model"
