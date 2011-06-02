"""
load schedules -- include heuristics, lp, ga (as available)
print list of schedulername, duration, fitness value
"""

require 'java'
#include_class 'local.radioschedulers.importer.JsonProposalReader'
RubyFile = File
import 'java.io.File'
import 'local.radioschedulers.importer.JsonProposalReader'
import 'local.radioschedulers.importer.CsvScheduleReader'
import 'local.radioschedulers.ScheduleSpace'
import 'local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction'
import 'local.radioschedulers.alg.ga.fitness.NormalizedScheduleFitnessFunction'
#include_class 'File'

puts "# starting"

ndays = 365 / 4
oversubs = ARGV[0]
parallel = ARGV[1]

proposals_file = File.new "proposals_testset_ndays-#{ndays}_oversubs-#{oversubs}.json"
schedules_file = File.new "schedule_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.csv"
schedules_html_file = File.new "schedule_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.html"
space_file = File.new "space_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.csv"

puts "# loading execution times"

def shortname(name)
	i = name.index "@"
	if i == nil then 
		i = name.index " instance "
	end
	if i == nil then 
		#i = name.length
	end
	name = name[0, i]
end

def load_durations(f, durations)
	exectime_file = RubyFile.new f
	exectime_file.readlines().each { |l|
		(duration, name1) = l.split(/\t/)
		name = shortname(name1.strip)
		durations[name] = duration
	}
end

durations = {}
load_durations "executiontime_#{oversubs}_#{parallel}.log", durations
load_durations "executiontime_lp_#{oversubs}_#{parallel}.log", durations


pr = JsonProposalReader.new proposals_file
puts "# loading proposals"
proposals = pr.readall

csv = CsvScheduleReader.new schedules_file, space_file, proposals
puts "# loading space"
space = csv.readspace

puts "# loading schedules"
schedules = csv.readall

puts "# generating list"

def normalized(minutes, space, proposals)
  f = SimpleScheduleFitnessFunction.new
  f.switch_lost_minutes = minutes
  n = NormalizedScheduleFitnessFunction.new f
  n.setupNormalization(space, proposals)
  n
end

f            = normalized 15, space, proposals
fobservatory = normalized  0, space, proposals
fobserver    = normalized 60, space, proposals

out = RubyFile.new "heuristics_stats_#{oversubs}_#{parallel}.tsv", 'w'
schedules.each do |k, v|
	kshort = shortname k
	out.puts "#{k}\t#{durations[kshort]}\t#{f.evaluate(v)}\t#{fobserver.evaluate(v)}\t#{fobservatory.evaluate(v)}"
	#, fnormalize(v)
end


