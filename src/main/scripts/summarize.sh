d=$1

pushd $(dirname $0) >/dev/null
SCRIPTDIR=$PWD
popd >/dev/null

EMPTY=""

for i in $d/*/
do
	pushd $i
	[ -a scores.txt ] || 
	for j in *.log
	do 
		f=$(echo $j|sed s,.log,,g)
		if ! test -s "${f}_ga-population-development.txt"; then
			EMPTY="$EMPTY ${i}${j}"
			echo "${i}${j} is useless"
			continue
		fi
		echo -n "$f "
		python $SCRIPTDIR/pointcalcval.py ${f}_ga-population-development.txt | tail -n1
	done | 
	sort -rnk2 > scores.txt
	popd >/dev/null
done

if [ -n "$EMPTY" ]; then
	echo "these log files have no data:"
	echo $EMPTY
fi

echo name value n sum max median scores... 
ls $d/*/*.log | 
grep -Eo '[^/]*$' | 
sed s,.log,,g|
sort -u |
while read i; do 
	#echo $i >&2
	echo -n $i $(python ${SCRIPTDIR}/pointcalcval.py $d/*/${i}_ga-population-development.txt|tail -n1) ""
	for j in $d/*/; do
		grep -w "$i" -n $j/scores.txt | cut -f1 -d:
	done|python ~/max.py|xargs echo -n
	for j in $d/*/; do 
		{ grep -w "$i" -n $j/scores.txt || echo --:; } |
		cut -f1 -d:
	done|xargs echo ""
done | sort -nk5 | tee $d/scores.txt | 
awk '{if($3>5) print $0;}'|head 

