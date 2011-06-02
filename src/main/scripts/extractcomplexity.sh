LOG=$1
NJOBS=$(grep -Eo ' found all jobs: [0-9]*' $LOG|sed 's, found all jobs: ,,g')
NCOMBO=$(grep -Eo ' got FRcompatibleJobFactory with [0-9]*' $LOG|sed 's, got FRcompatibleJobFactory with ,,g')

echo $NJOBS $NCOMBO

