
echo +++ summarizequality
python ../summarizequality.py || exit 1

configs=$(head -n3 todo_both)

files=""
for c in $configs
do 
	for l in true false
	do 
		d=results_${l}_false
		files="${files} ${d}/%s/${c}"
		echo +++ parentfinder $c $l
		python ../parentfinder.py "$d" "$c" $d/*/${c}_ga-final-population-similarity.json || exit 1
	done
done

> complexity.txt
for i in {1,2,3,4}00.0; do 
	for p in 1 2 3 4; do 
		echo $(jruby ../complexity.rb $i $p 2>/dev/null | grep '^(#| INFO)' -Ev) $(bash ../extractcomplexity.sh "alg_${i}_${p}.log") >> complexity.txt
		echo +++ schedulesList $i $p
		jruby ../schedulesList.rb "$i" "$p"  || exit 1
	done
done 

for i in {1,2,4}00.0
do 
	for p in 1 2 4
	do 
#		[ "${i}_${p}" == "400.0_4" ] && continue
		echo +++ qualitydom $i $p
		python ../qualitydom.py "$i" "$p"  || exit 1
		echo +++ summarizequalitydev $i $p
		python ../summarizequalitydev.py $files -- "${i}_${p}" || exit 1
	done
done

python ../runtime.py
python ../proposaldemand.py

