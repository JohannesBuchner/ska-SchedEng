#!/bin/bash

DIR=$(dirname $0)

echo +++ summarizequality
python $DIR/summarizequality.py || exit 1

configs=$(head -n3 todo_both)

files=""
for c in $configs
do 
	for d in results_false_false results_true_false_heuristicsonly results_true_false
	do 
		files="${files} ${d}/%s/${c}"
		echo +++ parentfinder $c $l
		python $DIR/parentfinder.py "$d" "$c" \
			$d/*/${c}_ga-final-population-similarity.json || exit 1
	done
done

#> complexity.txt
for i in {1,2,3,4}00.0; do 
	for p in 1 2 3 4; do 
		[ "${i}_${p}" == "400.0_4" ] && continue
#		echo $(jruby $DIR/complexity.rb $i $p 2>/dev/null | 
#		grep '^(#| INFO)' -Ev) $(bash $DIR/extractcomplexity.sh "alg_${i}_${p}.log") >> complexity.txt
		echo +++ schedulesList $i $p
#		jruby $DIR/schedulesList.rb "$i" "$p"  || exit 1
	done
done 

for i in {1,2,4}00.0
do 
	for p in 1 2 4
	do 
		[ "${i}_${p}" == "400.0_4" ] && continue
		echo +++ qualitydom $i $p
#		python $DIR/qualitydom.py "$i" "$p"  || exit 1
		echo +++ summarizequalitydev $i $p
		python $DIR/summarizequalitydev.py $files -- "${i}_${p}" || exit 1
	done
done

python $DIR/runtime.py
python $DIR/proposaldemand.py

