"""
plots fitness comparison of GA empty next to heuristics to algorithm_quality.pdf
"""

import json, numpy, scipy
import copy
import sys

defconfig = '50_0.1_0.1_0_0_0_0_0_0_0_0'
#defconfig = '50_0.100_0.100_0.000_0.000_0.000_0.000_0.000'
#bestconfig = '33_0.273_0.008_0.000_0.151_0.186_0.134_0.000'
bestconfig = '41_0_0_0.019_0.012_0.02_0.048_0.019_0.031_0.1_1'
bestfilledconfig = '61_0_0_0_0_0_0_0_0.006_0.099_15'

def getData(scenario):
	filled   = 'results_true_false/'  + scenario + "/" + defconfig + "_ga-final-population-similarity.json"
	unfilled = 'results_false_false/' + scenario + "/" + defconfig + "_ga-final-population-similarity.json"
	unfilled_best = 'results_false_false/' + scenario + "/" + bestconfig + "_ga-final-population-similarity.json"
	filled_best   = 'results_true_false/'  + scenario + "/" + bestfilledconfig + "_ga-final-population-similarity.json"

	c = json.load(file(filled))
	c_unfilled = json.load(file(unfilled))
	c_best = json.load(file(filled_best))
	c_best_unfilled = json.load(file(unfilled_best))

	order = c['initial'].keys()
	order.remove('thats it')
	order.sort()
	replaceMap = {
		'with jobselector ':'',
		'with jobSortFunction ':' by ',
		'local\.radioschedulers\.alg.lp\.':'',
		' instance [0-9]*':'',
		'@.*$':'',
		'JobSortCriterion':'',
		'Selector':'',
		'Scheduler':'',
		'GreedyPlacementScheduler with jobSortFunction PressureJobSortCriterion':'Greedy by pressure',
		'GreedyPlacementScheduler with jobSortFunction PriorityJobSortCriterion':'Greedy by priority',
		'GreedyScheduler':'Greedy, by shortest first',
		'  *':' ',
	}

	alg = []
	values = []

	for i in order:
		for k in c['initial']:
			if k.startswith(i):
				alg.append(k)
				values.append(c['initial'][k])

	alg.append(u'GA (default, empty)')
	values.append(c_unfilled['final'][0]['value'])
	alg.append(u'GA (default, filled)')
	values.append(c['final'][0]['value'])
	alg.append(u'GA (optimized, empty)')
	values.append(c_best_unfilled['final'][0]['value'])
	alg.append(u'GA (optimized, filled)')
	values.append(c_best['final'][0]['value'])

	import re
	newnames = []
	for v in alg:
		#print v
		w = v
		for k in replaceMap:
			w = re.sub(k, replaceMap[k], w)
		newnames.append(w)
	newnames.reverse()
	values.reverse()
	return (newnames, values)

scenarios_keys = []
scenarios = {}

oversubs_file    = ['100.0', '200.0', '400.0']
oversubs_percent = ['100', '200', '400']
#styles1 = ['b','g','y','r']
#styles2 = ['o','x','<','s']
stylesm = [
	'v','^','<','>', # 1
	'x','_','|','+'  # 2
	'1','2','3','4', # 3
	's','p','*','D', # 4
]
styles = {}

for oi in range(len(oversubs_percent)):
	for p in [1,2,4]:
		#if p == 4 and oi == 2:
		#	continue
		k = '%s%% %d' % (oversubs_percent[oi],p)
		scenarios_keys.append(k)
		scenarios[k] = '%s_%d' % (oversubs_file[oi], p)
		#styles[k] = styles1[oi]+styles2[p - 1]
		styles[k] = stylesm[oi + (p-1)*4] + 'b'


names = []
allvalues = {}
positions = {}
allnames = {}
n = 0
for scname in scenarios_keys:
	names, values = getData(scenarios[scname])
	n = max(len(names), n)
	for name in names:
		if not positions.has_key(name):
			positions[name] = len(positions)
	allnames[scname] = names
	allvalues[scname] = values

## sort positions by max, min value.
order = copy.deepcopy(positions.keys())
def overall(name):
	m = []
	for scname in scenarios_keys:
		if name not in allnames[scname]:
			continue
		i = allnames[scname].index(name)
		m.append(allvalues[scname][i])
	return m
def overallsort(x,y):
	xv = overall(x)
	yv = overall(y)
	if max(xv) > max(yv):
		return 1
	if max(xv) < max(yv):
		return -1
	if min(xv) < min(yv):
		return 1
	if min(xv) > min(yv):
		return -1

order.sort(cmp=overallsort)
for i in range(len(order)):
	positions[order[i]] = i

### plotting

import matplotlib
matplotlib.use('cairo')
import matplotlib.pyplot as plt

fig = plt.figure(figsize=(10,8))

fontsize = 11

plt.xlabel('Fitness value')
plt.title('Algorithm output comparison')
plt.grid(True)

"""
pos = numpy.arange(n) + 0.5
plt.barh(pos, values, align='center', height=0.2)
"""
locs, labels = plt.yticks(positions.values(), positions.keys(), fontsize=fontsize)
for i in range(len(positions.keys())):
	if "GA" in positions.keys()[i] or "Linear" in positions.keys()[i]:
		plt.setp(labels[i], 'weight', 'bold')
#plt.setp(labels, 'ha', 'center')
#plt.setp(labels, 'va', 'top')
#plt.yticks(pos, newnames)

for scname in scenarios_keys:
	plt.plot(
		allvalues[scname], 
		map(lambda x: positions[x], allnames[scname]),
		styles[scname], label=scname
	)
plt.ylim([-0.5,n-0.5])
plt.xlim(0, 1)
plt.xticks(numpy.arange(0, 1.01, 0.1))
plt.legend(loc='upper left', prop=matplotlib.font_manager.FontProperties(size=fontsize - 2))
plt.subplots_adjust(left = 0.45)
plt.subplots_adjust(right = 0.95)
plt.subplots_adjust(bottom = 0.05)
plt.subplots_adjust(top = 0.95)
plt.savefig('algorithm_quality.pdf')



