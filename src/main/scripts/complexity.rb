"""
load space -- calculate number of choices; calculate normalization factor
print list of oversubs, parallel, #choices, normalization
"""

require 'java'
#include_class 'local.radioschedulers.importer.JsonProposalReader'
RubyFile = File
import 'java.io.File'
import 'local.radioschedulers.importer.JsonProposalReader'
import 'local.radioschedulers.importer.CsvScheduleReader'
import 'local.radioschedulers.ScheduleSpace'
import 'local.radioschedulers.alg.ga.fitness.NormalizedScheduleFitnessFunction'
import 'local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction'
#include_class 'File'

puts "# starting"

ndays = 365 / 4
oversubs = ARGV[0]
parallel = ARGV[1]

proposals_file = File.new "proposals_testset_ndays-#{ndays}_oversubs-#{oversubs}.json"
schedules_file = File.new "schedule_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.csv"
schedules_html_file = File.new "schedule_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.html"
space_file = File.new "space_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.csv"
exectime_file = RubyFile.new "executiontime_#{oversubs}_#{parallel}.log"

pr = JsonProposalReader.new proposals_file
puts "# loading proposals"
proposals = pr.readall

csv = CsvScheduleReader.new schedules_file, space_file, proposals
puts "# loading space"
space = csv.readspace

logComplexity = 0
space.each do |e|
	t = e.key
	s = e.value
	#puts "#{t} -- #{s.size}"
	logComplexity += Math.log(s.size)
end

puts "# generating list"

def normalized(minutes, space, proposals)
  f = SimpleScheduleFitnessFunction.new
  f.switch_lost_minutes = minutes
  n = NormalizedScheduleFitnessFunction.new f
  n.setupNormalization(space, proposals)
  n
end

f            = normalized 15, space, proposals

puts "#{oversubs}\t#{parallel}\t#{f.normalization}\t#{logComplexity}"

