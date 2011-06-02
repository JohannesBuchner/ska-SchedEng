import json
import re
import sys
import numpy
import scipy

ndays = 91
#oversubs = sys.argv[1]


def getDemands(oversubs):
	filename = "proposals_testset_ndays-%d_oversubs-%s.json" % (ndays,oversubs)

	j = json.load(file(filename, 'r'))

	demands = {}
	for h in range(0,24*4+1):
		demands[h/4.] = 0

	for propclass in j:
		(cls, proposal) = propclass
		for job in proposal['jobs']:
			timeline = []
			n = job['resources']['antennas']['numberrequired']
			demand = n * job['hours'] / 24.
			for i in range(0,24*4+1):
				h = i / 4.
				within = False
				if job['lstmax'] > job['lstmin']:
					if h >= job['lstmin'] and h < job['lstmax']:
						within = True
				elif job['lstmax'] < job['lstmin']:
					if not (h >= job['lstmax'] and h < job['lstmin']):
						within = True
				if within:
					timeline.append('*')
					demands[h] = demands[h] + demand
				else:
					timeline.append(' ')
			#for i in range(n):
			#	print "".join(timeline)

	#print
	return demands

import matplotlib
matplotlib.use('cairo')
import matplotlib.pyplot as plt

plt.xlim(0, 24)
plt.xticks(range(0, 25, 4))
plt.xlabel("local sidereal time [hours]")
plt.ylabel("# of antennas demanded per day")
for oversubs in ["100.0", "200.0", "300.0", "400.0"]:
	y = []
	x = []
	demands = getDemands(oversubs)
	for h in range(0,24*4+1):
		x.append(h/4.)
		y.append(demands[h/4.] / ndays)

	plt.plot(x, y, ls='steps-', label=oversubs)

plt.plot(x, [42]*len(x), ls='steps--', label='available')
plt.legend(loc='upper left', prop=matplotlib.font_manager.FontProperties(size=11) )
#plt.subplots_adjust(bottom = 0.2)
#plt.xticks(x, xlabels, rotation=90)
#plt.xlim([-301, 1000])
plt.savefig('demand.pdf')

