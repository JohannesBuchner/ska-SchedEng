
import re
import sys
import numpy
import scipy

visible_algorithms = [
	'SerialLeastChoice, EarliestDeadline',
	'SerialLeastChoice, KeepingPrioritized',
	'SerialLeastChoice, Prioritized',
	'SerialListing, KeepingPrioritized',
	'SerialListing, Prioritized',
	'TrivialFirstParallelListing, Priority',
	'ContinuousUnlessOneChoice, Prioritized',
	'GreedyPressure',
#	'ParallelLinear', 0.5..3days
]
symbols = [ 'b^-', 'gs-', 'ro-', 'cs-', 'mo-', 'yx-', 'k*-', 'w.-', 'rd' ]

replaceMap = {
	' with jobselector ':', ',
	' with jobSortFunction ':', ',
	'local\.radioschedulers\.alg\.lp\.':'',
	' instance [0-9]*':'',
	'@.*$':'',
	'JobSortCriterion':'',
	'Scheduler':'',
	'Selector':'',
}

def rename(w):
	for r in replaceMap:
		w = re.sub(r, replaceMap[r], w)	
	return w

def floatify(v):
	if v == '':
		return numpy.nan
	try:
		return float(v)
	except Exception:
		return str.strip(v)

#values = [[floatify(v) for v in line.split("\t")] for line in f.readlines()]

x = []
xlabels = []
complexity = numpy.loadtxt("complexity.txt")
for v in complexity:
	xlabels.append("%.0f%% %.0f" % (v[0], v[1]))
	#x.append(v[5])
	x.append(len(x))

algorithms = {}
for oversubs in [100.0, 200.0, 300.0, 400.0]:
	for parallel in [1,2,3,4]:
		name = "%.0f%% %d" % (oversubs, parallel)
		try:
			f = file("heuristics_stats_%.1f_%d.tsv" % (oversubs, parallel), "r")
		except:
			if name in xlabels:
				i = xlabels.index(name)
				xlabels.remove(name)
				x.remove(x[i])
				print "skipping ", name, " (no heuristics_stats file)"
			continue
		for l in f.readlines():
			parts = l.split("\t")
			k = rename(parts[0])
			v = parts[1]
			if v == "":
				v = None
			else:
				v = float(v)
			if not algorithms.has_key(k):
				algorithms[k] = {}
				
			algorithms[k][name] = v


xunsorted = x
x.sort()
order = []
for i in x:
	order.append(xunsorted.index(i))

import matplotlib
matplotlib.use('cairo')
import matplotlib.pyplot as plt

def ifThereOrNone(dic, k):
	if dic.has_key(k):
		return dic[k]
	else:
		return numpy.nan

for j in range(len(visible_algorithms)):
	a = visible_algorithms[j]
	if not algorithms.has_key(a):
		print "no data on %s" % a
		continue
	y = map(lambda i: ifThereOrNone(algorithms[a],xlabels[i]), order)
	#print a, x, y
	plt.plot(x, y, symbols[j], label=a)

#plt.plot(ga[:,2], ga[:,1], "yo", label="GA survivors")

#plt.ylim(0.0, 1)
#plt.xlim(0.0, 1)
plt.legend(loc='upper left', prop=matplotlib.font_manager.FontProperties(size=11) )
plt.subplots_adjust(bottom = 0.2)
plt.xticks(x, xlabels, rotation=90)
plt.ylabel("execution time [s]")
plt.xlabel("# of job combinations")
#plt.xlim([-301, 1000])
plt.savefig('runtime.pdf')

