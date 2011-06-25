#!/bin/bash
LOAD="$1"
BLOCK="$2"

MAXPARALLEL=2

DIR=$(dirname $0)

export CLASSPATH="${DIR}/target/classes/:/usr/share/java/log4j.jar:/home/user/.m2/repository/net/sf/jgap/jgap/3.4.4/jgap-3.4.4.jar:/home/user/.m2/repository/net/sf/trove4j/trove4j/2.0.2/trove4j-2.0.2.jar:/home/user/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.7.1/jackson-core-asl-1.7.1.jar:/home/user/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.7.1/jackson-mapper-asl-1.7.1.jar:/home/user/.m2/repository/org/uncommons/watchmaker/watchmaker-framework/0.7.1/watchmaker-framework-0.7.1.jar:/home/user/.m2/repository/org/uncommons/maths/uncommons-maths/1.2/uncommons-maths-1.2.jar"

JAVA_OPTS="-server -Xmx1g -XX:-UseGCOverheadLimit"

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

for oversubs in 1
do
for parallel in 1 2 3 4
do

mkdir -p "mopra_${LOAD}/${oversubs}_${parallel}/"

mark="mopra_${LOAD}/${oversubs}_${parallel}/${pop}_${crossover}_${mut}_${mutkeep}_${mutsimfw}_${mutsimbw}_${mutsimprev}_${mutex}_${mutplace}_${crossovertwo}_${crossoverdays}"

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

done
done
wait

python ${DIR}/pointcalcval.py $FILES

