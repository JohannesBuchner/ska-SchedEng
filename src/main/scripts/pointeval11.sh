#!/bin/bash
LOAD="$1"
BLOCK="$2"

MAXPARALLEL=2

DIR=$(dirname $0)

. $DIR/loadclasspath.sh

JAVA_OPTS="-server -Xmx500m -XX:-UseGCOverheadLimit"

eval="java ${JAVA_OPTS} local/radioschedulers/run/EvaluateWF"

read name eq popfloat
pop=$(echo $popfloat|sed 's,\..*,,g')
read name eq crossover
read name eq mut
read name eq mutkeep
read name eq mutsimfw
read name eq mutsimbw
read name eq mutsimprev
read name eq mutex
read name eq mutplace
read name eq crossovertwo
read name eq crossoverdays
if [ "$crossovertwo" == "0" ]; then
	crossoverdays=0
fi
	

echo read params.

FILES=""
RUNNING=0

for oversubs in 100.0 200.0 400.0 # 300.0 
do
for parallel in 1 2 4 # 3 
do

if [ "$oversubs" == 400.0 ] && [ "$parallel" == 4 ]; then
        continue;
fi

mkdir -p "results_${LOAD}_${BLOCK}/${oversubs}_${parallel}/"

mark="results_${LOAD}_${BLOCK}/${oversubs}_${parallel}/${pop}_${crossover}_${mut}_${mutkeep}_${mutsimfw}_${mutsimbw}_${mutsimprev}_${mutex}_${mutplace}_${crossovertwo}_${crossoverdays}"

echo $mark

FILES="${FILES} ${mark}_ga-population-development.txt"

[ -a "${mark}_ga-final-population-similarity.json" ] && continue
if [ -e "${mark}.log" ]; then
	# if someone is already working on it, wait and skip. 
	# otherwise retry this failure
	while pgrep -f "${mark}_"; do
		sleep 10
	done
	[ -a "${mark}_ga-final-population-similarity.json" ] && continue
fi

$eval "${mark}_" "${oversubs}" "${parallel}" "${pop}" "${crossover}" "${mut}" "${mutkeep}" "${mutsimfw}" "${mutsimbw}" "${mutsimprev}" "${mutex}" "${mutplace}" "${crossovertwo}" "${crossoverdays}" ${LOAD} > ${mark}.log &
PID=$!
if [ $(jobs -p|wc -l) -ge "$MAXPARALLEL" ]; then
	wait $PID
fi

#mv ga-settings.txt ${mark}_ga-settings.txt
#mv ga-population-development.txt ${mark}_ga-population-development.txt
#mv ga-final-population-similarity.txt ${mark}_ga-final-population-similarity.txt

wait
done
done
wait

python ${DIR}/pointcalcval.py $FILES

