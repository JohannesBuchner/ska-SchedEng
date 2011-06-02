#!/usr/bin/env python

from sys import argv

def prio2name(filename):
	f = file(filename)
	l = f.readline()
	rel = {}
	for parts in l[4:].split(" + "):
		if parts.strip() == "0;":
			continue
		try:
			prio, name = parts.strip().split(" ",2)
		except ValueError, e:
			print parts.strip()
			print ValueError, e
			break
		prio = float(prio)
		name = name.strip(", ;")
		x, nr, day = name.split("_")
		day = int(day)
		nr = int(nr)
		if not rel.has_key(day):
			#print "new timeslot", day
			rel[day] = {}	
		assert not rel[day].has_key(prio)
		rel[day][prio] = nr
		
	return rel

r1 = prio2name(argv[1])
r2 = prio2name(argv[2])

oldnewmap = {}
for i in r1.keys():
	for prio in r1[i].keys():
		#print i, r2[i][prio], ' <--> ', r1[i][prio]
		oldnewmap["x_%d_%d" % (r1[i][prio], i)] = "x_%d_%d" % (r2[i][prio], i)

f = file(argv[3])
fout = file(argv[4], 'w')
fout.write(f.readline())
fout.write(f.readline())
fout.write(f.readline())
fout.write(f.readline())

for l in f.readlines():
	name, value = l.split()
	fout.write("%s %32s\n" % (oldnewmap[name], value))


