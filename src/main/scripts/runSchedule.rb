"""
run a scheduler
"""

require 'java'
#include_class 'local.radioschedulers.importer.JsonProposalReader'
RubyFile = File
import 'java.io.File'
import 'local.radioschedulers.importer.JsonProposalReader'
import 'local.radioschedulers.importer.CsvScheduleReader'
import 'local.radioschedulers.ScheduleSpace'
import 'local.radioschedulers.alg.serial.RandomizedSelector'
import 'local.radioschedulers.alg.serial.SmootheningScheduler'
import 'local.radioschedulers.alg.serial.SerialLeastChoiceScheduler'
import 'local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction'
#include_class 'File'

puts "# starting"

ndays = 365 / 4
oversubs = ARGV[0]
parallel = ARGV[1]

proposals_file = File.new "proposals_testset_ndays-#{ndays}_oversubs-#{oversubs}.json"
schedules_file = File.new "schedule_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.csv"
space_file = File.new "space_testset_ndays-#{ndays}_oversubs-#{oversubs}_parallel-#{parallel}.csv"

pr = JsonProposalReader.new proposals_file
puts "# loading proposals"
proposals = pr.readall

csv = CsvScheduleReader.new schedules_file, space_file, proposals
puts "# loading space"
space = csv.readspace

s = SmootheningScheduler.new(SerialLeastChoiceScheduler.new(RandomizedSelector.new))

s.schedule space

