import sys, os

def calctotal(filenames):
	total = 0.
	for f in filenames:
		print "partial", f, 
		lines = file(f).readlines()
		if len(lines) == 0:
			print "is empty!"
			return
		if len(lines) < 200 / 50:
			print "is almost empty!"
			return
		a = [[float(k) for k in l.split()] for l in lines[-5:]]
		#v = max([l[1] for l in a])
		v = a[-3][1]
		if not (v > 0.01):
			print f, "has weird results!"
			return
		print ":", v
		total = total + v
	return total


filenames = sys.argv[1:]

total = calctotal(filenames)
if total > 0.01:
	print total
else:
	print "error", total, "from", filenames
