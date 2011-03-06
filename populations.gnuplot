cd "testoutput"
set terminal png
set output "durations.png"
plot "histdurations.txt" w histep, "histdurationsgen.txt" w histep
set output "durationsw.png"
plot "histdurationsw.txt" w histep, "histdurationswgen.txt" w histep

set output "hours.png"
set logscale x
plot "histhours.txt" w histep, "histhoursgen.txt" w histep
unset logscale

set output "antennas.png"
set logscale y
plot "histantennasw.txt" w histep, "histantennaswgen.txt" w histep
set output "antennasw.png"
plot "histantennas.txt" w histep, "histantennasgen.txt" w histep
