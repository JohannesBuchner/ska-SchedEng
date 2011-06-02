HASHDIR=/tmp/bladesoln/by-hash/

if [ -s $1.out ]; then
	echo "$1.out already there!! exiting."
	exit 1
fi
HASH=$(bash ~/hashlp.sh $1)
echo -n "hash ${HASH} -- duration "
cat $HASHDIR/${HASH}.time
python ~/convertlp.py $HASHDIR/${HASH} $1 $HASHDIR/${HASH}.out $1.out


