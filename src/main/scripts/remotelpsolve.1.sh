if [ $RANDOM -gt 16300 ]; then
REMOTE=jbuchner@ska-blade3.aut.ac.nz
else
REMOTE=jbuchner@ska-blade4.aut.ac.nz
fi
REMOTE=jbuchner@ska-blade3.aut.ac.nz

NAME=$(md5sum < $1|awk '{print $1}')
echo "using $REMOTE to calculate $1 (hash $NAME)" >>/tmp/remotelpsolve.log
{
gzip < $1 | ssh ${REMOTE} "cat > ${NAME}.gz" &&
ssh ${REMOTE} "bash runlp.sh $NAME" &&
ssh ${REMOTE} "gunzip < ${NAME}.out.gz" > $1.out
} 2>&1 >>/tmp/remotelpsolve.log
#cat $1.out
