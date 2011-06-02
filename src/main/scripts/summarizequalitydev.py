"""
plots the fitness value development over the generation number for various 
runs.
"""

import json, numpy, scipy
import sys
import math, re

configfolders = sys.argv[1:sys.argv.index('--', 1)]
scenarios = sys.argv[len(configfolders)+2:]

#print configfolders
#print scenarios
#print (configfolders + ["--"] + scenarios) == sys.argv[1:]

# for each prior: collect sum of values over all scenarios
# for each configfolders: collect sum of values per iteration 
#                         {'steps':[0, 30, 60], 'values':[1234, 2123, 3213]}

priors = {}
collectedrun = {}


def getConfigName(cf):
	folder, config = cf.split('/%s/', 2)
	replaceMap = {
		'results_false_false':'empty',
		'results_true_false':'filled, without LP solution',
		'results_truelin_false':'filled, elite=2',
		'results_noelite_true_false':'filled, no elite',
	}
	folder = replaceMap[folder]
	if folder == 'filled, elite=2':
		config = "(any)"
	return folder + " \t " + config.replace('_', ' ')
	

for cf in configfolders:
	sums = {}
	
	for scenario in scenarios:
		filename = cf % (scenario) + "_ga-final-population-similarity.json"
		c = json.load(file(filename))

		replaceMap = {
			'with jobselector ':'',
			'with jobSortFunction ':'',
			'local\.radioschedulers\.lp\.':'',
			' instance [0-9]*':'',
			'@.*$':'',
			'JobSortCriterion':'',
		}
		
		if not priors.has_key(scenario):
			priors[scenario] = {}
		# now we load all priors
		for k in c['initial']:
			if k == "thats it":
				continue
			w = k
			for r in replaceMap:
				w = re.sub(r, replaceMap[r], w)
			priors[scenario][w] = c['initial'][k]
			#print scenario, w, c['initial'][k]
		# ok, done with that.

		f = file(cf % (scenario) + "_ga-settings.txt")
		settings = dict([l.split(": ", 2) for l in f.readlines()[0:10]])


		# on to the population development.
		filename = cf % (scenario) + "_ga-population-development.txt"
		f = file(filename)
		
		index = []
		best = []
		mean = []
		variance = []
		while True:
			l = f.readline()
			if l == "":
				break
			(i, v) = l.split()
			index.append(int(i) * int(settings['populationSize']))
			best.append(float(v))
			# a = mean + stdev
			(i, a) = f.readline().split()
			# b = mean - 3 * stdev
			(i, b) = f.readline().split()
			a, b = float(a), float(b)
			m = (3*a + b) / 4
			s = a - m
			mean.append(m)
			variance.append(s**2)
		
		if not sums.has_key('mean'):
			sums['index'] = index
			sums['best'] = best
			sums['mean'] = mean
			sums['variance'] = variance
		else:
			# sums['index'] -- nothing with those
			print sums['best'], best
			sums['best']  = map(lambda a,b: a+b, sums['best'], best)
			sums['mean']  = map(lambda a,b: a+b, sums['mean'], mean)
			#sums['variance'] = map(lambda a,b: a**2+b**2, sums['variance'], mean)
			sums['variance'] = map(lambda a,b: max(a,b), sums['variance'], variance)

	# have development in sums now.
	collectedrun[cf] = sums
	collectedrun[cf]['stdev'] = map(lambda x:math.sqrt(x), collectedrun[cf]['variance'])


priors2 = {}
for sc in scenarios:
	for alg in priors[sc]:
		if priors2.has_key(alg):
			priors2[alg] = priors[sc][alg] + priors2[alg]
		else:
			priors2[alg] = priors[sc][alg]
priors = priors2


# ok. 

# now we want a plot
# left side: priors (optional)
# for every configuration, plot the best 
# for every configuration, plot the mean with errorbars, dim

import matplotlib
matplotlib.use('cairo')
import matplotlib.pyplot as plt

fig = plt.figure(figsize=(13,8))
plt.ylabel('Fitness value')
plt.xlabel('Iteration * population size')
#plt.title('GA population development')
plt.grid(True)

plt.plot([-30] * len(priors), priors.values(), 'sb') #, label="heuristics, LP, greedy algorithms")
i = 0
m = max(priors.values())
plt.annotate('Heuristics', (-30, m), (-30, m+0.05), 
	va='bottom', ha='center', color='blue', rotation=90,
	arrowprops={'color':"blue", 'width':1, 'headwidth':2, 'shrink':0.2, 'alpha':0.5}
)
plt.plot([-30, 10000], [m]*2, '--')

"""
revpriors = priors.keys()
revpriors.reverse()
for p in revpriors:
	if "CPU" in p:
		continue
	plt.annotate(p, (-0.030, priors[p]), xytext=(-0.070, priors[p]), 
		arrowprops={'color':"black", 'width':1, 'headwidth':3, 'shrink':0.00, 'alpha':0.1}, 
		ha="right", fontsize=11, rotation=65, va="top"
	)
	i = i + 1
"""


marker = ['--x','--+'] + ['s:','d-'] + ['^-','v:']
colors = ['c', 'c', 'm', 'm', 'r', 'r', 'y', 'k', 'lightgreen', 'lightblue']
i = 0
for cf in configfolders:
	cfname = getConfigName(cf)
	print cf
	#color = (i / 25., (i + 10) / 25., 0)
	#ecolor = ((i + 10) / 25., (i + 10) / 25., 0)
	
	#plt.errorbar(collectedrun[cf]['index'], collectedrun[cf]['mean'], yerr=collectedrun[cf]['stdev'], fmt='-', alpha=0.1, label=cfname)
	errors = numpy.array([collectedrun[cf]['stdev'], [0]*len(collectedrun[cf]['stdev'])])
	plt.errorbar(collectedrun[cf]['index'], collectedrun[cf]['best'], 
		yerr=errors, fmt=marker[i], capsize=2, label=cfname, color=colors[i])
	#errors = numpy.array([collectedrun[cf]['stdev'], numpy.array(collectedrun[cf]['best']) - numpy.array(collectedrun[cf]['mean'])])
	#plt.errorbar(collectedrun[cf]['index'], collectedrun[cf]['mean'], yerr=errors, uplims=True, \
	#	fmt='x-', label=cfname, ecolor=colors[i], color=colors[i], alpha=0.2)
	i = i + 1
	
plt.ylim(0, 1.3)
plt.legend(loc='upper right', prop=matplotlib.font_manager.FontProperties(size=11) )
#plt.subplots_adjust(bottom = 0.4)
plt.xticks(range(0, 1000+1, 200))
plt.yticks(numpy.arange(0, 1.01, 0.1))
plt.xlim([-60, 1000])
plt.savefig('ga_development_%s.pdf' % ','.join(scenarios))



