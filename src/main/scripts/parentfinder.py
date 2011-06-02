"""
creates operator and initial population influence plots
"""
import json, numpy, scipy, math
import sys
import re
import copy

files = sys.argv[3:]

folder = sys.argv[1]
defconfig = sys.argv[2]
#scenarios = sys.argv[3:]

replaceMap = {
	'Scheduler':'',
	'with jobselector ':'',
	'with jobSortFunction ':' by ',
	'local\.radioschedulers\.alg\.lp\.':'',
	' instance [0-9]*':'',
	'@.*$':'',
	'JobSortCriterion':'',
	'Selector':'',
	'CPULikeScheduler':'',
	'GreedyPlacementScheduler with jobSortFunction PressureJobSortCriterion':'Greedy by pressure',
	'GreedyPlacementScheduler with jobSortFunction PriorityJobSortCriterion':'Greedy by priority',
	'GreedyScheduler':'Greedy, by shortest first',
	'  *':' ',
	'^ *':'',
}

alloperators = [
	'Crossover',
	#'CrossoverDouble',
	'Mutation',
	'MutationKeeping',
	'MutationSimilarFw',
	'MutationSimilarBw',
	'MutationSimilarPrev',
	'MutationExchange',
	'MutationJobPlacement',
]
opcolors = [
	'green', 'darkgreen', 'red', 'blue', 'blue', 'blue', 'blue', 'blue'
]
alloperators.reverse()
opcolors.reverse()

opReplaceMap = {
	'Schedule':'',
	'KeepingMutation':'MutationKeeping',
	'SimilarMutation forwards:true backwards:false':'MutationSimilarFw',
	'SimilarMutation forwards:false backwards:true':'MutationSimilarBw',
	'SimilarPrevMutation':'MutationSimilarPrev',
	'JobPlacementMutation forwards:true backwards:true':'MutationJobPlacement',
	'ExchangeMutation':'MutationExchange',
}
parents = {}
operators = {}
optotal = 91 * 24 * 4. #0000 # max(operators.values())
nfiles = len(files)
for filename in files:
	#filename = folder + "/" + s + "/" + defconfig + "_ga-final-population-similarity.json"

	c = json.load(file(filename))
	
	#popsize = (len(c['final']) - 1) / 2
	popsize = 1
	for j in c['final'][0:popsize]:
		if j == "thats it":
			continue
		for n in j['history']:
			p = n
			if p == "thats it" or p == "ScheduleFactory":
				continue
			
			for k in replaceMap:
				p = re.sub(k, replaceMap[k], p)
			
			if parents.has_key(p):
				parents[p] = parents[p] + 1. / popsize / nfiles
			else:
				parents[p] = 1. / popsize / nfiles
	for j in c['final'][0:1]:
		for n in j['operators']:
			p = n
			if j['operators'][n] != 0:
				v = 1 * j['operators'][n]
			else:
				v = 0
			
			for k in opReplaceMap:
				p = re.sub(k, opReplaceMap[k], p)
			
			v = v * 1. / nfiles / optotal

			if p == "thats it" or p == "ScheduleFactory":
				continue
			if operators.has_key(p):
				operators[p] = operators[p] + v * 1.
			else:
				operators[p] = v * 1.

parentsoutname = "parents_%s_%s.pdf" % (folder, defconfig)
opoutname      = "operators_%s_%s.pdf" % (folder, defconfig)

#parents["ParallelLinearScheduler"] = 0.5

order = copy.deepcopy(parents.keys())
order.sort()
order.reverse()
#colors = ['b'] * 4 + ['g'] * 3 + ['yellow']
#colors.reverse()
colors = ['yellow']*len(order)

names = []
values = []

for p in order:
	pname = p
	#for k in replaceMap:
	#	pname = re.sub(k, replaceMap[k], pname)
	names.append(pname)
	entries = filter(lambda x:x.startswith(p), parents.keys())
	values.append(sum(map(lambda x:parents[x], entries)))
	print "%s\t%s" % (pname, values[-1])
	#print "         ", len(entries)


opnames = []
opvalues = []

for p in alloperators:
	opnames.append(p)
	if operators.has_key(p):
		v = operators[p]
	else:
		v = 0
	opvalues.append(v)
	print "%s\t%s" % (p, v)


### plotting

import matplotlib
matplotlib.use('cairo')
import matplotlib.pyplot as plt

n = len(names)

fig = plt.figure(figsize=(3.3,1*(n*0.1+1)))

fontsize = 8

#plt.ylabel('Fitness value')
#plt.title('Algorithm output comparison')
#plt.grid(True, alpha=0.2)

pos = numpy.arange(n) + 0.5
plt.barh(pos, values, align='center', height=0.9, color=colors)
plt.xlim([0,1])
plt.ylim([0,n])
#locs, labels = plt.xticks(range(n), names, fontsize=fontsize)
plt.yticks(pos, names, fontsize=fontsize, horizontalalignment='left', x="-1.05")
plt.xticks([0, 0.5, 1], ['none', '', 'all'], fontsize=fontsize)
#locs, labels = plt.xticks(range(n), names, fontsize=fontsize)
#plt.setp(labels, 'rotation', 'vertical')
#

#for scname in allvalues:
#	plt.plot(range(n), allvalues[scname], 'o', label=scname)
#plt.xlim([-0.5,n-0.5])
#plt.legend(loc='upper left', prop=matplotlib.font_manager.FontProperties(size=fontsize-2)
#)
plt.subplots_adjust(left = 0.5)
plt.subplots_adjust(right = 0.95)
plt.subplots_adjust(bottom = 0.2)
plt.savefig(parentsoutname)

print "wrote to %s" % parentsoutname



n = len(opnames)

fig = plt.figure(figsize=(3.,1*(n*0.1+1)))

fontsize = 8

#plt.ylabel('Fitness value')
#plt.title('Algorithm output comparison')
plt.grid(True, alpha=0.2)

pos = numpy.arange(n) + 0.5
plt.barh(pos, opvalues, align='center', height=0.9)
#locs, labels = plt.xticks(range(n), names, fontsize=fontsize)
plt.yticks(pos, opnames, fontsize=fontsize, horizontalalignment='left', x="-0.697")

#ma = int(math.ceil(max(opvalues)))
plt.xticks(fontsize=fontsize)
#plt.xticks(range(0, 1000*int(ma/1000+1), 1000*int(ma/4000 + 1)), fontsize=fontsize)
#plt.xticks(range(0, 300+1, 50))
#plt.xticks([0, 1], ['none', 'all'])
plt.xlim(0)
plt.ylim(0, n)
#locs, labels = plt.xticks(range(n), names, fontsize=fontsize)
#plt.setp(labels, 'rotation', 'vertical')
#

#for scname in allvalues:
#	plt.plot(range(n), allvalues[scname], 'o', label=scname)
#plt.xlim([-0.5,n-0.5])
#plt.legend(loc='upper left', prop=matplotlib.font_manager.FontProperties(size=fontsize-2)
#)
plt.subplots_adjust(left = 0.4)
plt.subplots_adjust(right = 0.95)
plt.subplots_adjust(bottom = 0.1)
plt.subplots_adjust(top = 0.95)
plt.savefig(opoutname)

print "wrote to %s" % opoutname


