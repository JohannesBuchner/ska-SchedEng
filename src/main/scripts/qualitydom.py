
import re
import sys
import numpy


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

oversubs = sys.argv[1]
parallel = sys.argv[2]

#f = file(, "r")
#duration = [[floatify(v) for v in line.split("\t")] for line in f.readlines()]
f = file("heuristics_stats_%s_%s.tsv" % (oversubs, parallel), "r")
labels = []
values = []
duration = []

lines = f.readlines()
lines.sort()
for line in lines:
	v = line.split("\t")
	labels.append(rename(v[0]))
	values.append(map(floatify, v[1:]))
lpid = None
if 'ParallelLinear' in labels:
	lpid = labels.index('ParallelLinear')
	l,v = labels[lpid], values[lpid]
	labels.remove(l)
	values.remove(v)
	labels.append(l)
	values.append(v)
values = numpy.array(values)

ncolumns = len(values[0])
nondominated_ids = []

for i in range(len(values)):
	v1 = values[i]
	# does another dominate this
	is_dominated = False
	for v2 in values:
		better = 0
		for k in range(ncolumns-1):
			if (k == 0 and v2[k] < v1[k]) or (k!=0 and v2[k] > v1[k]):
				better = better + 1
		if better == ncolumns - 1:
			# print "%s dominated by %s" % (i[ncolumns-1], j[ncolumns-1])
			is_dominated = True
			break
	if not is_dominated and v1[1] > 0.5:
		nondominated_ids.append(i)

import scipy
import numpy

def split(a):
	#a.sort(cmp=lambda x,y: int(x[2] - y[2]))
	#print "split", a
	b = numpy.array(map(lambda v:v[:-1], a))
	c = map(lambda v:rename(v[-1]), a)
	#print b, c
	return (b, c)
#def isGA(v):
#	return v[ncolumns-1].startswith("GA survivor")
#ga, ga_labels = split(filter(isGA, values))
dominated_ids = filter(lambda x : x not in nondominated_ids, range(len(labels)))
dominated_labels = map(lambda i: labels[i], dominated_ids)
nondominated_labels = map(lambda i: labels[i], nondominated_ids)
dominated_values = numpy.array(map(lambda i: values[i], dominated_ids))
nondominated_values = numpy.array(map(lambda i: values[i], nondominated_ids))

print nondominated_labels

import matplotlib
matplotlib.use('cairo')
import matplotlib.pyplot as plt
fig = plt.figure(figsize=(5,4), num=2)
plt.ylabel('Observatory-biased fitness value (priority)')
plt.xlabel('Observer-biased fitness value  (continuity)')
#plt.title('Algorithm quality for %.0f%% %s' % (float(oversubs), parallel))
plt.grid(True)

styles_forms = ['s','o','<','>','x','+','*','p','>','v','^','s']
styles_colors = ['b']*len(styles_forms)
styles = map(lambda i:styles_colors[i]+styles_forms[i]+"-", range(len(styles_forms)))
for i in range(len(nondominated_ids)):
	if i < len(styles_forms):
		j = i
		color='b'
	else:
		j = i - len(styles_forms)
		color='g'
	styles[i] = color + styles_forms[j]
	

j = 0
for i in nondominated_ids:
	plt.plot(values[i,2], values[i,3], styles[j], 
		label=labels[i])
	j = j + 1

if len(dominated_values) != 0:
	plt.plot(dominated_values[:,2], dominated_values[:,3], ".", color='grey', label="other heuristics")

if lpid is not None:
	plt.plot(values[lpid,2], values[lpid,3], "rd", label="ParallelLinear")
#plt.plot(ga[:,2], ga[:,1], "yo", label="GA survivors")

plt.ylim(0.0, 1)
plt.xlim(0.0, 1)
plt.legend(loc='lower right', prop=matplotlib.font_manager.FontProperties(size=11) )
#plt.subplots_adjust(bottom = 0.4)
plt.subplots_adjust(right = 0.98)
plt.subplots_adjust(top = 0.95)
plt.subplots_adjust(bottom = 0.15)
#plt.xticks(range(0, 1000+1, 200))
plt.xticks(numpy.arange(0, 1.01, 0.1))
plt.yticks(numpy.arange(0, 1.01, 0.1))
#plt.xlim([-301, 1000])
plt.savefig('observer_observatory_quality_%s_%s.pdf' % (oversubs, parallel))


# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #


fig = plt.figure(figsize=(5,4), num=3)
plt.ylabel('Fitness value')
plt.xlabel('Runtime [s]')
#plt.title('Algorithm quality vs. runtime for %.0f%% %s' % (float(oversubs), parallel))
plt.grid(True)

"""
duration = numpy.array(duration)
values   = numpy.array(values)

labels = duration[:,1]
runtime = duration[:,0]
quality = values[:,0]
vlabels = values[:,-1]
bothlabels = filter(lambda x: x in vlabels, labels)
x =     map   (lambda label: float(runtime[labels == label][0]), bothlabels)
y = 	map   (lambda label: float(quality [vlabels == label][0]), bothlabels)
"""
def isLP(x):
	return x.startswith('ParallelLinear')

lpids = filter(lambda i: isLP(labels[i]), range(len(labels)))
#notlpids = filter(lambda x: not isLP(labels[x]), range(len(values)))
rest = filter(lambda i: not isLP(labels[i]), dominated_ids)
x = map(lambda x:values[x,0], rest)
y = map(lambda x:values[x,1], rest)

#plt.plot([0,max(x)], [max(y)]*2, '--', label='highest heuristic quality (LP excluded)')
#plt.plot([0,max(x)], [max(quality)]*2, '--', label='GA')

j = 0
for i in nondominated_ids:
	if isLP(labels[i]):
		continue
	plt.plot(values[i,0], values[i,1], styles[j], 
		label=labels[i])
	j = j + 1
for i in lpids:
	if len(x) == 0:
		continue
	plt.plot([0,max(x)], [values[i,1]]*2, '--', label=labels[i])

if len(x) > 0:
	plt.plot([0,max(x)], [max(values[:,1])]*2, '--', label='highest')
if len(lpids) > 0 and len(x) > 0:
	lpid = lpids[0]
	plt.plot([0,max(x)], [values[lpid,1]]*2, '--', label=labels[lpid])
if len(x) > 0:
	plt.plot(x, y, '.', color='grey', label='other heuristics')

plt.ylim(0, 1)
plt.xlim(0)
plt.legend(loc='lower right', prop=matplotlib.font_manager.FontProperties(size=11) )
#plt.subplots_adjust(left = 0.08)
plt.subplots_adjust(right = 0.98)
plt.subplots_adjust(top = 0.95)
plt.subplots_adjust(bottom = 0.15)
plt.yticks(numpy.arange(0, 1.01, 0.1))
#plt.xlim([-301, 1000])
plt.savefig('quality_vs_runtime_%s_%s.pdf' % (oversubs, parallel))

