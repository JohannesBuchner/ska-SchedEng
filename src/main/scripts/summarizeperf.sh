for i in 0.8 1.8 2.7 # 3.5
do 
	for p in 1 2 3 4
	do 
		f=alg_${i}_${p}.log
		if [ -a "${f}.corrected" ]; then
			f=alg_${i}_${p}.log.corrected
		fi
		comb=$(grep -E "got FRcompatibleJobFactory with [0-9]* combinations." $f|sed 's,.* \([0-9]*\) combinations.*,\1,g')
		grep -E 'scheduling (using .*|done)' $f | 
		sed 's,DEBUG *\([^ ]*\) .*scheduling [^ ]* *\(.*\),\1 \2,g'|
		while read t0 alg; do
			read t1 bla
			echo $i $p $comb $((-${t0} + ${t1})) \"$alg\"
		done
	done
done > perf.all

echo -n "plot " > perf2d.gnuplot
echo -n "splot " > perf3d.gnuplot
for i in "CPULikeScheduler" GreedyScheduler GreedyPlacementScheduler ParallelLinearScheduler; do 
	grep $i perf.all >perf.$i
	echo -n "\"perf.$i\" u 3:4, " >> perf2d.gnuplot
	echo -n "\"perf.$i\" u 1:2:4, " >> perf3d.gnuplot
done
echo 0 >> perf2d.gnuplot
echo 0 >> perf3d.gnuplot

